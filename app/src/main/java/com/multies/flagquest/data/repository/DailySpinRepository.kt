package com.multies.flagquest.data.repository

import com.multies.flagquest.data.local.dao.GameDao
import com.multies.flagquest.data.local.entity.DailySpinEntity
import com.multies.flagquest.data.local.entity.SpinRewardHistoryEntity
import com.multies.flagquest.data.local.entity.SpinTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.TimeZone

class DailySpinRepository(
    private val gameDao: GameDao,
    val timeProvider: TimeProvider = DefaultTimeProvider(),
    val randomProvider: RandomProvider = SecureRandomProvider()
) {
    private val checkSpinEligibilityUseCase = CheckSpinEligibilityUseCase(timeProvider)
    private val selectSpinRewardUseCase = SelectSpinRewardUseCase(randomProvider)
    private val claimSpinRewardUseCase = ClaimSpinRewardUseCase(gameDao, timeProvider)
    
    fun getDailySpinFlow(): Flow<DailySpinEntity?> = gameDao.getDailySpinFlow()
    
    suspend fun getDailySpin(): DailySpinEntity? = gameDao.getDailySpin()
    
    fun getRewardHistoryFlow(): Flow<List<SpinRewardHistoryEntity>> = gameDao.getRewardHistoryFlow()
    
    suspend fun getLastRewardHistory(): SpinRewardHistoryEntity? = gameDao.getLastRewardHistory()
    
    fun checkEligibility(state: DailySpinEntity?, timeZone: TimeZone = TimeZone.getDefault()): SpinEligibility {
        return checkSpinEligibilityUseCase.checkEligibility(state, timeZone)
    }
    
    fun selectReward(): SpinReward {
        return selectSpinRewardUseCase.selectReward()
    }
    
    fun selectSpecificReward(rewardId: String): SpinReward {
        return selectSpinRewardUseCase.selectSpecificReward(rewardId)
    }
    
    suspend fun claimReward(
        transactionId: String,
        reward: SpinReward,
        timeZone: TimeZone = TimeZone.getDefault()
    ): ClaimResult {
        return claimSpinRewardUseCase.claimReward(transactionId, reward, timeZone)
    }

    suspend fun insertDailySpin(state: DailySpinEntity) {
        gameDao.insertDailySpin(state)
    }
}
