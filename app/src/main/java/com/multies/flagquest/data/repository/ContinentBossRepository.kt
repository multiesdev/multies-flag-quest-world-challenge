package com.multies.flagquest.data.repository

import com.multies.flagquest.data.local.dao.GameDao
import com.multies.flagquest.data.local.entity.AtlasEntity
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.StatsEntity
import com.multies.flagquest.data.model.BossContinent
import com.multies.flagquest.data.model.ContinentBossEngine
import com.multies.flagquest.data.model.ContinentBossRecord
import com.multies.flagquest.data.model.Country
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContinentBossRepository(
    private val gameDao: GameDao,
    private val gameRepository: GameRepository
) {

    suspend fun getBossRecords(
        allCountries: List<Country>,
        discoveries: List<AtlasEntity>,
        allProgress: List<ProgressEntity>
    ): List<ContinentBossRecord> = withContext(Dispatchers.IO) {
        val bosses = ContinentBossEngine.supportedBosses
        val totalStars = allProgress.sumOf { it.starsEarned }

        bosses.map { boss ->
            val continentCountries = allCountries.filter { it.continentEn.equals(boss.continentEn, ignoreCase = true) }
            val discoveredInCont = continentCountries.count { country ->
                discoveries.any { d -> d.countryId == country.id && d.isDiscovered }
            }

            val isUnlocked = discoveredInCont >= boss.requiredDiscoveries || totalStars >= boss.requiredStars
            val unlockText = if (isUnlocked) {
                ""
            } else {
                "Discover ${boss.requiredDiscoveries} countries in ${boss.continentEn} ($discoveredInCont/${boss.requiredDiscoveries})"
            }

            val completedStat = gameDao.getStatByKey("boss_completed_${boss.bossId}")
            val starsStat = gameDao.getStatByKey("boss_stars_${boss.bossId}")
            val scoreStat = gameDao.getStatByKey("boss_score_${boss.bossId}")
            val accStat = gameDao.getStatByKey("boss_accuracy_${boss.bossId}")
            val heartsStat = gameDao.getStatByKey("boss_hearts_left_${boss.bossId}")

            val isCompleted = (completedStat?.statValue ?: 0L) > 0L
            val starsEarned = (starsStat?.statValue ?: 0L).toInt()
            val highestScore = (scoreStat?.statValue ?: 0L).toInt()
            val bestAccuracyPct = (accStat?.statValue ?: 0L).toInt()
            val bestHeartsLeft = (heartsStat?.statValue ?: 0L).toInt()

            ContinentBossRecord(
                bossId = boss.bossId,
                isUnlocked = isUnlocked,
                unlockRequirementText = unlockText,
                isCompleted = isCompleted,
                starsEarned = starsEarned,
                highestScore = highestScore,
                bestAccuracyPct = bestAccuracyPct,
                bestHeartsLeft = bestHeartsLeft
            )
        }
    }

    suspend fun recordBossVictory(
        boss: BossContinent,
        score: Int,
        accuracyPct: Int,
        heartsLeft: Int
    ): Pair<Int, Boolean> = withContext(Dispatchers.IO) { // Returns (coinsReward, isNewRecord)
        val prevStarsStat = gameDao.getStatByKey("boss_stars_${boss.bossId}")
        val previousStars = (prevStarsStat?.statValue ?: 0L).toInt()

        val stars = when {
            accuracyPct >= 90 && heartsLeft >= 2 -> 3
            accuracyPct >= 70 && heartsLeft >= 1 -> 2
            else -> 1
        }

        // Save progress stats
        gameDao.insertStat(StatsEntity("boss_completed_${boss.bossId}", 1L))

        if (stars > previousStars) {
            gameDao.insertStat(StatsEntity("boss_stars_${boss.bossId}", stars.toLong()))
        }

        val prevScore = gameDao.getStatByKey("boss_score_${boss.bossId}")?.statValue ?: 0L
        if (score > prevScore) {
            gameDao.insertStat(StatsEntity("boss_score_${boss.bossId}", score.toLong()))
        }

        val prevAcc = gameDao.getStatByKey("boss_accuracy_${boss.bossId}")?.statValue ?: 0L
        if (accuracyPct > prevAcc) {
            gameDao.insertStat(StatsEntity("boss_accuracy_${boss.bossId}", accuracyPct.toLong()))
        }

        val prevHearts = gameDao.getStatByKey("boss_hearts_left_${boss.bossId}")?.statValue ?: 0L
        if (heartsLeft > prevHearts) {
            gameDao.insertStat(StatsEntity("boss_hearts_left_${boss.bossId}", heartsLeft.toLong()))
        }

        // Save in level_progress for general star stats aggregation
        val progressIndex = when (boss.bossId) {
            "boss_europe" -> 101
            "boss_africa" -> 102
            "boss_asia" -> 103
            "boss_north_america" -> 104
            "boss_south_america" -> 105
            else -> 106
        }
        gameDao.insertProgress(
            ProgressEntity(
                categoryId = "BOSS_CONTINENT",
                levelIndex = progressIndex,
                starsEarned = maxOf(stars, previousStars),
                isCompleted = true,
                highestScore = maxOf(score, prevScore.toInt()),
                completedAt = System.currentTimeMillis()
            )
        )

        // Grant coins: First-time completion bonus = 250 coins. Improvement bonus = 100 coins. Repeat = 25 coins.
        val isFirstTime = previousStars == 0
        val coinsToGrant = when {
            isFirstTime -> 250
            stars > previousStars -> 100
            else -> 25 // Repeat victory small reward
        }

        gameRepository.updateCoins(coinsToGrant)
        Pair(coinsToGrant, stars > previousStars || isFirstTime)
    }
}
