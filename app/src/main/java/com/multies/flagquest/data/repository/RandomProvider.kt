package com.multies.flagquest.data.repository

import java.security.SecureRandom
import java.util.Random

interface RandomProvider {
    fun nextDouble(): Double
    fun nextInt(bound: Int): Int
}

class SecureRandomProvider : RandomProvider {
    private val secureRandom = SecureRandom()
    override fun nextDouble(): Double = secureRandom.nextDouble()
    override fun nextInt(bound: Int): Int = secureRandom.nextInt(bound)
}

class SeededRandomProvider(seed: Long) : RandomProvider {
    private val random = Random(seed)
    override fun nextDouble(): Double = random.nextDouble()
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}
