package com.multies.flagquest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.multies.flagquest.data.local.dao.GameDao
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

@Database(
    entities = [
        UserProfileEntity::class,
        ProgressEntity::class,
        AchievementEntity::class,
        AtlasEntity::class,
        StatsEntity::class,
        MissedQuestionEntity::class,
        MissionEntity::class,
        DailySpinEntity::class,
        SpinTransactionEntity::class,
        SpinRewardHistoryEntity::class,
        MemoryLevelEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to user_profile
                db.execSQL("ALTER TABLE user_profile ADD COLUMN nextHeartRegenTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN relaxedMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN classroomMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isAdaptiveDifficultyEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN adaptiveDifficultyOffset INTEGER NOT NULL DEFAULT 0")
                
                // Create missed_questions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS missed_questions (
                        questionId TEXT NOT NULL PRIMARY KEY,
                        categoryId TEXT NOT NULL,
                        incorrectCount INTEGER NOT NULL DEFAULT 1,
                        correctCountInARow INTEGER NOT NULL DEFAULT 0,
                        nextReviewTimestamp INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns for Phase 5 to user_profile
                db.execSQL("ALTER TABLE user_profile ADD COLUMN xp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN level INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN hintsCount INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN premiumThemeTrialExpiry INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastDailyRewardDayClaimed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN lastSpinTimestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN avatarId TEXT NOT NULL DEFAULT 'default'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN purchasedThemes TEXT NOT NULL DEFAULT 'system,light,dark'")
                
                // Create missions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS missions (
                        id TEXT NOT NULL PRIMARY KEY,
                        titleKey TEXT NOT NULL,
                        type TEXT NOT NULL,
                        currentProgress INTEGER NOT NULL DEFAULT 0,
                        targetProgress INTEGER NOT NULL DEFAULT 10,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isClaimed INTEGER NOT NULL DEFAULT 0,
                        rewardCoins INTEGER NOT NULL DEFAULT 20,
                        rewardHints INTEGER NOT NULL DEFAULT 0,
                        lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isFavorite column to atlas_discoveries
                db.execSQL("ALTER TABLE atlas_discoveries ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_spin_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        lastSpinDate TEXT NOT NULL,
                        lastSpinTimestamp INTEGER NOT NULL,
                        nextEligibleDate TEXT NOT NULL,
                        lastRewardId TEXT NOT NULL,
                        lastTransactionId TEXT NOT NULL,
                        claimStatus TEXT NOT NULL,
                        timezoneId TEXT NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS spin_transactions (
                        transactionId TEXT NOT NULL PRIMARY KEY,
                        timestamp INTEGER NOT NULL,
                        rewardId TEXT NOT NULL,
                        status TEXT NOT NULL,
                        dateStr TEXT NOT NULL,
                        coinsGranted INTEGER NOT NULL,
                        heartsGranted INTEGER NOT NULL,
                        overflowCoinsGranted INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS spin_reward_history (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        transactionId TEXT NOT NULL,
                        rewardId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        dateStr TEXT NOT NULL,
                        coinsWon INTEGER NOT NULL,
                        heartsWon INTEGER NOT NULL,
                        overflowCoins INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS memory_level_records (
                        levelIndex INTEGER NOT NULL PRIMARY KEY,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        bestCompletionTimeSec INTEGER NOT NULL DEFAULT 0,
                        bestMoves INTEGER NOT NULL DEFAULT 0,
                        bestMismatches INTEGER NOT NULL DEFAULT 0,
                        highestScore INTEGER NOT NULL DEFAULT 0,
                        highestStars INTEGER NOT NULL DEFAULT 0,
                        attemptsCount INTEGER NOT NULL DEFAULT 0,
                        lastFlagIds TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "flag_quest_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration() // Simple fallback for design phase
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
