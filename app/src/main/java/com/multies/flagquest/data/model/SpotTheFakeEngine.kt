package com.multies.flagquest.data.model

import kotlin.random.Random

object SpotTheFakeEngine {

    fun generateLevelQuestions(
        config: SpotTheFakeLevelConfig,
        allCountries: List<Country>,
        random: Random = Random.Default
    ): List<SpotTheFakeQuestion> {
        val questions = mutableListOf<SpotTheFakeQuestion>()
        val countryMap = allCountries.associateBy { it.id.uppercase() }

        // Use target countries from config or fallback to all available countries
        val targetList = config.targetCountries
            .mapNotNull { countryMap[it] }
            .ifEmpty { allCountries.shuffled(random) }

        for (round in 0 until config.roundsCount) {
            val format = config.allowedFormats[round % config.allowedFormats.size]
            val targetCountry = targetList[round % targetList.size]

            val question = generateRoundQuestion(
                roundIndex = round + 1,
                format = format,
                targetCountry = targetCountry,
                allCountries = allCountries,
                allowedMutations = config.allowedMutations,
                random = random
            )
            questions.add(question)
        }

        return questions
    }

    fun generateRoundQuestion(
        roundIndex: Int,
        format: GameFormat,
        targetCountry: Country,
        allCountries: List<Country>,
        allowedMutations: List<FakeMutationType>,
        random: Random = Random.Default
    ): SpotTheFakeQuestion {
        val selectedMutationType = allowedMutations.random(random)
        val mutation = createMutationForCountry(targetCountry, selectedMutationType, random)

        val options = mutableListOf<SpotTheFakeOption>()
        val fakeOptionIndex: Int

        when (format) {
            GameFormat.FORMAT_A -> {
                // 4 options of same country flag: 3 authentic, 1 fake
                fakeOptionIndex = random.nextInt(4)
                for (i in 0 until 4) {
                    if (i == fakeOptionIndex) {
                        options.add(
                            SpotTheFakeOption(
                                id = "${targetCountry.id}_fake_$i",
                                country = targetCountry,
                                isFake = true,
                                mutation = mutation
                            )
                        )
                    } else {
                        options.add(
                            SpotTheFakeOption(
                                id = "${targetCountry.id}_auth_$i",
                                country = targetCountry,
                                isFake = false,
                                mutation = null
                            )
                        )
                    }
                }
            }
            GameFormat.FORMAT_B -> {
                // 4 options of different countries: 3 authentic flags, 1 fake flag
                val distractors = allCountries
                    .filter { it.id != targetCountry.id }
                    .shuffled(random)
                    .take(3)

                fakeOptionIndex = random.nextInt(4)
                var distractorIdx = 0

                for (i in 0 until 4) {
                    if (i == fakeOptionIndex) {
                        options.add(
                            SpotTheFakeOption(
                                id = "${targetCountry.id}_fake",
                                country = targetCountry,
                                isFake = true,
                                mutation = mutation
                            )
                        )
                    } else {
                        val dist = distractors[distractorIdx++]
                        options.add(
                            SpotTheFakeOption(
                                id = "${dist.id}_auth",
                                country = dist,
                                isFake = false,
                                mutation = null
                            )
                        )
                    }
                }
            }
            GameFormat.FORMAT_C -> {
                // Format C: Binary decision (1 option rendered as either fake or authentic)
                val showFake = random.nextBoolean()
                fakeOptionIndex = if (showFake) 0 else 1 // Option 0 isFake if showFake == true

                options.add(
                    SpotTheFakeOption(
                        id = "${targetCountry.id}_card_fake",
                        country = targetCountry,
                        isFake = true,
                        mutation = mutation
                    )
                )
                options.add(
                    SpotTheFakeOption(
                        id = "${targetCountry.id}_card_auth",
                        country = targetCountry,
                        isFake = false,
                        mutation = null
                    )
                )
            }
        }

        val explanations = buildLocalizedExplanations(targetCountry, mutation)

        return SpotTheFakeQuestion(
            roundIndex = roundIndex,
            format = format,
            targetCountry = targetCountry,
            options = options,
            fakeOptionIndex = fakeOptionIndex,
            explanationEn = explanations["en"] ?: "",
            explanationAr = explanations["ar"] ?: "",
            explanationDe = explanations["de"] ?: "",
            explanationFr = explanations["fr"] ?: ""
        )
    }

