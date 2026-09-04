package com.multies.flagquest.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_preferences")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_MUSIC = booleanPreferencesKey("music")
        val KEY_SOUND = booleanPreferencesKey("sound")
        val KEY_VIBRATION = booleanPreferencesKey("vibration")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val KEY_REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        
        // Customization and Unlockable Themes
        val KEY_SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val KEY_PURCHASED_THEME_IDS = stringPreferencesKey("purchased_theme_ids")
        
        val KEY_SELECTED_BACKGROUND = stringPreferencesKey("selected_background")
        val KEY_PURCHASED_BACKGROUNDS = stringPreferencesKey("purchased_backgrounds")
        
        val KEY_SELECTED_ANSWER_CARD = stringPreferencesKey("selected_answer_card")
        val KEY_PURCHASED_ANSWER_CARDS = stringPreferencesKey("purchased_answer_cards")
        
        val KEY_SELECTED_BUTTON_STYLE = stringPreferencesKey("selected_button_style")
        val KEY_PURCHASED_BUTTON_STYLES = stringPreferencesKey("purchased_button_styles")
        
        val KEY_SELECTED_PROFILE_FRAME = stringPreferencesKey("selected_profile_frame")
        val KEY_PURCHASED_PROFILE_FRAMES = stringPreferencesKey("purchased_profile_frames")
        
        val KEY_SELECTED_PASSPORT_COVER = stringPreferencesKey("selected_passport_cover")
        val KEY_PURCHASED_PASSPORT_COVERS = stringPreferencesKey("purchased_passport_covers")
        
        val KEY_SELECTED_CELEBRATION_EFFECT = stringPreferencesKey("selected_celebration_effect")
        val KEY_PURCHASED_CELEBRATION_EFFECTS = stringPreferencesKey("purchased_celebration_effects")

        // Accessibility Extended Timer
        val KEY_TIMER_MODE = stringPreferencesKey("timer_mode")

        // Volume Controls
        val KEY_MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val KEY_SOUND_VOLUME = floatPreferencesKey("sound_volume")
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: getDeviceDefaultLanguage()
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        val raw = preferences[KEY_THEME]?.lowercase(Locale.ROOT) ?: "system"
        val value = when (raw) {
            "light", "dark", "amoled", "system" -> raw
            else -> "system" // Safe migration of invalid or legacy values
        }
        if (com.multies.flagquest.BuildConfig.DEBUG) {
            android.util.Log.d("ThemeDebug", "SettingsDataStore read themeFlow: $value")
        }
        value
    }

    val musicFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUSIC] ?: true
    }

    val soundFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SOUND] ?: true
    }

    val vibrationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_VIBRATION] ?: true
    }

    val highContrastFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HIGH_CONTRAST] ?: false
    }

    val reducedMotionFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_REDUCED_MOTION] ?: false
    }

    // New flows for customization, themes, volumes, and timer
    val selectedThemeIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_THEME_ID] ?: "vibrant_world"
    }

    val purchasedThemeIdsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_THEME_IDS] ?: "vibrant_world,light,dark"
    }

    val selectedBackgroundFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_BACKGROUND] ?: "default"
    }

    val purchasedBackgroundsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_BACKGROUNDS] ?: "default"
    }

    val selectedAnswerCardFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_ANSWER_CARD] ?: "default"
    }

    val purchasedAnswerCardsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_ANSWER_CARDS] ?: "default"
    }

    val selectedButtonStyleFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_BUTTON_STYLE] ?: "default"
    }

    val purchasedButtonStylesFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_BUTTON_STYLES] ?: "default"
    }

    val selectedProfileFrameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_PROFILE_FRAME] ?: "default"
    }

    val purchasedProfileFramesFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_PROFILE_FRAMES] ?: "default"
    }

    val selectedPassportCoverFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_PASSPORT_COVER] ?: "default"
    }

    val purchasedPassportCoversFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_PASSPORT_COVERS] ?: "default"
    }

    val selectedCelebrationEffectFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_CELEBRATION_EFFECT] ?: "default"
    }

    val purchasedCelebrationEffectsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PURCHASED_CELEBRATION_EFFECTS] ?: "default"
    }

    val timerModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_TIMER_MODE] ?: "default"
    }

    val musicVolumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_MUSIC_VOLUME] ?: 0.3f
    }

    val soundVolumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_SOUND_VOLUME] ?: 0.5f
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = lang
        }
    }

    suspend fun saveTheme(theme: String) {
        val normalized = theme.lowercase(Locale.ROOT)
        if (com.multies.flagquest.BuildConfig.DEBUG) {
            android.util.Log.d("ThemeDebug", "SettingsDataStore saveTheme: $normalized")
        }
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = normalized
        }
    }

    suspend fun saveMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MUSIC] = enabled
        }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SOUND] = enabled
        }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIBRATION] = enabled
        }
    }

    suspend fun saveHighContrastEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIGH_CONTRAST] = enabled
        }
    }

    suspend fun saveReducedMotionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REDUCED_MOTION] = enabled
        }
    }

    // Customization save methods
    suspend fun saveSelectedThemeId(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_THEME_ID] = themeId
        }
    }

    suspend fun savePurchasedThemeIds(purchasedIds: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_THEME_IDS] = purchasedIds
        }
    }

    suspend fun saveSelectedBackground(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_BACKGROUND] = style
        }
    }

    suspend fun savePurchasedBackgrounds(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_BACKGROUNDS] = purchased
        }
    }

    suspend fun saveSelectedAnswerCard(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_ANSWER_CARD] = style
        }
    }

    suspend fun savePurchasedAnswerCards(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_ANSWER_CARDS] = purchased
        }
    }

    suspend fun saveSelectedButtonStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_BUTTON_STYLE] = style
        }
    }

    suspend fun savePurchasedButtonStyles(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_BUTTON_STYLES] = purchased
        }
    }

    suspend fun saveSelectedProfileFrame(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_PROFILE_FRAME] = style
        }
    }

    suspend fun savePurchasedProfileFrames(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_PROFILE_FRAMES] = purchased
        }
    }

    suspend fun saveSelectedPassportCover(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_PASSPORT_COVER] = style
        }
    }

    suspend fun savePurchasedPassportCovers(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_PASSPORT_COVERS] = purchased
        }
    }

    suspend fun saveSelectedCelebrationEffect(style: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_CELEBRATION_EFFECT] = style
        }
    }

    suspend fun savePurchasedCelebrationEffects(purchased: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PURCHASED_CELEBRATION_EFFECTS] = purchased
        }
    }

    suspend fun saveTimerMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TIMER_MODE] = mode
        }
    }

    suspend fun saveMusicVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MUSIC_VOLUME] = volume
        }
    }

    suspend fun saveSoundVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SOUND_VOLUME] = volume
        }
    }

    private fun getDeviceDefaultLanguage(): String {
        val defaultLang = Locale.getDefault().language
        return if (defaultLang in listOf("en", "ar", "de", "fr")) defaultLang else "en"
    }
}
