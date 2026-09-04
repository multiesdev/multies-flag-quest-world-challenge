package com.multies.flagquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.multies.flagquest.data.local.entity.AchievementEntity
import com.multies.flagquest.data.local.entity.AtlasEntity
import com.multies.flagquest.data.local.entity.ProgressEntity
import com.multies.flagquest.data.local.entity.StatsEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.local.entity.MissedQuestionEntity
import com.multies.flagquest.data.local.entity.MissionEntity
import com.multies.flagquest.data.local.entity.DailySpinEntity
import com.multies.flagquest.data.local.entity.SpinTransactionEntity
import com.multies.flagquest.data.local.entity.SpinRewardHistoryEntity
import com.multies.flagquest.data.local.entity.MemoryLevelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)


    // --- Level Progress ---
    @Query("SELECT * FROM level_progress ORDER BY categoryId, levelIndex")
    fun getAllProgressFlow(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE categoryId = :categoryId")
    fun getProgressByCategoryFlow(categoryId: String): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)


    // --- Achievements ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)


    // --- Atlas Discoveries ---
    @Query("SELECT * FROM atlas_discoveries")
    fun getAtlasDiscoveriesFlow(): Flow<List<AtlasEntity>>

    @Query("SELECT * FROM atlas_discoveries WHERE countryId = :countryId")
    suspend fun getAtlasDiscovery(countryId: String): AtlasEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtlasDiscovery(discovery: AtlasEntity)

    @Query("UPDATE atlas_discoveries SET isFavorite = :isFavorite WHERE countryId = :countryId")
    suspend fun updateFavorite(countryId: String, isFavorite: Boolean)


    // --- Statistics ---
    @Query("SELECT * FROM statistics")
    fun getAllStatsFlow(): Flow<List<StatsEntity>>

    @Query("SELECT * FROM statistics WHERE statKey = :key")
    suspend fun getStatByKey(key: String): StatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: StatsEntity)

    // --- Missed Questions (Smart Review) ---
    @Query("SELECT * FROM missed_questions")
    fun getAllMissedQuestionsFlow(): Flow<List<MissedQuestionEntity>>

    @Query("SELECT * FROM missed_questions WHERE questionId = :questionId")
    suspend fun getMissedQuestion(questionId: String): MissedQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedQuestion(missedQuestion: MissedQuestionEntity)

    @Query("DELETE FROM missed_questions WHERE questionId = :questionId")
    suspend fun deleteMissedQuestion(questionId: String)

    // --- Missions ---
    @Query("SELECT * FROM missions")
    fun getAllMissionsFlow(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions")
    suspend fun getAllMissions(): List<MissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Update
    suspend fun updateMission(mission: MissionEntity)

    // --- Daily Spin & Transactions ---
    @Query("SELECT * FROM daily_spin_state WHERE id = 1")
    fun getDailySpinFlow(): Flow<DailySpinEntity?>

    @Query("SELECT * FROM daily_spin_state WHERE id = 1")
    suspend fun getDailySpin(): DailySpinEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySpin(state: DailySpinEntity)

    @Update
    suspend fun updateDailySpin(state: DailySpinEntity)

    @Query("SELECT * FROM spin_transactions WHERE transactionId = :txId")
    suspend fun getTransaction(txId: String): SpinTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: SpinTransactionEntity)

    @Update
    suspend fun updateTransaction(tx: SpinTransactionEntity)

    @Query("SELECT * FROM spin_reward_history ORDER BY timestamp DESC")
    fun getRewardHistoryFlow(): Flow<List<SpinRewardHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardHistory(history: SpinRewardHistoryEntity)

    @Query("SELECT * FROM spin_reward_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRewardHistory(): SpinRewardHistoryEntity?

    // --- Memory Level Records ---
    @Query("SELECT * FROM memory_level_records ORDER BY levelIndex")
    fun getAllMemoryRecordsFlow(): Flow<List<MemoryLevelEntity>>

    @Query("SELECT * FROM memory_level_records WHERE levelIndex = :levelIndex")
    suspend fun getMemoryRecord(levelIndex: Int): MemoryLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryRecord(record: MemoryLevelEntity)
}
