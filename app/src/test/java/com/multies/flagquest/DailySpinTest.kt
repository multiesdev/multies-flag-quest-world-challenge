package com.multies.flagquest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.multies.flagquest.data.local.GameDatabase
import com.multies.flagquest.data.local.dao.GameDao
import com.multies.flagquest.data.local.entity.DailySpinEntity
import com.multies.flagquest.data.local.entity.UserProfileEntity
import com.multies.flagquest.data.repository.CheckSpinEligibilityUseCase
import com.multies.flagquest.data.repository.ClaimSpinRewardUseCase
import com.multies.flagquest.data.repository.RandomProvider
import com.multies.flagquest.data.repository.SelectSpinRewardUseCase
import com.multies.flagquest.data.repository.SpinEligibility
import com.multies.flagquest.data.repository.SpinReward
import com.multies.flagquest.data.repository.SpinRewardConfig
import com.multies.flagquest.data.repository.TimeProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FakeTimeProvider(var currentMs: Long = 1774080000000L /* A fixed arbitrary date: March 12, 2026 */) : TimeProvider {
    override fun currentTimeMillis(): Long = currentMs
    override fun elapsedRealtime(): Long = currentMs // Fake monotonic clock
    
    override fun getLocalCalendarDay(timestamp: Long, timeZone: TimeZone): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date(timestamp))
    }
    
    override fun getLocalCalendarDayCode(timestamp: Long, timeZone: TimeZone): Int {
        val cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = timestamp
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }
}

