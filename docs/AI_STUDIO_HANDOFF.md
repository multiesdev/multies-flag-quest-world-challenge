# AI Studio Project Migration & Handoff Document

**Project:** Atlas Masters: World Quiz  
**Target Migration:** Google AI Studio Account Migration  
**Document Generation Date:** September 2026  
**Document Version:** 1.0.0  

---

## 1. Project Identification & Metadata

| Property | Value | Source of Truth |
| :--- | :--- | :--- |
| **Platform Display Name** | `Atlas Masters: World Quiz` | `metadata.json` |
| **Android Launcher Label** | `Atlas Masters: World Quiz` | `app/src/main/res/values/strings.xml` (`@string/app_name`) |
| **Gradle Root Project Name**| `Atlas Masters World Quiz` | `settings.gradle.kts` (`rootProject.name`) |
| **Android Application ID** | `com.multies.flagquest` | `app/build.gradle.kts` (`defaultConfig.applicationId`) |
| **Package / Namespace** | `com.multies.flagquest` | `app/build.gradle.kts` (`android.namespace`) |
| **Version Code** | `1` | `app/build.gradle.kts` (`versionCode`) |
| **Version Name** | `1.0` | `app/build.gradle.kts` (`versionName`) |
| **Compile SDK** | `36` (minor `1`) | `app/build.gradle.kts` |
| **Target SDK** | `36` | `app/build.gradle.kts` |
| **Min SDK** | `24` (Android 7.0 Nougat) | `app/build.gradle.kts` |
| **Target Java Version** | `Java 11` | `compileOptions.sourceCompatibility` |

---

## 2. Architecture & Technology Stack

The application follows **Clean Architecture** principles and **MVVM (Model-View-ViewModel)** with an entirely reactive, unidirectional data flow (UDF).

```
┌─────────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                        │
│ (Screens, Components, Themes, Dynamic Layout Direction RTL) │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│                        ViewModels                           │
│  - GameViewModel (Core Quiz, Profile, Store, Stats)         │
│  - WhoAmIViewModel (Dedicated Clue Deduction Engine VM)     │
│  - ContinentBossViewModel (Multi-phase Gauntlet VM)         │
└──────────────────────────────┬──────────────────────────────┘
                               │ Coroutines / Repository API
┌──────────────────────────────▼──────────────────────────────┐
│                    Repositories & Use Cases                 │
│  - GameRepository        - DailySpinRepository              │
│  - ContinentBossRepo     - CountryMapRepository             │
│  - CheckSpinEligibility  - ClaimSpinRewardUseCase           │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐
│        Room Database        │ │    Assets & DataStore       │
│  (SQLite v6, 11 Entities,   │ │  - countries.json (195 UN)  │
│   5 Sequential Migrations)  │ │  - 108 Country SVG maps     │
│  - UserProfile, Missions,   │ │  - 142 Authentic/Fake Flags │
│    Spins, Memory, Stats     │ │  - SettingsDataStore (Prefs)│
└─────────────────────────────┘ └─────────────────────────────┘
```

### Core Technologies
- **UI Framework:** 100% Jetpack Compose with Material Design 3 (M3).
- **Navigation:** Navigation Compose (`2.8.9`) via typed sealed `Screen` routes.
- **Asynchronous & Reactive:** Kotlin Coroutines (`1.10.2`) and `StateFlow` / `SharedFlow`.
- **Local Persistence (Database):** Android Room (`2.7.0`) with KSP compiler, SQLite schema version `6`, supporting 11 entities and 5 robust incremental migrations (`MIGRATION_1_2` through `MIGRATION_5_6`).
- **Preferences:** Jetpack DataStore Preferences (`1.1.7`) for instant user setting serialization (language, audio, contrast, motion).
- **JSON Serialization:** Moshi (`1.15.2`) with Kotlin reflection and codegen.
- **Image & Vector Rendering:** Coil Compose (`2.7.0`) with Coil SVG decoder for high-fidelity flag and country outline rendering.
- **Audio & Haptics:** Native Android `SoundPool` / `MediaPlayer` (`AudioManager.kt`) and `VibrationEffect` (`HapticHelper.kt`).
- **Testing:** Robolectric (`4.16.1`), JUnit 4, Kotlinx Coroutines Test (`1.10.2`), and Roborazzi (`1.59.0`).

