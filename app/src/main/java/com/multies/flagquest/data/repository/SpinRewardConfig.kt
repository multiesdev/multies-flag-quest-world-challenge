package com.multies.flagquest.data.repository

data class SpinReward(
    val id: String,
    val coins: Int,
    val hearts: Int,
    val weight: Int, // percentage out of 100
    val probabilityText: String
)

object SpinRewardConfig {
    val rewards = listOf(
        SpinReward("coins_5", coins = 5, hearts = 0, weight = 22, "22%"),
        SpinReward("coins_10", coins = 10, hearts = 0, weight = 20, "20%"),
        SpinReward("coins_25", coins = 25, hearts = 0, weight = 18, "18%"),
        SpinReward("coins_50", coins = 50, hearts = 0, weight = 14, "14%"),
        SpinReward("coins_100", coins = 100, hearts = 0, weight = 8, "8%"),
        SpinReward("coins_200", coins = 200, hearts = 0, weight = 3, "3%"),
        SpinReward("heart_1", coins = 0, hearts = 1, weight = 8, "8%"),
        SpinReward("hearts_2", coins = 0, hearts = 2, weight = 5, "5%"),
        SpinReward("hearts_3", coins = 0, hearts = 3, weight = 2, "2%")
    )

    fun validate(): Boolean {
        return rewards.sumOf { it.weight } == 100
    }
}