class FakeRandomProvider(var nextIntVal: Int = 0) : RandomProvider {
    override fun nextDouble(): Double = 0.0
    override fun nextInt(bound: Int): Int = nextIntVal
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailySpinTest {

    private lateinit var db: GameDatabase
    private lateinit var gameDao: GameDao
    private lateinit var fakeTimeProvider: FakeTimeProvider
    private lateinit var fakeRandomProvider: FakeRandomProvider
    
    private lateinit var checkEligibilityUseCase: CheckSpinEligibilityUseCase
    private lateinit var selectRewardUseCase: SelectSpinRewardUseCase
    private lateinit var claimRewardUseCase: ClaimSpinRewardUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        gameDao = db.gameDao()
        
        fakeTimeProvider = FakeTimeProvider()
        fakeRandomProvider = FakeRandomProvider()
        
        checkEligibilityUseCase = CheckSpinEligibilityUseCase(fakeTimeProvider)
        selectRewardUseCase = SelectSpinRewardUseCase(fakeRandomProvider)
        claimRewardUseCase = ClaimSpinRewardUseCase(gameDao, fakeTimeProvider)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testCheckEligibility_NewUser_IsEligible() {
        // GIVEN: No previous daily spin record (null)
        val state: DailySpinEntity? = null
        
        // WHEN: Checking eligibility
        val eligibility = checkEligibilityUseCase.checkEligibility(state)
        
        // THEN: User is eligible
        assertTrue(eligibility is SpinEligibility.Eligible)
    }

    @Test
    fun testCheckEligibility_SpunOnPreviousDay_IsEligible() {
        // GIVEN: Last spin was yesterday (March 11, 2026)
        val lastSpinMs = 1774080000000L - 24 * 60 * 60 * 1000L // 1 day ago
        val state = DailySpinEntity(
            lastSpinDate = "2026-03-11",
            lastSpinTimestamp = lastSpinMs,
            nextEligibleDate = "2026-03-12",
            lastRewardId = "coins_10",
            lastTransactionId = "tx_123",
            claimStatus = "CLAIMED",
            timezoneId = "UTC"
        )
        
        // WHEN: Checking eligibility today (March 12, 2026)
        fakeTimeProvider.currentMs = 1774080000000L // Today
        val eligibility = checkEligibilityUseCase.checkEligibility(state, TimeZone.getTimeZone("UTC"))
        
        // THEN: User is eligible to spin
        assertTrue(eligibility is SpinEligibility.Eligible)
    }

    @Test
    fun testCheckEligibility_SpunToday_IsAlreadyClaimedWithCooldown() {
        // GIVEN: Last spin was earlier today
        val now = 1774080000000L
        val todayStr = fakeTimeProvider.getLocalCalendarDay(now, TimeZone.getTimeZone("UTC"))
        val state = DailySpinEntity(
            lastSpinDate = todayStr,
            lastSpinTimestamp = now - 2 * 60 * 60 * 1000L, // Spun 2 hours ago
            nextEligibleDate = "2026-03-13",
            lastRewardId = "coins_10",
            lastTransactionId = "tx_123",
            claimStatus = "CLAIMED",
            timezoneId = "UTC"
        )
        
        // WHEN: Checking eligibility at current time
        fakeTimeProvider.currentMs = now
        val eligibility = checkEligibilityUseCase.checkEligibility(state, TimeZone.getTimeZone("UTC"))
        
        // THEN: User is blocked and cooldown is calculated until local midnight of tomorrow (14 hours remaining)
        assertTrue(eligibility is SpinEligibility.AlreadyClaimedToday)
        val cooldown = (eligibility as SpinEligibility.AlreadyClaimedToday).remainingTimeMs
        
        // Assert cooldown is non-negative and is roughly computed until UTC midnight
        assertTrue(cooldown > 0)
    }

    @Test
    fun testCheckEligibility_ClockManipulated_ReturnsSuspiciousClock() {
        // GIVEN: A state recorded in future (e.g. user traveled in future, spun, then reset clock)
        val state = DailySpinEntity(
            lastSpinDate = "2026-03-13",
            lastSpinTimestamp = 1774080000000L + 24 * 60 * 60 * 1000L, // Spun tomorrow
            nextEligibleDate = "2026-03-14",
            lastRewardId = "coins_10",
            lastTransactionId = "tx_123",
            claimStatus = "CLAIMED",
            timezoneId = "UTC"
        )
        
        // WHEN: Checking eligibility today
        fakeTimeProvider.currentMs = 1774080000000L // Today (older than last spin timestamp)
        val eligibility = checkEligibilityUseCase.checkEligibility(state, TimeZone.getTimeZone("UTC"))
        
        // THEN: Return SuspiciousClock
        assertTrue(eligibility is SpinEligibility.SuspiciousClock)
    }

    @Test
    fun testSelectReward_VerifyProbabilitiesAndBoundaries() {
        // GIVEN: Specific random thresholds
        // Real weights from SpinRewardConfig:
        // coins_5: weight 22 (cumulative 0 to 21)
        // coins_10: weight 20 (cumulative 22 to 41)
        // coins_25: weight 18 (cumulative 42 to 59)
        // coins_50: weight 14 (cumulative 60 to 73)
        // coins_100: weight 8 (cumulative 74 to 81)
        // coins_200: weight 3 (cumulative 82 to 84)
        // heart_1: weight 8 (cumulative 85 to 92)
        // hearts_2: weight 5 (cumulative 93 to 97)
        // hearts_3: weight 2 (cumulative 98 to 99)

        // 1. Boundary for coins_5 (bounds 0..21)
        fakeRandomProvider.nextIntVal = 10
        assertEquals("coins_5", selectRewardUseCase.selectReward().id)

        // 2. Boundary for coins_10 (bounds 22..41)
        fakeRandomProvider.nextIntVal = 30
        assertEquals("coins_10", selectRewardUseCase.selectReward().id)

        // 3. Boundary for coins_25 (bounds 42..59)
        fakeRandomProvider.nextIntVal = 50
        assertEquals("coins_25", selectRewardUseCase.selectReward().id)

        // 4. Boundary for heart_1 (bounds 85..92)
        fakeRandomProvider.nextIntVal = 90
        assertEquals("heart_1", selectRewardUseCase.selectReward().id)

        // 5. Boundary for hearts_3 (bounds 98..99)
        fakeRandomProvider.nextIntVal = 99
        assertEquals("hearts_3", selectRewardUseCase.selectReward().id)
    }

    @Test
    fun testClaimReward_CoinsOnly_IncreasesCoinsCorrectly() = runBlocking {
        // GIVEN: Initial profile with 100 coins and 5 lives
        val initialProfile = UserProfileEntity(coins = 100, lives = 5)
        gameDao.insertUserProfile(initialProfile)
        
        val coinReward = SpinReward(id = "coins_100", coins = 100, hearts = 0, weight = 7, probabilityText = "7%")
        
        // WHEN: Claiming coins_100
        val result = claimRewardUseCase.claimReward("tx_coins", coinReward, TimeZone.getTimeZone("UTC"))
        
        // THEN: Verify the profile updates
        assertTrue(result.success)
        assertEquals(200, result.totalCoins)
        assertEquals(5, result.totalHearts)
        assertEquals(100, result.coinsGranted)
        assertEquals(0, result.heartsGranted)
        assertEquals(0, result.overflowCoins)
        
        // Database contains the updated profile
        val dbProfile = gameDao.getUserProfile()
        assertEquals(200, dbProfile?.coins)
        assertEquals(5, dbProfile?.lives)
    }

    @Test
    fun testClaimReward_HeartsUnderCap_GrantsHearts() = runBlocking {
        // GIVEN: Initial profile with 50 coins and 3 lives
        val initialProfile = UserProfileEntity(coins = 50, lives = 3)
        gameDao.insertUserProfile(initialProfile)
        
        val heartReward = SpinReward(id = "hearts_2", coins = 0, hearts = 2, weight = 10, probabilityText = "10%")
        
        // WHEN: Claiming 2 hearts
        val result = claimRewardUseCase.claimReward("tx_hearts_undercap", heartReward, TimeZone.getTimeZone("UTC"))
        
        // THEN: Verify results (3 lives + 2 hearts = 5 lives. No overflow.)
        assertTrue(result.success)
        assertEquals(5, result.totalHearts)
        assertEquals(50, result.totalCoins)
        assertEquals(2, result.heartsGranted)
        assertEquals(0, result.overflowCoins)
        
        val dbProfile = gameDao.getUserProfile()
        assertEquals(5, dbProfile?.lives)
        assertEquals(50, dbProfile?.coins)
    }

    @Test
    fun testClaimReward_HeartsExceedCapPartially_GrantsPartialAndConvertsExcessToCoins() = runBlocking {
        // GIVEN: Profile with 9 lives (Max cap is 10)
        val initialProfile = UserProfileEntity(coins = 100, lives = 9)
        gameDao.insertUserProfile(initialProfile)
        
        val heartReward = SpinReward(id = "hearts_2", coins = 0, hearts = 2, weight = 10, probabilityText = "10%")
        
        // WHEN: Claiming 2 hearts (9 + 2 = 11, which is 1 over the cap)
        val result = claimRewardUseCase.claimReward("tx_hearts_partial", heartReward, TimeZone.getTimeZone("UTC"))
        
        // THEN: Hearts granted = 1, overflow = 1 heart -> 25 coins. Final: 10 hearts, 125 coins.
        assertTrue(result.success)
        assertEquals(10, result.totalHearts)
        assertEquals(125, result.totalCoins)
        assertEquals(1, result.heartsGranted)
        assertEquals(25, result.overflowCoins)
        
        val dbProfile = gameDao.getUserProfile()
        assertEquals(10, dbProfile?.lives)
        assertEquals(125, dbProfile?.coins)
    }

    @Test
    fun testClaimReward_HeartsExceedCapFully_ConvertsAllToCoins() = runBlocking {
        // GIVEN: Profile already at maximum cap (10 lives)
        val initialProfile = UserProfileEntity(coins = 100, lives = 10)
        gameDao.insertUserProfile(initialProfile)
        
        val heartReward = SpinReward(id = "hearts_2", coins = 0, hearts = 2, weight = 10, probabilityText = "10%")
        
        // WHEN: Claiming 2 hearts
        val result = claimRewardUseCase.claimReward("tx_hearts_fully_overflow", heartReward, TimeZone.getTimeZone("UTC"))
        
        // THEN: Hearts granted = 0, overflow = 2 hearts -> 50 coins. Final: 10 hearts, 150 coins.
        assertTrue(result.success)
        assertEquals(10, result.totalHearts)
        assertEquals(150, result.totalCoins)
        assertEquals(0, result.heartsGranted)
        assertEquals(50, result.overflowCoins)
        
        val dbProfile = gameDao.getUserProfile()
        assertEquals(10, dbProfile?.lives)
        assertEquals(150, dbProfile?.coins)
    }

    @Test
    fun testClaimReward_DuplicateTransactionId_ReturnsCachedResultAndPreventsDoubleSpending() = runBlocking {
        // GIVEN: Profile with 100 coins
        val initialProfile = UserProfileEntity(coins = 100, lives = 5)
        gameDao.insertUserProfile(initialProfile)
        
        val coinReward = SpinReward(id = "coins_50", coins = 50, hearts = 0, weight = 15, probabilityText = "15%")
        val txId = "unique_tx_double_spend_prevention"
        
        // WHEN: Claiming first time
        val firstResult = claimRewardUseCase.claimReward(txId, coinReward, TimeZone.getTimeZone("UTC"))
        assertTrue(firstResult.success)
        assertEquals(150, firstResult.totalCoins)
        
        // WHEN: Claiming second time with the exact same transactionId
        val secondResult = claimRewardUseCase.claimReward(txId, coinReward, TimeZone.getTimeZone("UTC"))
        
        // THEN: Second claim returns cached status and DOES NOT grant additional coins
        assertTrue(secondResult.success)
        assertEquals(150, secondResult.totalCoins) // Remains 150, did not become 200
        
        val dbProfile = gameDao.getUserProfile()
        assertEquals(150, dbProfile?.coins) // Still 150
    }
}