---

## 3. Completed Features & Phases

### Phase 1: Core Geography Quiz Engine
- **Standard Quiz Categories:** Flags & Continents, Capitals, Population, Currencies, International Organizations, and Mixed Challenge.
- **Feedback & Mechanics:** Immediate answer validation, informative educational "Did You Know?" fact dialogs, streak counters, and score multipliers.
- **Offline Dataset:** Complete curated dataset of 195 sovereign nations (193 UN members + 2 UN observers) fully localized in English, Arabic, German, and French.

### Phase 2: Interactive World Atlas
- **Sovereign Nation Catalog:** 195 flip-card country views displaying flag, capital, population, geographic area, currency, languages, and fun facts.
- **Discovery Progress:** Countries unlock permanently in the Atlas upon correctly answering quiz questions.
- **Search & Filter:** Search bar by country name, filter by Discovered, Favorites (`isFavorite`), and Continents.
- **Territory Neutrality:** Educational policy documentation reflecting UN statistics and ISO-3166-1 guidelines.

### Phase 3: Special Challenge Modes
- **Time Blitz Challenge:** 30-second rapid-fire flag identification testing reflex speed.
- **Sudden Death Survival:** Single-life elimination mode challenging players to reach a 20-country streak.
- **Capital City Marathon:** 15-question consecutive national capital gauntlet.

### Phase 4: Gamified Mini-Game Modules
1. **Flag Memory Game:** Grid-based memory match game with preview countdown, move counter, mismatch tracker, star ratings, and local record persistence (`MemoryLevelEntity`).
2. **Silent Map Challenge:** Interactive SVG country outline identification with pan/zoom canvas and 108 authentic SVG geographic borders.
3. **Who Am I? Country Deduction:** 5 progressive clue stages with point decay (100 down to 20 pts), hint reveal mechanics, and level progression.
4. **Spot the Fake Flag:** Vexillological inspection mode with authentic vs. altered SVG/PNG flags and detailed vexillological explanations.
5. **Country Ranking Challenge:** Interactive drag/reorder challenge sorting countries by Area, Population, Elevation, Coastline Length, Equator Distance, Latitude, and Longitude.
6. **Quick Geography:** 60-second blitz testing true/false and rapid multiple-choice trivia with escalating streak multipliers.
7. **Continent Boss Gauntlet:** Multi-phase epic continent boss battle spanning 6 stages (Flag Mastery, Map Mastery, Capital Mastery, Knowledge, Borders, and Speed Finale).

### Phase 5: Retention, Economy & Rewards
- **8-Spoke Lucky Wheel (Daily Spin):** Verifiable probability distribution, cooldown timer, anti-tampering clock validation, and persistent transaction logging (`DailySpinEntity`, `SpinTransactionEntity`, `SpinRewardHistoryEntity`).
- **7-Day Daily Gift Calendar:** Consecutive streak rewards with protected progress.
- **Missions Engine:** Daily quests and Weekly Grand Missions tracked in Room (`MissionEntity`).
- **Lives & Economy:** Hearts regeneration timer, 5-heart maximum with coin refill option, and XP/Level progression.
- **Smart Review:** Spaced-repetition practice mode targeting questions missed in prior sessions (`MissedQuestionEntity`).

### Phase 6: Customization, Theming & Accessibility
- **9 Distinct M3 Themes:** Vibrant World (System Default), Minimalist Light, Classic Dark, Space Odyssey, Ancient Map, Cyber Neon, Ocean Deep, Sahara Sands, and Aurora Borealis.
- **AMOLED Mode:** Pure pitch-black canvas (`#000000`) for high power efficiency on OLED screens.
- **Accessibility Suite:** High-Contrast Mode, Reduced Motion toggle (disables decorative animations), touch targets $\ge 48\text{dp}$, and complete TalkBack semantics.
- **Internationalization (i18n):** 100% string parity across English (`en`), Arabic (`ar`), German (`de`), and French (`fr`).
- **Full Dynamic RTL:** Arabic layout direction dynamically mirrors all Compose navigation and UI trees (`LayoutDirection.Rtl`) independently of device OS language.

---

## 4. Frozen Product, UX & Architectural Decisions

