package com.multies.flagquest.data.model

import java.util.Random

object QuickGeographyEngine {

    // Verified Land Border Map (ISO Code -> List of bordering ISO codes)
    private val LAND_BORDERS = mapOf(
        "FR" to listOf("DE", "ES", "IT", "CH"),
        "DE" to listOf("FR", "CH", "SE"),
        "US" to listOf("CA", "MX"),
        "CA" to listOf("US"),
        "MX" to listOf("US"),
        "ES" to listOf("FR", "MA"),
        "IT" to listOf("FR", "CH"),
        "CH" to listOf("FR", "DE", "IT"),
        "MA" to listOf("DZ", "ES"),
        "DZ" to listOf("MA"),
        "RU" to listOf("FI", "NO", "CN"),
        "FI" to listOf("NO", "SE", "RU"),
        "NO" to listOf("SE", "FI", "RU"),
        "SE" to listOf("NO", "FI"),
        "AR" to listOf("BR", "CL"),
        "BR" to listOf("AR", "CO", "CL"),
        "CL" to listOf("AR", "BR"),
        "CO" to listOf("BR"),
        "CN" to listOf("IN", "RU", "VN"),
        "VN" to listOf("CN")
    )

    fun calculateQuestionScore(baseScore: Int = 100, elapsedMs: Long, streak: Int): Int {
        val speedBonus = when {
            elapsedMs < 2000L -> 50
            elapsedMs < 3000L -> 30
            elapsedMs < 5000L -> 10
            else -> 0
        }
        val (num, denom) = when {
            streak < 3 -> Pair(10, 10) // x1.0
            streak in 3..4 -> Pair(12, 10) // x1.2
            streak in 5..9 -> Pair(15, 10) // x1.5
            else -> Pair(20, 10) // x2.0
        }
        return ((baseScore + speedBonus) * num) / denom
    }

    fun getStreakMultiplier(streak: Int): Float {
        return when {
            streak < 3 -> 1.0f
            streak in 3..4 -> 1.2f
            streak in 5..9 -> 1.5f
            else -> 2.0f
        }
    }

    fun generateSessionQuestions(
        levelIndex: Int,
        countryCatalog: List<Country>,
        seed: Long = System.currentTimeMillis()
    ): List<QuickGeographyQuestion> {
        if (countryCatalog.isEmpty()) return emptyList()

        val rand = Random(seed + levelIndex * 1000L)
        val sessionQuestions = mutableListOf<QuickGeographyQuestion>()
        val targetCount = 75 // Generous question pool for 60-second session

        var attempts = 0
        val usedPrimaryIds = mutableListOf<String>()

        while (sessionQuestions.size < targetCount && attempts < 1000) {
            attempts++
            val type = QuickGeographyQuestionType.values()[rand.nextInt(QuickGeographyQuestionType.values().size)]
            
            // Pick country distinct from recent 3 primary countries
            val candidateCountries = countryCatalog.filter { !usedPrimaryIds.takeLast(3).contains(it.id) }.shuffled(rand)
            if (candidateCountries.isEmpty()) continue

            val primary = candidateCountries.first()
            val question = buildQuestionForType(type, primary, countryCatalog, rand, sessionQuestions.size)

            if (question != null && validateQuestion(question)) {
                sessionQuestions.add(question)
                usedPrimaryIds.add(primary.id)
            }
        }

        return sessionQuestions
    }

    private fun validateQuestion(q: QuickGeographyQuestion): Boolean {
        if (q.optionsEn.size < 2) return false
        if (q.optionsEn.distinct().size != q.optionsEn.size) return false
        if (q.optionsAr.distinct().size != q.optionsAr.size) return false
        if (q.optionsDe.distinct().size != q.optionsDe.size) return false
        if (q.optionsFr.distinct().size != q.optionsFr.size) return false
        if (q.correctOptionIndex !in q.optionsEn.indices) return false
        return true
    }

