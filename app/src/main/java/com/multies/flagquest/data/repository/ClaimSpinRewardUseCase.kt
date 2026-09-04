package com.multies.flagquest.data.repository

import com.multies.flagquest.data.local.dao.GameDao
import com.multies.flagquest.data.local.entity.DailySpinEntity
import com.multies.flagquest.data.local.entity.SpinRewardHistoryEntity
import com.multies.flagquest.data.local.entity.SpinTransactionEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import java.util.TimeZone

data class ClaimResult(
    val success: Boolean,
    val rewardId: String,
    val coinsGranted: Int,
    val heartsGranted: Int,
    val overflowCoins: Int,
    val totalCoins: Int,
    val totalHearts: Int,
    val transactionId: String,
    val error: String? = null
)

class ClaimSpinRewardUseCase(
    private val gameDao: GameDao,
    private val timeProvider: TimeProvider
) {
    
    suspend fun claimReward(
        transactionId: String,
        reward: SpinReward,
        timeZone: TimeZone = TimeZone.getDefault()
    ): ClaimResult {
        // Run database queries on background thread. Since Room operations are suspending,
        // we can fetch, calculate and write sequentially.
        val profile = gameDao.getUserProfile() ?: UserProfileEntity()
        val existingTx = gameDao.getTransaction(transactionId)
        
        if (existingTx != null && existingTx.status == "COMMITTED") {
            // Already claimed/committed, return cached details
            return ClaimResult(
                success = true,
                rewardId = existingTx.rewardId,
                coinsGranted = existingTx.coinsGranted,
                heartsGranted = existingTx.heartsGranted,
                overflowCoins = existingTx.overflowCoinsGranted,
                totalCoins = profile.coins,
                totalHearts = profile.lives,
                transactionId = transactionId,
                error = "Transaction already committed"
            )
        }
        
        val now = timeProvider.currentTimeMillis()
        val todayStr = timeProvider.getLocalCalendarDay(now, timeZone)
        
        // Calculate the rewards
        var coinsToGrant = reward.coins
        var heartsToGrant = 0
        var overflowCoins = 0
        
        if (reward.hearts > 0) {
            val currentLives = profile.lives
            val cap = 10
            if (currentLives + reward.hearts <= cap) {
                heartsToGrant = reward.hearts
            } else {
                val addedHearts = (cap - currentLives).coerceAtLeast(0)
                heartsToGrant = addedHearts
                val excessHearts = reward.hearts - addedHearts
                overflowCoins = excessHearts * 25
            }
        }
        
        val finalCoins = profile.coins + coinsToGrant + overflowCoins
        val finalLives = profile.lives + heartsToGrant
        
        // Stop automatic regeneration if hearts >= 5
        val nextRegen = if (finalLives >= 5) {
            0L
        } else {
            profile.nextHeartRegenTimestamp
        }
        
        // Prepare updated UserProfileEntity
        val updatedProfile = profile.copy(
            coins = finalCoins,
            lives = finalLives,
            nextHeartRegenTimestamp = nextRegen,
            lastSpinTimestamp = now // Also update legacy lastSpinTimestamp field to keep in sync
        )
        
        // Prepare DailySpinEntity
        val dailySpinState = gameDao.getDailySpin() ?: DailySpinEntity()
        val updatedDailySpin = dailySpinState.copy(
            lastSpinDate = todayStr,
            lastSpinTimestamp = now,
            nextEligibleDate = timeProvider.getLocalCalendarDay(now + 24 * 60 * 60 * 1000L, timeZone),
            lastRewardId = reward.id,
            lastTransactionId = transactionId,
            claimStatus = "CLAIMED",
            timezoneId = timeZone.id
        )
        
        // Prepare SpinTransactionEntity
        val spinTx = SpinTransactionEntity(
            transactionId = transactionId,
            timestamp = now,
            rewardId = reward.id,
            status = "COMMITTED",
            dateStr = todayStr,
            coinsGranted = coinsToGrant,
            heartsGranted = heartsToGrant,
            overflowCoinsGranted = overflowCoins
        )
        
        // Prepare SpinRewardHistoryEntity
        val history = SpinRewardHistoryEntity(
            transactionId = transactionId,
            rewardId = reward.id,
            timestamp = now,
            dateStr = todayStr,
            coinsWon = coinsToGrant,
            heartsWon = heartsToGrant,
            overflowCoins = overflowCoins
        )
        
        // Commit atomically to DB
        try {
            gameDao.updateUserProfile(updatedProfile)
            gameDao.insertDailySpin(updatedDailySpin)
            gameDao.insertTransaction(spinTx)
            gameDao.insertRewardHistory(history)
            
            return ClaimResult(
                success = true,
                rewardId = reward.id,
                coinsGranted = coinsToGrant,
                heartsGranted = heartsToGrant,
                overflowCoins = overflowCoins,
                totalCoins = finalCoins,
                totalHearts = finalLives,
                transactionId = transactionId
            )
        } catch (e: Exception) {
            return ClaimResult(
                success = false,
                rewardId = reward.id,
                coinsGranted = 0,
                heartsGranted = 0,
                overflowCoins = 0,
                totalCoins = profile.coins,
                totalHearts = profile.lives,
                transactionId = transactionId,
                error = e.localizedMessage ?: "Database error"
            )
        }
    }
}