1. **100% Offline-First Architecture:**
   - The app runs entirely offline without requiring external backend servers, cloud databases, or third-party user accounts.
   - All assets (flags, SVG maps, country data) are packaged in `app/src/main/assets/`.
2. **Non-Predatory Economy:**
   - Coins, hints, and lives are earned exclusively through gameplay, daily spins, and missions.
   - No pay-to-win mechanics or predatory dark patterns.
   - Hearts regenerate automatically; Classroom and Relaxed modes remove life penalties entirely.
3. **Database Migration Integrity:**
   - `GameDatabase` must strictly maintain historical migrations (`MIGRATION_1_2` through `MIGRATION_5_6`).
   - Destructive migration is used only as an emergency safety fallback.
4. **Single-Activity Architecture:**
   - `MainActivity.kt` acts as the single entry point, hosting a unified Compose `NavHost`.
5. **Localization Parity Rule:**
   - Every user-facing string added to `strings.xml` must have corresponding entries in `values-ar`, `values-de`, `values-fr`, and `Locales.kt`.
   - Any discrepancy will trigger a test failure in `LocalizationParityTest`.

---

## 5. Incomplete & Planned Features

1. **Modular ViewModel Extraction (In Progress):**
   - Completed: `WhoAmIViewModel` and `ContinentBossViewModel` have been extracted into standalone feature viewmodels.
   - Pending Extraction from `GameViewModel`:
     - `SpotTheFakeViewModel`
     - `CountryRankingViewModel`
     - `QuickGeographyViewModel`
     - `SilentMapViewModel`
     - `FlagMemoryViewModel`
2. **Additional Country Outlines:**
   - 108 country border SVGs are currently present in `assets/maps/countries/`.
   - The remaining smaller island nations and microstates can be expanded with dedicated SVG outlines.
3. **Local Pass-and-Play Multiplayer:**
   - Turn-based 2-player local device challenge mode designed in concept, pending implementation.
4. **Cloud Backup / Synchronization (Optional):**
   - Firebase BOM and App Check libraries exist in `build.gradle.kts` dependencies, but Firestore and Firebase Auth remain commented out to preserve offline-first operation.

---

## 6. Exact Last Completed Development Step

**Step Completed: Step 2B / Step 2B-T (WhoAmIViewModel Extraction & Verification)**
- Extracted `WhoAmIViewModel.kt` into `com.multies.flagquest.features.whoami` package.
- Migrated state handling, timer jobs, clue revealing, feedback timers, score decay, and victory recording.
- Re-architected coin persistence logic to guarantee exactly-once coin credits upon level completion.
- Developed comprehensive unit test suite in `WhoAmIViewModelTest.kt` covering all game transitions and persistence guarantees.
- Executed verification:
  - `gradle :app:testDebugUnitTest --tests "com.multies.flagquest.WhoAmI*"` -> **PASSED**
  - Full test suite `gradle :app:testDebugUnitTest` -> **83/90 PASSED** (with 7 known pre-existing `ThemeRegressionTest` failures in the established baseline)
  - `gradle :app:compileDebugKotlin` -> **PASSED**
  - `gradle :app:assembleDebug` -> **PASSED**

---

## 7. Recommended Next Development Step

**Step: Step 2C — SpotTheFakeViewModel Extraction**
- **Objective:** Extract the game state and logic from `SpotTheFakeGameScreen.kt` and `GameViewModel.kt` into a standalone `SpotTheFakeViewModel` under `com.multies.flagquest.features.spotthefake`.
- **Key Tasks:**
  1. Define `SpotTheFakeUiState` encapsulating round questions, timer, selected options, reveal explanations, score, and level progression.
  2. Implement `SpotTheFakeViewModel` with dependency injection of `GameRepository`.
  3. Decouple `SpotTheFakeGameScreen.kt` to observe `SpotTheFakeViewModel.uiState`.
  4. Write `SpotTheFakeViewModelTest.kt` verifying question generation, level completion, coin rewards, and error feedback.
  5. Run `gradle :app:testDebugUnitTest` to verify test suite health.

---

## 8. Known Limitations & Technical Debt

1. **`GameViewModel` File Size:**
   - `GameViewModel.kt` contains ~1,400 lines of code. It currently retains shared logic for profile, stats, store, daily rewards, and several minigame modes that are slated for progressive extraction.