    private fun createMutationForCountry(
        country: Country,
        type: FakeMutationType,
        random: Random
    ): FakeFlagMutation {
        return when (type) {
            FakeMutationType.INCORRECT_COLOR -> FakeFlagMutation(
                type = type,
                targetColor = "RED",
                replacementColor = "PURPLE"
            )
            FakeMutationType.SWAP_STRIPE_COLORS -> FakeFlagMutation(
                type = type,
                swappedStripeIndices = Pair(0, 1)
            )
            FakeMutationType.MISSING_SYMBOL -> FakeFlagMutation(
                type = type,
                symbolName = "emblem"
            )
            FakeMutationType.EXTRA_SYMBOL -> FakeFlagMutation(
                type = type,
                symbolName = "star"
            )
            FakeMutationType.INCORRECT_STAR_COUNT -> FakeFlagMutation(
                type = type,
                starCountActual = 1,
                starCountFake = 4
            )
            FakeMutationType.INCORRECT_STRIPE_ORIENTATION -> FakeFlagMutation(
                type = type,
                originalOrientation = "HORIZONTAL",
                fakeOrientation = "VERTICAL"
            )
            FakeMutationType.INCORRECT_SYMBOL_POSITION -> FakeFlagMutation(
                type = type,
                originalPosition = "CENTER",
                fakePosition = "TOP_LEFT"
            )
            FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL -> FakeFlagMutation(
                type = type
            )
            FakeMutationType.INCORRECT_FIELD_OR_BORDER -> FakeFlagMutation(
                type = type,
                borderOriginalColor = "WHITE",
                borderFakeColor = "BLACK"
            )
            FakeMutationType.SUBTLE_PROPORTION_CHANGE -> FakeFlagMutation(
                type = type,
                stripeName = "middle"
            )
        }
    }

