package com.multies.flagquest.data.repository

class SelectSpinRewardUseCase(private val randomProvider: RandomProvider) {
    
    /**
     * Selects a reward based on the configured probabilities.
     * randomProvider is injected, which can be secure in production or seeded in tests.
     */
    fun selectReward(): SpinReward {
        val value = randomProvider.nextInt(100) // 0 to 99
        var cumulativeWeight = 0
        for (reward in SpinRewardConfig.rewards) {
            cumulativeWeight += reward.weight
            if (value < cumulativeWeight) {
                return reward
            }
        }
        return SpinRewardConfig.rewards.first() // Fallback
    }

    /**
     * Selects a specific reward by ID for deterministic testing/scenarios.
     */
    fun selectSpecificReward(rewardId: String): SpinReward {
        return SpinRewardConfig.rewards.find { it.id == rewardId } 
            ?: SpinRewardConfig.rewards.first()
    }
}