2. **Pre-Existing Baseline Failures in `ThemeRegressionTest`:**
   - The established pre-Step-2B test baseline contains 7 known test failures in `ThemeRegressionTest.kt` (`test1`, `test3`, `test5`, `test8`, `test9`, `test10`, `test12`) due to Robolectric test-environment timing between `GameViewModel.updateTheme` and `SettingsDataStore` coroutine dispatchers. These failures are isolated to `ThemeRegressionTest` and do not affect runtime theme switching or other test suites.
3. **Silent Map Fallbacks:**
   - When a country outline SVG is unavailable, `SilentMapRenderer.kt` gracefully displays a continent-level outline placeholder.
4. **Roborazzi Screenshot Tests:**
   - Roborazzi tasks (`finalizeTestRoborazziDebug`) are skipped during standard unit tests and must be explicitly recorded via `gradle :app:recordRoborazziDebug`.
5. **Google Services Warning:**
   - `MissingGoogleServicesStrategy.WARN` is configured in `build.gradle.kts`. Without a `google-services.json` file in `/app`, Gradle logs a harmless build warning.

---

## 9. Directory Structure & Key Files

```
/
├── .env.example                               # Environment template (GEMINI_API_KEY)
├── .gitignore                                 # Git exclusions (.env, local.properties, build, etc.)
├── build.gradle.kts                           # Root Gradle script
├── debug.keystore.base64                      # Base64 encoded debug keystore
├── generate_countries.py                      # Offline countries generator script (195 UN nations)
├── generate_flags.py                          # SVG flag generator (authentic & fake SVGs)
├── metadata.json                              # AI Studio platform identification
├── settings.gradle.kts                        # Gradle settings & plugin repositories
├── docs/
│   └── AI_STUDIO_HANDOFF.md                   # This handoff document
└── app/
    ├── build.gradle.kts                       # Application build config & dependencies
    ├── proguard-rules.pro                     # Proguard rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml            # Manifest with RTL support & permissions
        │   ├── assets/
        │   │   ├── countries.json             # Comprehensive 195 country dataset
        │   │   ├── organizations.json         # International organizations dataset
        │   │   ├── questions.json             # Core trivia questions
        │   │   ├── country_ranking_data.json  # Ranking dataset (area, population, elevation)
        │   │   ├── flags/
        │   │   │   ├── authentic/             # 71 authentic flag assets (SVG/PNG)
        │   │   │   └── fake/                  # 71 altered fake flag assets (SVG/PNG)
        │   │   └── maps/countries/            # 108 country silhouette SVG vector maps
        │   ├── java/com/multies/flagquest/
        │   │   ├── MainActivity.kt            # Single Activity, theme & RTL provider
        │   │   ├── audio/                     # AudioManager and HapticHelper
        │   │   ├── data/
        │   │   │   ├── local/                 # GameDatabase, SettingsDataStore
        │   │   │   │   ├── dao/GameDao.kt     # Room DAO with queries & transactions
        │   │   │   │   └── entity/            # 11 Room entity definitions
        │   │   │   ├── model/                 # Game models and algorithmic engines
        │   │   │   └── repository/            # Repositories & use cases
        │   │   ├── features/
        │   │   │   └── whoami/                # WhoAmIViewModel & Clue deduction engine
        │   │   └── ui/
        │   │       ├── components/            # Reusable UI components & GameHeader
        │   │       ├── localization/Locales.kt# 4-language translation catalog
        │   │       ├── navigation/Screen.kt   # Typed navigation route definitions
        │   │       ├── screens/               # 20+ Compose screen composables
        │   │       ├── theme/                 # 9 M3 themes, AMOLED, Color, Type
        │   │       └── viewmodel/             # GameViewModel, ContinentBossViewModel
        │   └── res/
        │       ├── values/                    # Base English strings.xml, colors, themes
        │       ├── values-ar/strings.xml      # Arabic translations
        │       ├── values-de/strings.xml      # German translations
        │       └── values-fr/strings.xml      # French translations
        └── test/java/com/multies/flagquest/   # 14 unit & Robolectric test classes (90 tests)
```

---

## 10. Build Tools & Dependency Versions