    fun buildLocalizedExplanations(country: Country, mutation: FakeFlagMutation): Map<String, String> {
        val nameEn = country.nameEn
        val nameAr = country.nameAr
        val nameDe = country.nameDe
        val nameFr = country.nameFr

        return when (mutation.type) {
            FakeMutationType.INCORRECT_COLOR -> mapOf(
                "en" to "The authentic flag of $nameEn features specific primary colors. Here, a key color was replaced.",
                "ar" to "يتكوّن علم $nameAr الأصلي من ألوان أساسية محددة. هنا، تم استبدال أحد الألوان الرئيسية.",
                "de" to "Die echte Flagge von $nameDe hat bestimmte Hauptfarben. Hier wurde eine Hauptfarbe ersetzt.",
                "fr" to "Le drapeau authentique de $nameFr comporte des couleurs primaires spécifiques. Ici, une couleur clé a été remplacée."
            )
            FakeMutationType.SWAP_STRIPE_COLORS -> mapOf(
                "en" to "The authentic flag of $nameEn has stripes arranged in a precise color sequence. Here the colors are swapped.",
                "ar" to "يتكوّن علم $nameAr الأصلي من أشرطة ملونة بترتيب دقيق. هنا تم تبديل ترتيب الألوان.",
                "de" to "Die echte Flagge von $nameDe hat Streifen in einer genauen Farbreihenfolge. Hier wurden die Farben vertauscht.",
                "fr" to "Le drapeau authentique de $nameFr comporte des bandes dans un ordre précis. Ici, les couleurs sont inversées."
            )
            FakeMutationType.MISSING_SYMBOL -> mapOf(
                "en" to "The authentic flag of $nameEn includes an emblem or central symbol, which is missing here.",
                "ar" to "يتضمن علم $nameAr الأصلي شعاراً أو رمزاً مركزياً، وهو مفقود هنا.",
                "de" to "Die echte Flagge von $nameDe enthält ein Emblem oder zentrales Symbol, das hier fehlt.",
                "fr" to "Le drapeau authentique de $nameFr inclut un emblème ou un symbole central, qui est absent ici."
            )
            FakeMutationType.EXTRA_SYMBOL -> mapOf(
                "en" to "The authentic flag of $nameEn does not contain an extra symbol on the field.",
                "ar" to "علم $nameAr الأصلي لا يحتوي على رمز إضافي مضاف على العلم.",
                "de" to "Die echte Flagge von $nameDe enthält kein zusätzliches Symbol auf dem Feld.",
                "fr" to "Le drapeau authentique de $nameFr ne comporte pas de symbole supplémentaire sur le champ."
            )
            FakeMutationType.INCORRECT_STAR_COUNT -> mapOf(
                "en" to "The authentic flag of $nameEn has a specific number of stars, which differs from this version.",
                "ar" to "يحتوي علم $nameAr الأصلي على عدد محدد من النجوم يختلف عن النسخة الظاهرة هنا.",
                "de" to "Die echte Flagge von $nameDe hat eine bestimmte Anzahl an Sternen, die sich von dieser Version unterscheidet.",
                "fr" to "Le drapeau authentique de $nameFr comporte un nombre précis d'étoiles, différent de cette version."
            )
            FakeMutationType.INCORRECT_STRIPE_ORIENTATION -> mapOf(
                "en" to "The authentic flag of $nameEn uses horizontal/vertical stripes, rendered in the wrong orientation here.",
                "ar" to "يستخدم علم $nameAr الأصلي أشرطة ملونة باتجاه مختلف عن الاتجاه المعروض هنا.",
                "de" to "Die echte Flagge von $nameDe nutzt Streifen in einer anderen Ausrichtung als hier dargestellt.",
                "fr" to "Le drapeau authentique de $nameFr utilise des bandes orientées différemment de ce qui est affiché ici."
            )
            FakeMutationType.INCORRECT_SYMBOL_POSITION -> mapOf(
                "en" to "The emblem on the flag of $nameEn is located in a different position.",
                "ar" to "يقع الشعار على علم $nameAr في موضع مختلف عن الموضع الظاهر هنا.",
                "de" to "Das Emblem auf der Flagge von $nameDe befindet sich an einer anderen Position.",
                "fr" to "L'emblème sur le drapeau de $nameFr est situé à une position différente."
            )
            FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL -> mapOf(
                "en" to "The symbol on the authentic flag of $nameEn faces a specific direction and is mirrored here.",
                "ar" to "يتجه الرمز في علم $nameAr الأصلي باتجاه محدد، وهو ظاهر هنا بشكل معكوس.",
                "de" to "Das Symbol auf der echten Flagge von $nameDe zeigt in eine bestimmte Richtung und ist hier spiegelverkehrt.",
                "fr" to "Le symbole sur le drapeau authentique de $nameFr est orienté dans un sens précis, et inversé ici."
            )
            FakeMutationType.INCORRECT_FIELD_OR_BORDER -> mapOf(
                "en" to "The authentic flag of $nameEn has a specific field background or border.",
                "ar" to "يمتلك علم $nameAr الأصلي خلفية أو إطاراً محدداً يختلف عما يظهر هنا.",
                "de" to "Die echte Flagge von $nameDe hat einen bestimmten Hintergrund oder Rand.",
                "fr" to "Le drapeau authentique de $nameFr comporte un fond ou une bordure spécifique."
            )
            FakeMutationType.SUBTLE_PROPORTION_CHANGE -> mapOf(
                "en" to "On the authentic flag of $nameEn, the stripe proportions and width ratios differ.",
                "ar" to "في علم $nameAr الأصلي، تختلف نسب الأشرطة وأبعاد الألوان.",
                "de" to "Auf der echten Flagge von $nameDe weichen die Streifenproportionen und Breitenverhältnisse ab.",
                "fr" to "Sur le drapeau authentique de $nameFr, les proportions des bandes et les ratios de largeur diffèrent."
            )
        }
    }

    fun calculateResult(
        levelIndex: Int,
        correctAnswers: Int,
        totalRounds: Int,
        maxStreak: Int,
        timeTakenSec: Long
    ): SpotTheFakeScoreResult {
        val accuracyPercent = if (totalRounds > 0) (correctAnswers * 100) / totalRounds else 0
        val baseScore = correctAnswers * 100
        val streakBonus = maxStreak * 25
        val speedBonus = ((totalRounds * 15 - timeTakenSec).coerceAtLeast(0) * 5).toInt()
        val totalScore = baseScore + streakBonus + speedBonus

        val stars = when {
            accuracyPercent >= 90 -> 3
            accuracyPercent >= 70 -> 2
            accuracyPercent >= 50 -> 1
            else -> 0
        }

        val coinsEarned = if (stars > 0) 50 * stars + maxStreak * 10 else 10

        return SpotTheFakeScoreResult(
            levelIndex = levelIndex,
            score = totalScore,
            correctAnswers = correctAnswers,
            totalRounds = totalRounds,
            stars = stars,
            coinsEarned = coinsEarned,
            maxStreak = maxStreak,
            accuracyPercent = accuracyPercent,
            timeTakenSec = timeTakenSec
        )
    }
}
