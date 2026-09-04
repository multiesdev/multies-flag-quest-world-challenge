package com.multies.flagquest.data.repository

import android.content.Context
import com.multies.flagquest.data.local.dao.GameDao
import com.multies.flagquest.data.local.entity.AchievementEntity
import com.multies.flagquest.data.local.entity.AtlasEntity
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.StatsEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.local.entity.MissedQuestionEntity
import com.multies.flagquest.data.local.entity.MissionEntity
import com.multies.flagquest.data.local.entity.MemoryLevelEntity
import com.multies.flagquest.data.model.Country
import com.multies.flagquest.data.model.Question
import com.multies.flagquest.data.model.Organization
import com.multies.flagquest.data.model.OrganizationData
import com.multies.flagquest.data.model.QuestionEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class GameRepository(
    private val context: Context,
    private val gameDao: GameDao,
    private val moshi: Moshi
) {
    // --- Room Reactive Flows ---
    val userProfile: Flow<UserProfileEntity?> = gameDao.getUserProfileFlow()
    val allProgress: Flow<List<ProgressEntity>> = gameDao.getAllProgressFlow()
    val allAchievements: Flow<List<AchievementEntity>> = gameDao.getAllAchievementsFlow()
    val atlasDiscoveries: Flow<List<AtlasEntity>> = gameDao.getAtlasDiscoveriesFlow()
    val allStatistics: Flow<List<StatsEntity>> = gameDao.getAllStatsFlow()
    val missedQuestions: Flow<List<MissedQuestionEntity>> = gameDao.getAllMissedQuestionsFlow()
    val allMissions: Flow<List<MissionEntity>> = gameDao.getAllMissionsFlow()
    val memoryRecords: Flow<List<MemoryLevelEntity>> = gameDao.getAllMemoryRecordsFlow()

    // --- Memory Operations ---
    suspend fun getMemoryRecord(levelIndex: Int): MemoryLevelEntity? = withContext(Dispatchers.IO) {
        gameDao.getMemoryRecord(levelIndex)
    }

    suspend fun saveMemoryRecord(record: MemoryLevelEntity) = withContext(Dispatchers.IO) {
        gameDao.insertMemoryRecord(record)
        // Also sync with general level_progress for "MEMORY" category
        val progress = ProgressEntity(
            categoryId = "MEMORY",
            levelIndex = record.levelIndex,
            starsEarned = record.highestStars,
            isCompleted = record.isCompleted,
            highestScore = record.highestScore,
            completedAt = System.currentTimeMillis()
        )
        gameDao.insertProgress(progress)
    }

    // --- User Profile Operations ---
    suspend fun createInitialProfileIfNeeded() = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile()
        if (profile == null) {
            gameDao.insertUserProfile(UserProfileEntity())
            initializeDefaultAchievements()
            refreshMissionsIfNeeded(forceReset = true)
        } else {
            // Ensure achievements and missions are initialized if they aren't
            val achievements = gameDao.getAllAchievements()
            if (achievements.isEmpty()) {
                initializeDefaultAchievements()
            }
            refreshMissionsIfNeeded(forceReset = false)
        }
    }

    suspend fun updateCoins(delta: Int) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(coins = (profile.coins + delta).coerceAtLeast(0))
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateLives(delta: Int) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val newLives = (profile.lives + delta).coerceIn(0, profile.maxLives)
        
        val nextRegen = if (newLives < profile.maxLives) {
            if (profile.nextHeartRegenTimestamp == 0L) {
                System.currentTimeMillis() + 300 * 1000L
            } else {
                profile.nextHeartRegenTimestamp
            }
        } else {
            0L
        }

        val updated = profile.copy(
            lives = newLives,
            nextHeartRegenTimestamp = nextRegen
        )
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateStreak(correct: Boolean) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val newStreak = if (correct) profile.currentStreak + 1 else 0
        val longest = if (newStreak > profile.longestStreak) newStreak else profile.longestStreak
        val updated = profile.copy(currentStreak = newStreak, longestStreak = longest)
        gameDao.updateUserProfile(updated)
        
        if (correct) {
            incrementStatAndCheckAchievements("correct_answers", 1)
            val currentLongest = getStatByKey("highest_streak")?.statValue ?: 0L
            if (longest.toLong() > currentLongest) {
                incrementStatAndCheckAchievements("highest_streak", longest.toLong() - currentLongest)
            }
            
            // Check streak missions
            if (newStreak >= 5) {
                incrementMissionProgress("daily_streak_5", 5)
            }
        }
    }

    suspend fun updateSettings(lang: String, theme: String, music: Boolean, sound: Boolean, vibration: Boolean) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(
            selectedLanguage = lang,
            themeMode = theme,
            isMusicEnabled = music,
            isSoundEnabled = sound,
            isVibrationEnabled = vibration
        )
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateThemeMode(theme: String) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(themeMode = theme)
        gameDao.updateUserProfile(updated)
    }

    // --- Progress & Levels ---
    suspend fun completeLevel(categoryId: String, levelIndex: Int, stars: Int, score: Int) = withContext(Dispatchers.IO) {
        val allProgressList = gameDao.getAllProgressFlow().firstOrNull() ?: emptyList()
        val existingProgress = allProgressList.find {
            it.categoryId.equals(categoryId, ignoreCase = true) && it.levelIndex == levelIndex
        }

        val alreadyCompleted = existingProgress?.isCompleted == true
        val existingStars = existingProgress?.starsEarned ?: 0
        val starsDiff = (stars - existingStars).coerceAtLeast(0)

        val progress = ProgressEntity(
            categoryId = categoryId,
            levelIndex = levelIndex,
            starsEarned = stars.coerceAtLeast(existingStars),
            isCompleted = true,
            highestScore = score.coerceAtLeast(existingProgress?.highestScore ?: 0),
            completedAt = System.currentTimeMillis()
        )
        gameDao.insertProgress(progress)
        
        // Reward level completion bonus coins ONLY ONCE when first completed
        if (!alreadyCompleted) {
            val levelCompletionBonus = when {
                levelIndex <= 10 -> 10
                levelIndex <= 20 -> 15
                levelIndex <= 35 -> 20
                else -> 25
            }
            updateCoins(levelCompletionBonus)
        }

        // Award stars difference to profile totalStars
        if (starsDiff > 0) {
            val profile = gameDao.getUserProfile() ?: UserProfileEntity()
            gameDao.updateUserProfile(profile.copy(totalStars = profile.totalStars + starsDiff))
        }

        // Phase 5 stats checking
        if (!alreadyCompleted) {
            incrementStatAndCheckAchievements("level_completions", 1)
        }
        if (stars == 3 && existingStars < 3) {
            incrementStatAndCheckAchievements("perfect_levels", 1)
        }
        
        if (!alreadyCompleted) {
            when (categoryId.uppercase()) {
                "FLAGS" -> incrementStatAndCheckAchievements("category_flags_completed", 1)
                "ORGANIZATIONS" -> incrementStatAndCheckAchievements("category_organizations_completed", 1)
                "AREA" -> incrementStatAndCheckAchievements("category_area_completed", 1)
                "POPULATION" -> incrementStatAndCheckAchievements("category_population_completed", 1)
                "CAPITALS" -> incrementStatAndCheckAchievements("category_capitals_completed", 1)
                "CURRENCIES" -> incrementStatAndCheckAchievements("category_currencies_completed", 1)
                "MAPS" -> incrementStatAndCheckAchievements("category_maps_completed", 1)
            }
        }
        
        // Add XP reward for completing level (50 XP standard, only on first completion)
        if (!alreadyCompleted) {
            addXp(50)
        }
    }

    // --- Discoveries (Atlas) ---
    suspend fun discoverCountry(countryId: String) = withContext(Dispatchers.IO) {
        val existing = gameDao.getAtlasDiscovery(countryId)
        val isFirstDiscovery = existing == null
        val discovery = AtlasEntity(
            countryId = countryId,
            isDiscovered = true,
            answeredCorrectlyCount = (existing?.answeredCorrectlyCount ?: 0) + 1,
            discoveredAt = System.currentTimeMillis(),
            isFavorite = existing?.isFavorite ?: false
        )
        gameDao.insertAtlasDiscovery(discovery)
        
        if (isFirstDiscovery) {
            incrementStatAndCheckAchievements("countries_discovered", 1)
            
            // Check Daily Mission: "Discover 3 countries"
            incrementMissionProgress("daily_countries", 1)
            
            val countries = loadCountries()
            val country = countries.find { it.id == countryId }
            val continent = country?.continentEn?.lowercase() ?: ""
            if (continent.contains("europe")) {
                incrementStatAndCheckAchievements("europe_discovered", 1)
            } else if (continent.contains("africa")) {
                incrementStatAndCheckAchievements("africa_discovered", 1)
            } else if (continent.contains("asia")) {
                incrementStatAndCheckAchievements("asia_discovered", 1)
            }
        }
    }

    suspend fun toggleFavorite(countryId: String) = withContext(Dispatchers.IO) {
        val existing = gameDao.getAtlasDiscovery(countryId)
        if (existing != null) {
            val updated = existing.copy(isFavorite = !existing.isFavorite)
            gameDao.insertAtlasDiscovery(updated)
        } else {
            val discovery = AtlasEntity(
                countryId = countryId,
                isDiscovered = false,
                isFavorite = true
            )
            gameDao.insertAtlasDiscovery(discovery)
        }
    }

    // --- Achievements ---
    private suspend fun initializeDefaultAchievements() {
        val defaults = listOf(
            AchievementEntity("correct_1", "ach_correct_1_title", "ach_correct_1_desc", "emoji_star", maxProgress = 1),
            AchievementEntity("correct_10", "ach_correct_10_title", "ach_correct_10_desc", "emoji_medal", maxProgress = 10),
            AchievementEntity("correct_100", "ach_correct_100_title", "ach_correct_100_desc", "emoji_trophy", maxProgress = 100),
            AchievementEntity("streak_5", "ach_streak_5_title", "ach_streak_5_desc", "emoji_fire", maxProgress = 5),
            AchievementEntity("streak_10", "ach_streak_10_title", "ach_streak_10_desc", "emoji_lightning", maxProgress = 10),
            AchievementEntity("streak_20", "ach_streak_20_title", "ach_streak_20_desc", "emoji_crown", maxProgress = 20),
            AchievementEntity("explorer_europe", "ach_explorer_europe_title", "ach_explorer_europe_desc", "emoji_map_europe", maxProgress = 5),
            AchievementEntity("explorer_africa", "ach_explorer_africa_title", "ach_explorer_africa_desc", "emoji_map_africa", maxProgress = 5),
            AchievementEntity("explorer_asia", "ach_explorer_asia_title", "ach_explorer_asia_desc", "emoji_map_asia", maxProgress = 5),
            AchievementEntity("master_flag", "ach_master_flag_title", "ach_master_flag_desc", "emoji_flag_master", maxProgress = 5),
            AchievementEntity("master_org", "ach_master_org_title", "ach_master_org_desc", "emoji_org_master", maxProgress = 3),
            AchievementEntity("master_area", "ach_master_area_title", "ach_master_area_desc", "emoji_area_master", maxProgress = 3),
            AchievementEntity("master_pop", "ach_master_pop_title", "ach_master_pop_desc", "emoji_pop_master", maxProgress = 3),
            AchievementEntity("atlas_25", "ach_atlas_25_title", "ach_atlas_25_desc", "emoji_globe_25", maxProgress = 10),
            AchievementEntity("atlas_50", "ach_atlas_50_title", "ach_atlas_50_desc", "emoji_globe_50", maxProgress = 20),
            AchievementEntity("atlas_100", "ach_atlas_100_title", "ach_atlas_100_desc", "emoji_globe_100", maxProgress = 40),
            AchievementEntity("perfect_level", "ach_perfect_level_title", "ach_perfect_level_desc", "emoji_perfect", maxProgress = 1)
        )
        gameDao.insertAchievements(defaults)
    }

    suspend fun incrementStatAndCheckAchievements(statKey: String, increment: Long) = withContext(Dispatchers.IO) {
        val existing = gameDao.getStatByKey(statKey)
        val currentVal = (existing?.statValue ?: 0L) + increment
        gameDao.insertStat(StatsEntity(statKey, currentVal))
        
        // Fetch achievements
        val achievements = gameDao.getAllAchievements()
        
        val statMapping = mapOf(
            "correct_answers" to listOf("correct_1" to 1, "correct_10" to 10, "correct_100" to 100),
            "highest_streak" to listOf("streak_5" to 5, "streak_10" to 10, "streak_20" to 20),
            "europe_discovered" to listOf("explorer_europe" to 5),
            "africa_discovered" to listOf("explorer_africa" to 5),
            "asia_discovered" to listOf("explorer_asia" to 5),
            "category_flags_completed" to listOf("master_flag" to 5),
            "category_organizations_completed" to listOf("master_org" to 3),
            "category_area_completed" to listOf("master_area" to 3),
            "category_population_completed" to listOf("master_pop" to 3),
            "countries_discovered" to listOf("atlas_25" to 10, "atlas_50" to 20, "atlas_100" to 40),
            "perfect_levels" to listOf("perfect_level" to 1)
        )
        
        val mappings = statMapping[statKey] ?: return@withContext
        for ((achId, threshold) in mappings) {
            val ach = achievements.find { it.id == achId } ?: continue
            if (ach.isUnlocked) continue
            
            val newProgress = currentVal.toInt().coerceAtMost(threshold)
            if (newProgress != ach.progress) {
                val isNowUnlocked = newProgress >= threshold
                val updatedAch = ach.copy(
                    progress = newProgress,
                    isUnlocked = isNowUnlocked,
                    unlockedAt = if (isNowUnlocked) System.currentTimeMillis() else ach.unlockedAt
                )
                gameDao.updateAchievement(updatedAch)
                
                if (isNowUnlocked) {
                    val reward = when (achId) {
                        "correct_1" -> 50
                        "correct_10" -> 100
                        "correct_100" -> 250
                        "streak_5" -> 50
                        "streak_10" -> 100
                        "streak_20" -> 200
                        "perfect_level" -> 50
                        "explorer_europe", "explorer_africa", "explorer_asia" -> 50
                        "master_flag", "master_org", "master_area", "master_pop" -> 100
                        "atlas_25" -> 100
                        "atlas_50" -> 200
                        "atlas_100" -> 500
                        else -> 50
                    }
                    updateCoins(reward)
                    
                    if (achId == "correct_100" || achId == "streak_20") {
                        updateHintsCount(1)
                    } else if (achId == "atlas_100") {
                        updateHintsCount(3)
                    }
                }
            }
        }
    }

    suspend fun unlockAchievement(id: String) = withContext(Dispatchers.IO) {
        val achievements = gameDao.getAllAchievements()
        val matching = achievements.find { it.id == id }
        if (matching != null && !matching.isUnlocked) {
            gameDao.updateAchievement(matching.copy(
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis()
            ))
            updateCoins(50) // Bonus for unlocking achievement
        }
    }

    suspend fun insertStat(key: String, value: Long) = withContext(Dispatchers.IO) {
        gameDao.insertStat(StatsEntity(key, value))
    }

    suspend fun getStatByKey(key: String): StatsEntity? = withContext(Dispatchers.IO) {
        gameDao.getStatByKey(key)
    }

    suspend fun getMissedQuestionsList(): List<MissedQuestionEntity> = withContext(Dispatchers.IO) {
        gameDao.getAllMissedQuestionsFlow().firstOrNull() ?: emptyList()
    }

    // --- Phase 5 Player XP, Hints & Cosmetic Systems ---
    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        var currentXp = profile.xp + amount
        var currentLevel = profile.level
        var coinsGained = 0
        var hintsGained = 0
        
        while (currentXp >= currentLevel * 100) {
            currentXp -= currentLevel * 100
            currentLevel += 1
            coinsGained += 50
            hintsGained += 1
        }
        
        val updated = profile.copy(
            xp = currentXp,
            level = currentLevel,
            coins = profile.coins + coinsGained,
            hintsCount = profile.hintsCount + hintsGained
        )
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateHintsCount(delta: Int) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(hintsCount = (profile.hintsCount + delta).coerceAtLeast(0))
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateLastSpinTimestamp(timestamp: Long) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(lastSpinTimestamp = timestamp)
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateLastDailyRewardClaim(dayClaimed: Int, timestamp: Long) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(
            lastDailyRewardDayClaimed = dayClaimed,
            lastDailyRewardClaimed = timestamp
        )
        gameDao.updateUserProfile(updated)
    }

    suspend fun updatePurchasedThemes(themes: String) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(purchasedThemes = themes)
        gameDao.updateUserProfile(updated)
    }

    suspend fun updatePremiumThemeTrial(expiry: Long) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(premiumThemeTrialExpiry = expiry)
        gameDao.updateUserProfile(updated)
    }

    suspend fun updateAvatarId(avatarId: String) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val updated = profile.copy(avatarId = avatarId)
        gameDao.updateUserProfile(updated)
    }

    // --- Phase 5 Mission System Logic ---
    suspend fun refreshMissionsIfNeeded(forceReset: Boolean = false) = withContext(Dispatchers.IO) {
        val missions = gameDao.getAllMissions()
        val now = System.currentTimeMillis()
        
        val lastDailyReset = gameDao.getStatByKey("last_daily_reset")?.statValue ?: 0L
        val lastWeeklyReset = gameDao.getStatByKey("last_weekly_reset")?.statValue ?: 0L
        
        val calendar = java.util.Calendar.getInstance()
        val todayYearDay = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = lastDailyReset
        val lastDailyYearDay = calendar.get(java.util.Calendar.YEAR) * 1000 + calendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = now
        val currentWeek = calendar.get(java.util.Calendar.YEAR) * 100 + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        
        calendar.timeInMillis = lastWeeklyReset
        val lastWeeklyWeekCode = calendar.get(java.util.Calendar.YEAR) * 100 + calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        
        val dailyResetTriggered = forceReset || lastDailyReset == 0L || todayYearDay != lastDailyYearDay
        val weeklyResetTriggered = forceReset || lastWeeklyReset == 0L || currentWeek != lastWeeklyWeekCode
        
        if (missions.isEmpty()) {
            val defaults = getInitialMissions(now)
            gameDao.insertMissions(defaults)
            gameDao.insertStat(StatsEntity("last_daily_reset", now))
            gameDao.insertStat(StatsEntity("last_weekly_reset", now))
        } else {
            val updated = missions.map { m ->
                if (m.type == "DAILY" && dailyResetTriggered) {
                    m.copy(currentProgress = 0, isCompleted = false, isClaimed = false, lastUpdatedTimestamp = now)
                } else if (m.type == "WEEKLY" && weeklyResetTriggered) {
                    m.copy(currentProgress = 0, isCompleted = false, isClaimed = false, lastUpdatedTimestamp = now)
                } else {
                    m
                }
            }
            gameDao.insertMissions(updated)
            if (dailyResetTriggered) {
                gameDao.insertStat(StatsEntity("last_daily_reset", now))
            }
            if (weeklyResetTriggered) {
                gameDao.insertStat(StatsEntity("last_weekly_reset", now))
            }
        }
    }

    private fun getInitialMissions(timestamp: Long): List<MissionEntity> = listOf(
        MissionEntity("daily_questions", "daily_questions", "DAILY", 0, 10, false, false, 25, 0, timestamp),
        MissionEntity("daily_countries", "daily_countries", "DAILY", 0, 3, false, false, 20, 1, timestamp),
        MissionEntity("daily_no_hints", "daily_no_hints", "DAILY", 0, 1, false, false, 15, 0, timestamp),
        MissionEntity("daily_streak_5", "daily_streak_5", "DAILY", 0, 5, false, false, 30, 0, timestamp),
        MissionEntity("daily_two_cats", "daily_two_cats", "DAILY", 0, 2, false, false, 20, 0, timestamp),
        MissionEntity("weekly_capitals", "weekly_capitals", "WEEKLY", 0, 10, false, false, 60, 1, timestamp),
        MissionEntity("weekly_timed_level", "weekly_timed_level", "WEEKLY", 0, 1, false, false, 40, 0, timestamp)
    )

    suspend fun incrementMissionProgress(id: String, amount: Int) = withContext(Dispatchers.IO) {
        refreshMissionsIfNeeded()
        val missions = gameDao.getAllMissions()
        val m = missions.find { m -> m.id == id } ?: return@withContext
        if (m.isCompleted) return@withContext
        
        val newProgress = (m.currentProgress + amount).coerceAtMost(m.targetProgress)
        val completed = newProgress >= m.targetProgress
        val updated = m.copy(
            currentProgress = newProgress,
            isCompleted = completed,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        gameDao.updateMission(updated)
    }

    suspend fun claimMissionReward(id: String) = withContext(Dispatchers.IO) {
        val missions = gameDao.getAllMissions()
        val m = missions.find { m -> m.id == id } ?: return@withContext
        if (m.isCompleted && !m.isClaimed) {
            val updated = m.copy(isClaimed = true)
            gameDao.updateMission(updated)
            
            // Give Rewards!
            updateCoins(m.rewardCoins)
            if (m.rewardHints > 0) {
                updateHintsCount(m.rewardHints)
            }
        }
    }

    // --- Missed Questions (Smart Review) ---
    suspend fun addMissedQuestion(questionId: String, categoryId: String) = withContext(Dispatchers.IO) {
        val existing = gameDao.getMissedQuestion(questionId)
        if (existing == null) {
            gameDao.insertMissedQuestion(MissedQuestionEntity(
                questionId = questionId,
                categoryId = categoryId,
                incorrectCount = 1,
                correctCountInARow = 0,
                nextReviewTimestamp = System.currentTimeMillis() + 10 * 60 * 1000 // 10 mins delay
            ))
        } else {
            gameDao.insertMissedQuestion(existing.copy(
                incorrectCount = existing.incorrectCount + 1,
                correctCountInARow = 0,
                nextReviewTimestamp = System.currentTimeMillis() + 10 * 60 * 1000
            ))
        }
    }

    suspend fun recordReviewSuccess(questionId: String) = withContext(Dispatchers.IO) {
        val existing = gameDao.getMissedQuestion(questionId)
        if (existing != null) {
            val newCorrect = existing.correctCountInARow + 1
            if (newCorrect >= 3) {
                gameDao.deleteMissedQuestion(questionId)
            } else {
                gameDao.insertMissedQuestion(existing.copy(
                    correctCountInARow = newCorrect,
                    nextReviewTimestamp = System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour delay
                ))
            }
        }
    }

    // --- Modes and Adaptive Difficulty ---
    suspend fun setRelaxedMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        gameDao.updateUserProfile(profile.copy(relaxedMode = enabled))
    }

    suspend fun setClassroomMode(enabled: Boolean) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        gameDao.updateUserProfile(profile.copy(classroomMode = enabled))
    }

    suspend fun setAdaptiveDifficultyEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        gameDao.updateUserProfile(profile.copy(isAdaptiveDifficultyEnabled = enabled))
    }

    suspend fun updateAdaptiveDifficultyOffset(offset: Int) = withContext(Dispatchers.IO) {
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        gameDao.updateUserProfile(profile.copy(adaptiveDifficultyOffset = offset.coerceIn(-1, 1)))
    }

    suspend fun updateUserProfileDirectly(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        gameDao.updateUserProfile(profile)
    }

    // --- Static Geo-Data Loaded from Assets JSON ---
    suspend fun loadCountries(): List<Country> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("countries.json").bufferedReader().use { it.readText() }
            val type = Types.newParameterizedType(List::class.java, Country::class.java)
            val adapter = moshi.adapter<List<Country>>(type)
            adapter.fromJson(jsonString) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackCountries()
        }
    }

    suspend fun loadOrganizations(): List<Organization> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("organizations.json").bufferedReader().use { it.readText() }
            val adapter = moshi.adapter(OrganizationData::class.java)
            adapter.fromJson(jsonString)?.organizations ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadQuestions(): List<Question> = withContext(Dispatchers.IO) {
        try {
            val countries = loadCountries()
            val organizations = loadOrganizations()
            QuestionEngine.generateAllQuestions(countries, organizations)
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackQuestions()
        }
    }

    // Fallbacks if assets are empty or corrupted during first boot
    private fun getFallbackCountries(): List<Country> = listOf(
        Country(
            id = "US",
            nameEn = "United States", nameAr = "الولايات المتحدة", nameDe = "Vereinigte Staaten", nameFr = "États-Unis",
            flagEmoji = "🇺🇸",
            capitalEn = "Washington, D.C.", capitalAr = "واشنطن العاصمة", capitalDe = "Washington, D.C.", capitalFr = "Washington",
            continentEn = "North America", continentAr = "أمريكا الشمالية", continentDe = "Nordamerika", continentFr = "Amérique du Nord",
            population = 333000000L, areaSqKm = 9833517.0,
            currencyEn = "US Dollar", currencyAr = "دولار أمريكي", currencyDe = "US-Dollar", currencyFr = "Dollar américain",
            funFactEn = "The United States does not have an official language at the federal level.",
            funFactAr = "الولايات المتحدة لا تملك لغة رسمية على المستوى الفيدرالي.",
            funFactDe = "Die Vereinigten Staaten haben auf Bundesebene keine offizielle Sprache.",
            funFactFr = "Les États-Unis n'ont pas de langue officielle au niveau fédéral."
        ),
        Country(
            id = "SA",
            nameEn = "Saudi Arabia", nameAr = "المملكة العربية السعودية", nameDe = "Saudi-Arabien", nameFr = "Arabie Saoudite",
            flagEmoji = "🇸🇦",
            capitalEn = "Riyadh", capitalAr = "الرياض", capitalDe = "Riad", capitalFr = "Riyad",
            continentEn = "Asia", continentAr = "آسيا", continentDe = "Asien", continentFr = "Asie",
            population = 36000000L, areaSqKm = 2149690.0,
            currencyEn = "Saudi Riyal", currencyAr = "ريال سعودي", currencyDe = "Saudi-Riyal", currencyFr = "Riyal saoudien",
            funFactEn = "Saudi Arabia is the largest country in the world without a permanent river.",
            funFactAr = "المملكة العربية السعودية هي أكبر دولة في العالم لا تحتوي على نهر دائم.",
            funFactDe = "Saudi-Arabien ist das größte Land der Welt ohne einen permanenten Fluss.",
            funFactFr = "L'Arabie Saoudite est le plus grand pays du monde sans rivière permanente."
        ),
        Country(
            id = "FR",
            nameEn = "France", nameAr = "فرنسا", nameDe = "Frankreich", nameFr = "France",
            flagEmoji = "🇫🇷",
            capitalEn = "Paris", capitalAr = "باريس", capitalDe = "Paris", capitalFr = "Paris",
            continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe",
            population = 68000000L, areaSqKm = 551695.0,
            currencyEn = "Euro", currencyAr = "يورو", currencyDe = "Euro", currencyFr = "Euro",
            funFactEn = "France is the most visited country in the world, welcoming nearly 90 million tourists annually.",
            funFactAr = "فرنسا هي الدولة الأكثر زيارة في العالم، حيث تستقبل ما يقارب 90 مليون سائح سنوياً.",
            funFactDe = "Frankreich ist das meistbesuchte Land der Welt mit fast 90 Millionen Touristen jährlich.",
            funFactFr = "La France est le pays le plus visité au monde, accueillant près de 90 millions de touristes par an."
        ),
        Country(
            id = "DE",
            nameEn = "Germany", nameAr = "ألمانيا", nameDe = "Deutschland", nameFr = "Allemagne",
            flagEmoji = "🇩🇪",
            capitalEn = "Berlin", capitalAr = "برلين", capitalDe = "Berlin", capitalFr = "Berlin",
            continentEn = "Europe", continentAr = "أوروبا", continentDe = "Europa", continentFr = "Europe",
            population = 84000000L, areaSqKm = 357022.0,
            currencyEn = "Euro", currencyAr = "يورو", currencyDe = "Euro", currencyFr = "Euro",
            funFactEn = "Germany shares borders with nine other countries.",
            funFactAr = "تشترك ألمانيا في الحدود مع تسع دول أخرى.",
            funFactDe = "Deutschland grenzt an neun andere Länder.",
            funFactFr = "L'Allemagne partage ses frontières avec neuf autres pays."
        )
    )

    private fun getFallbackQuestions(): List<Question> = listOf(
        Question(
            id = "q_flag_us",
            category = "FLAGS",
            difficulty = "EASY",
            questionTextEn = "Which country does this flag 🇺🇸 belong to?",
            questionTextAr = "إلى أي بلد ينتمي هذا العلم 🇺🇸؟",
            questionTextDe = "Zu welchem Land gehört diese Flagge 🇺🇸?",
            questionTextFr = "À quel pays appartient ce drapeau 🇺🇸?",
            optionsEn = listOf("United States", "United Kingdom", "Australia", "Canada"),
            optionsAr = listOf("الولايات المتحدة", "المملكة المتحدة", "أستراليا", "كندا"),
            optionsDe = listOf("Vereinigte Staaten", "Vereinigtes Königreich", "Australien", "Kanada"),
            optionsFr = listOf("États-Unis", "Royaume-Uni", "Australie", "Canada"),
            correctOptionIndex = 0,
            countryId = "US"
        ),
        Question(
            id = "q_capital_sa",
            category = "CAPITALS",
            difficulty = "EASY",
            questionTextEn = "What is the capital of Saudi Arabia 🇸🇦?",
            questionTextAr = "ما هي عاصمة المملكة العربية السعودية 🇸🇦؟",
            questionTextDe = "Was ist die Hauptstadt von Saudi-Arabien 🇸🇦?",
            questionTextFr = "Quelle est la capitale de l'Arabie Saoudite 🇸🇦?",
            optionsEn = listOf("Riyadh", "Jeddah", "Mecca", "Medina"),
            optionsAr = listOf("الرياض", "جدة", "مكة المكرمة", "المدينة المنورة"),
            optionsDe = listOf("Riad", "Dschidda", "Mekka", "Medina"),
            optionsFr = listOf("Riyad", "Djeddah", "La Mecque", "Médine"),
            correctOptionIndex = 0,
            countryId = "SA"
        )
    )
}