| Component | Version | Reference |
| :--- | :--- | :--- |
| **Android Gradle Plugin (AGP)** | `9.1.1` | `libs.versions.toml` (`agp`) |
| **Gradle** | `9.3.1` | Container toolchain |
| **Kotlin** | `2.2.10` | `libs.versions.toml` (`kotlin`) |
| **Kotlin Symbol Processing (KSP)** | `2.3.5` | `libs.versions.toml` (`googleDevtoolsKsp`) |
| **Compose BOM** | `2024.09.00` | `libs.versions.toml` (`composeBom`) |
| **Compose Material 3** | BOM Managed | `androidx.compose.material3` |
| **Navigation Compose** | `2.8.9` | `libs.versions.toml` (`navigationCompose`) |
| **AndroidX Lifecycle** | `2.8.7` | `libs.versions.toml` (`lifecycleRuntimeKtx`) |
| **Room Runtime & Compiler** | `2.7.0` | `libs.versions.toml` (`roomRuntime`) |
| **DataStore Preferences** | `1.1.7` | `libs.versions.toml` (`datastorePreferences`) |
| **Moshi** | `1.15.2` | `libs.versions.toml` (`moshiKotlin`) |
| **Coil (Compose & SVG)** | `2.7.0` | `libs.versions.toml` (`coilCompose`) |
| **Retrofit** | `2.12.0` | `libs.versions.toml` (`retrofit`) |
| **OkHttp** | `4.10.0` | `libs.versions.toml` (`okhttp`) |
| **Firebase BOM** | `34.15.0` | `libs.versions.toml` (`firebaseBom`) |
| **Robolectric** | `4.16.1` | `libs.versions.toml` (`robolectric`) |
| **Roborazzi** | `1.59.0` | `libs.versions.toml` (`roborazzi`) |
| **Secrets Gradle Plugin** | `2.0.1` | `libs.versions.toml` (`secretsGradlePlugin`) |

---

## 11. Internationalization, RTL & Accessibility

### Languages Supported
1. **English (`en`)**: Default fallback locale.
2. **Arabic (`ar`)**: Full right-to-left layout direction, mirror navigation transitions, Arabic numerals and localized country/capital names.
3. **German (`de`)**: Localized terms, compounds, and country metadata.
4. **French (`fr`)**: Localized terms and vexillological explanations.

### RTL Implementation Invariant
In `MainActivity.kt`:
```kotlin
val layoutDirection = if (selectedLanguage == "ar") {
    LayoutDirection.Rtl
} else {
    LayoutDirection.Ltr
}

CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    FlagQuestTheme(...) { ... }
}
```
Directional navigation icons use `Icons.AutoMirrored` to automatically flip according to the selected layout direction.

### Accessibility Standards
- Minimum interactive component touch targets $\ge 48\text{dp} \times 48\text{dp}$.
- Meaningful `contentDescription` on all icons and cards.
- Support for system font scaling with dynamic `sp` units.
- User-selectable High-Contrast color palette.
- User-selectable Reduced Motion preference.

---

## 12. Verification & Test Suite Status

The codebase contains **14 test classes** comprising **90 individual tests**:

| Test Class | Purpose | Status |
| :--- | :--- | :---: |
| `ContinentBossEngineTest` | Multi-phase boss gauntlet stages and damage formulas | **PASSED** |
| `CountryRankingEngineTest` | Sorting criteria validation (area, pop, coords) | **PASSED** |
| `DailySpinTest` | RNG probabilities, cooldown, transaction persistence | **PASSED** |
| `ExampleRobolectricTest` | Robolectric activity launch and context | **PASSED** |
| `ExampleUnitTest` | Baseline test sanity check | **PASSED** |
| `GameViewModelTest` | Core quiz, streak logic, heart regeneration, level unlocking | **PASSED** |
| `LocalizationParityTest` | 100% key parity across EN, AR, DE, FR XML resources | **PASSED** |
| `QuickGeographyEngineTest` | 60-second blitz generator, multipliers, time bonus | **PASSED** |
| `SilentMapTest` | Map SVG loader and silhouette existence | **PASSED** |
| `SpotTheFakeEngineTest` | Level generation, authentic vs. fake flag options | **PASSED** |
| `SpotTheFakeFlagRegressionTest` | Verifies all referenced flag SVGs/PNGs exist in assets | **PASSED** |
| `ThemeRegressionTest` | Color scheme contrast and palette integrity across 9 themes | **KNOWN BASELINE FAILURES** (7 failures: `test1`, `test3`, `test5`, `test8`, `test9`, `test10`, `test12` due to Robolectric test timing with DataStore) |
| `WhoAmIEngineTest` | 5-stage progressive clue deduction and score decay | **PASSED** (100%) |
| `WhoAmIViewModelTest` | Standalone ViewModel state, clue reveals, and coin persistence | **PASSED** (100%) |