    private fun buildQuestionForType(
        type: QuickGeographyQuestionType,
        primary: Country,
        allCountries: List<Country>,
        rand: Random,
        qIndex: Int
    ): QuickGeographyQuestion? {
        val qId = "q_quick_geo_${type.name}_$qIndex"

        return when (type) {
            QuickGeographyQuestionType.FLAG_TO_COUNTRY -> {
                val distractors = allCountries.filter { it.id != primary.id }.shuffled(rand).take(3)
                if (distractors.size < 3) return null
                val options = (distractors + primary).shuffled(rand)
                val correctIndex = options.indexOf(primary)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which country does this flag belong to?",
                    promptAr = "إلى أي دولة ينتمي هذا العلم؟",
                    promptDe = "Zu welchem Land gehört diese Flagge?",
                    promptFr = "À quel pays appartient ce drapeau ?",
                    flagEmoji = primary.flagEmoji,
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = primary.id,
                    educationalFactEn = primary.funFactEn,
                    educationalFactAr = primary.funFactAr,
                    educationalFactDe = primary.funFactDe,
                    educationalFactFr = primary.funFactFr
                )
            }

            QuickGeographyQuestionType.COUNTRY_TO_CAPITAL -> {
                if (primary.capitalEn.isBlank() || primary.capitalEn == "None") return null
                val distractors = allCountries
                    .filter { it.id != primary.id && it.capitalEn.isNotBlank() && it.capitalEn != primary.capitalEn }
                    .shuffled(rand)
                    .take(3)
                if (distractors.size < 3) return null

                val options = (distractors + primary).shuffled(rand)
                val correctIndex = options.indexOf(primary)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "What is the capital of ${primary.nameEn}?",
                    promptAr = "ما هي عاصمة ${primary.nameAr}؟",
                    promptDe = "Was ist die Hauptstadt von ${primary.nameDe}?",
                    promptFr = "Quelle est la capitale de ${primary.nameFr} ?",
                    optionsEn = options.map { it.capitalEn },
                    optionsAr = options.map { it.capitalAr },
                    optionsDe = options.map { it.capitalDe },
                    optionsFr = options.map { it.capitalFr },
                    correctOptionIndex = correctIndex,
                    countryId = primary.id,
                    educationalFactEn = "${primary.capitalEn} is the capital of ${primary.nameEn}.",
                    educationalFactAr = "${primary.capitalAr} هي عاصمة ${primary.nameAr}.",
                    educationalFactDe = "${primary.capitalDe} ist die Hauptstadt von ${primary.nameDe}.",
                    educationalFactFr = "${primary.capitalFr} est la capitale de ${primary.nameFr}."
                )
            }