### Standard Verification Commands
```bash
# Run focused Who Am I tests (100% passing)
gradle :app:testDebugUnitTest --tests "com.multies.flagquest.WhoAmI*" --no-configuration-cache --no-daemon

# Run full unit test suite (83 passed, 7 known ThemeRegressionTest failures)
gradle :app:testDebugUnitTest --no-configuration-cache --no-daemon

# Compile Kotlin sources
gradle :app:compileDebugKotlin --no-configuration-cache --no-daemon

# Build Debug APK
gradle :app:assembleDebug --no-configuration-cache --no-daemon
```
**Latest Focused Test Run Result:** `BUILD SUCCESSFUL` (10/10 Who Am I engine & ViewModel tests passed).  
**Full Test Suite Status:** 83 tests passed, 7 known pre-existing failures in `ThemeRegressionTest`.  
**Latest Build Result:** `BUILD SUCCESSFUL in 20s (APK assembled)`.

---

## 13. External Services & Secrets Inventory

> **SECURITY NOTICE:** Only variable names are documented below. Never write secret values, tokens, or API keys into documentation.

| Variable Name | Purpose | Configuration Method |
| :--- | :--- | :--- |
| `GEMINI_API_KEY` | Server-side & client-side Gemini AI features | Injected via AI Studio Secrets Panel into `.env` at build time |
| `KEYSTORE_PATH` | Release signing keystore location (Optional) | CI/CD environment variable |
| `STORE_PASSWORD` | Release keystore password (Optional) | CI/CD environment variable |
| `KEY_PASSWORD` | Release key alias password (Optional) | CI/CD environment variable |

---

## 14. Non-Committed & Transient Files Inspection

When exporting or pushing this repository to GitHub or another AI Studio workspace, ensure the following handling:

1. **`.env`**:
   - **Status:** Gitignored.
   - **Action:** Must **NEVER** be committed. It is created dynamically by AI Studio using credentials configured in the Secrets panel.
2. **`debug.keystore`**:
   - **Status:** Gitignored. Generated automatically by the platform during build.
3. **`debug.keystore.base64`**:
   - **Status:** Committed at root.
   - **Purpose:** Pre-packaged standard Android debug key ensuring identical signing certificates across AI Studio container instances. Safe for development.
4. **`local.properties`**:
   - **Status:** Gitignored. Contains local machine SDK paths (`sdk.dir`), which are auto-configured in cloud environments.
5. **`.build-outputs/` & `/build` & `/app/build`**:
   - **Status:** Gitignored. Build artifacts, APKs, and intermediate compiler files.
6. **`my-upload-key.jks`**:
   - **Status:** Production release keystore. If created, it must **NEVER** be committed to public repositories.

---

## 15. Critical Project-Specific Invariants

1. **`metadata.json` Capability Preservation:**
   - You **MUST NOT** remove `"MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"` from `majorCapabilities` in `metadata.json`. It is required by the AI Studio container platform.
2. **Application ID & Namespace Stability:**
   - Do **NOT** rename `namespace = "com.multies.flagquest"` or `applicationId = "com.multies.flagquest"`. Renaming this package invalidates the local SQLite database path and breaks on-device updates.
3. **Database Name:**
   - The Room database file name `"flag_quest_database"` must remain unchanged across all versions.
4. **String Parity Requirement:**
   - Whenever any new string is introduced, all 4 languages (`values/strings.xml`, `values-ar`, `values-de`, `values-fr`, and `Locales.kt`) must be updated synchronously to prevent `LocalizationParityTest` failure.
5. **Offline Operation:**
   - The application must remain 100% functional without an active network connection. All data files must reside inside `app/src/main/assets/`.