            QuickGeographyQuestionType.CAPITAL_TO_COUNTRY -> {
                if (primary.capitalEn.isBlank() || primary.capitalEn == "None") return null
                val distractors = allCountries
                    .filter { it.id != primary.id && it.capitalEn != primary.capitalEn }
                    .shuffled(rand)
                    .take(3)
                if (distractors.size < 3) return null

                val options = (distractors + primary).shuffled(rand)
                val correctIndex = options.indexOf(primary)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "${primary.capitalEn} is the capital of which country?",
                    promptAr = "${primary.capitalAr} هي عاصمة أي دولة؟",
                    promptDe = "${primary.capitalDe} ist die Hauptstadt welches Landes?",
                    promptFr = "${primary.capitalFr} est la capitale de quel pays ?",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = primary.id,
                    educationalFactEn = "${primary.capitalEn} is the official capital of ${primary.nameEn}.",
                    educationalFactAr = "${primary.capitalAr} هي العاصمة الرسمية لـ ${primary.nameAr}.",
                    educationalFactDe = "${primary.capitalDe} ist die offizielle Hauptstadt von ${primary.nameDe}.",
                    educationalFactFr = "${primary.capitalFr} est la capitale officielle de ${primary.nameFr}."
                )
            }

            QuickGeographyQuestionType.COUNTRY_TO_CONTINENT -> {
                val primaryContinent = primary.continentEn
                val allContinents = allCountries.map { it.continentEn }.distinct()
                val distractorContinents = allContinents.filter { it != primaryContinent }.shuffled(rand).take(3)
                if (distractorContinents.size < 3) return null

                val optionsEn = (distractorContinents + primaryContinent).shuffled(rand)
                val correctIndex = optionsEn.indexOf(primaryContinent)

                val optionsAr = optionsEn.map { c -> allCountries.firstOrNull { it.continentEn == c }?.continentAr ?: c }
                val optionsDe = optionsEn.map { c -> allCountries.firstOrNull { it.continentEn == c }?.continentDe ?: c }
                val optionsFr = optionsEn.map { c -> allCountries.firstOrNull { it.continentEn == c }?.continentFr ?: c }

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which continent is ${primary.nameEn} located in?",
                    promptAr = "في أي قارة تقع ${primary.nameAr}؟",
                    promptDe = "In welchem Kontinent liegt ${primary.nameDe}?",
                    promptFr = "Sur quel continent se trouve ${primary.nameFr} ?",
                    optionsEn = optionsEn,
                    optionsAr = optionsAr,
                    optionsDe = optionsDe,
                    optionsFr = optionsFr,
                    correctOptionIndex = correctIndex,
                    countryId = primary.id
                )
            }

            QuickGeographyQuestionType.MAP_SILHOUETTE -> {
                val verifiedCodes = setOf(
                    "US", "SA", "FR", "DE", "JP", "BR", "EG", "AU", "CA", "ZA",
                    "GB", "IN", "CN", "RU", "TR", "NZ", "MX", "AR", "IT", "ES",
                    "KR", "KE", "NG", "ID", "CH", "SE", "NO", "FI", "DZ", "MA",
                    "IS", "FJ", "CO", "VN", "CL", "PH"
                )
                if (!verifiedCodes.contains(primary.id.uppercase())) return null

                val distractors = allCountries.filter { it.id != primary.id }.shuffled(rand).take(3)
                if (distractors.size < 3) return null

                val options = (distractors + primary).shuffled(rand)
                val correctIndex = options.indexOf(primary)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which country is shown in this map silhouette?",
                    promptAr = "أي دولة تظهر في هذا المخطط الضلي؟",
                    promptDe = "Welches Land ist in dieser Silhouette zu sehen?",
                    promptFr = "Quel pays est représenté sur cette silhouette ?",
                    mapSvgPath = "maps/countries/${primary.id.lowercase()}.svg",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = primary.id
                )
            }

            QuickGeographyQuestionType.AREA_COMPARISON -> {
                val statsPrimary = CountryRankingEngine.STATS[primary.id.uppercase()] ?: return null
                val otherCountries = allCountries
                    .mapNotNull { c -> CountryRankingEngine.STATS[c.id.uppercase()]?.let { s -> Pair(c, s) } }
                    .filter { it.first.id != primary.id && it.second.areaSqKm != statsPrimary.areaSqKm }
                    .shuffled(rand)
                    .take(3)
                if (otherCountries.size < 3) return null

                val candidatePool = (otherCountries + Pair(primary, statsPrimary))
                val largestPair = candidatePool.maxByOrNull { it.second.areaSqKm } ?: return null

                val options = candidatePool.map { it.first }.shuffled(rand)
                val correctIndex = options.indexOf(largestPair.first)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which of these countries has the LARGEST total area?",
                    promptAr = "أي من هذه الدول ذات المساحة الإجمالية الأكبر؟",
                    promptDe = "Welches dieser Länder hat die GRÖSSERE Gesamtfläche?",
                    promptFr = "Lequel de ces pays a la plus GRANDE superficie totale ?",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = largestPair.first.id
                )
            }

            QuickGeographyQuestionType.FARTHER_NORTH -> {
                val statsPrimary = CountryRankingEngine.STATS[primary.id.uppercase()] ?: return null
                val otherCountries = allCountries
                    .mapNotNull { c -> CountryRankingEngine.STATS[c.id.uppercase()]?.let { s -> Pair(c, s) } }
                    .filter { it.first.id != primary.id && it.second.capitalLat != statsPrimary.capitalLat }
                    .shuffled(rand)
                    .take(3)
                if (otherCountries.size < 3) return null

                val candidatePool = (otherCountries + Pair(primary, statsPrimary))
                val northernmostPair = candidatePool.maxByOrNull { it.second.capitalLat } ?: return null

                val options = candidatePool.map { it.first }.shuffled(rand)
                val correctIndex = options.indexOf(northernmostPair.first)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which country's capital is located farther NORTH?",
                    promptAr = "أي دولة عاصمتها تقع أبعد أقصى الشمال؟",
                    promptDe = "Welches Landes Hauptstadt liegt weiter NÖRDLICH?",
                    promptFr = "La capitale de quel pays est la plus au NORD ?",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = northernmostPair.first.id
                )
            }

            QuickGeographyQuestionType.TRUE_FALSE -> {
                val isTrue = rand.nextBoolean()
                if (isTrue) {
                    QuickGeographyQuestion(
                        id = qId,
                        type = type,
                        promptEn = "True or False: ${primary.capitalEn} is the official capital of ${primary.nameEn}.",
                        promptAr = "صواب أم خطأ: ${primary.capitalAr} هي العاصمة الرسمية لـ ${primary.nameAr}.",
                        promptDe = "Wahr oder Falsch: ${primary.capitalDe} ist die offizielle Hauptstadt von ${primary.nameDe}.",
                        promptFr = "Vrai ou Faux : ${primary.capitalFr} est la capitale officielle de ${primary.nameFr}.",
                        optionsEn = listOf("TRUE", "FALSE"),
                        optionsAr = listOf("صواب", "خطأ"),
                        optionsDe = listOf("WAHR", "FALSCH"),
                        optionsFr = listOf("VRAI", "FAUX"),
                        correctOptionIndex = 0,
                        countryId = primary.id
                    )
                } else {
                    val wrongCountry = allCountries.filter { it.id != primary.id && it.capitalEn != primary.capitalEn }.shuffled(rand).firstOrNull() ?: return null
                    QuickGeographyQuestion(
                        id = qId,
                        type = type,
                        promptEn = "True or False: ${wrongCountry.capitalEn} is the capital of ${primary.nameEn}.",
                        promptAr = "صواب أم خطأ: ${wrongCountry.capitalAr} هي عاصمة ${primary.nameAr}.",
                        promptDe = "Wahr oder Falsch: ${wrongCountry.capitalDe} ist die Hauptstadt von ${primary.nameDe}.",
                        promptFr = "Vrai ou Faux : ${wrongCountry.capitalFr} est la capitale de ${primary.nameFr}.",
                        optionsEn = listOf("TRUE", "FALSE"),
                        optionsAr = listOf("صواب", "خطأ"),
                        optionsDe = listOf("WAHR", "FALSCH"),
                        optionsFr = listOf("VRAI", "FAUX"),
                        correctOptionIndex = 1,
                        countryId = primary.id
                    )
                }
            }

            QuickGeographyQuestionType.LAND_BORDER -> {
                val borders = LAND_BORDERS[primary.id.uppercase()]
                if (borders.isNullOrEmpty()) return null

                val correctBorderId = borders.shuffled(rand).first()
                val correctCountry = allCountries.firstOrNull { it.id.equals(correctBorderId, ignoreCase = true) } ?: return null

                val nonBorders = allCountries.filter { it.id != primary.id && !borders.contains(it.id.uppercase()) }.shuffled(rand).take(3)
                if (nonBorders.size < 3) return null

                val options = (nonBorders + correctCountry).shuffled(rand)
                val correctIndex = options.indexOf(correctCountry)

                QuickGeographyQuestion(
                    id = qId,
                    type = type,
                    promptEn = "Which country shares a land border with ${primary.nameEn}?",
                    promptAr = "أي دولة تشترك في حدود برية مع ${primary.nameAr}؟",
                    promptDe = "Welches Land grenzt auf dem Landweg an ${primary.nameDe}?",
                    promptFr = "Quel pays partage une frontière terrestre avec ${primary.nameFr} ?",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIndex,
                    countryId = primary.id
                )
            }
        }
    }
}
