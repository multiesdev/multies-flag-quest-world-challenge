package com.multies.flagquest.data.model

import java.util.Random

object QuestionEngine {

    private val random = Random(42L)

    fun generateAllQuestions(countries: List<Country>, organizations: List<Organization>): List<Question> {
        val questions = mutableListOf<Question>()

        // 1. FLAGS & CONTINENTS (100 Levels)
        questions.addAll(generateFlagsQuestions(countries, 100))

        // 2. INTERNATIONAL ORGANIZATIONS (50 Levels)
        questions.addAll(generateOrganizationsQuestions(countries, organizations, 50))

        // 3. COUNTRY AREA (50 Levels)
        questions.addAll(generateAreaQuestions(countries, 50))

        // 4. POPULATION (50 Levels)
        questions.addAll(generatePopulationQuestions(countries, 50))

        // 5. CAPITALS (50 Levels)
        questions.addAll(generateCapitalsQuestions(countries, 50))

        // 6. CURRENCIES (50 Levels)
        questions.addAll(generateCurrenciesQuestions(countries, 50))

        // 7. MAPS & LOCATIONS (50 Levels)
        questions.addAll(generateMapsQuestions(countries, 50))

        return questions
    }

    private fun generateFlagsQuestions(countries: List<Country>, count: Int = 100): List<Question> {
        val list = mutableListOf<Question>()
        val baseSeed = 101L
        var passCount = 0
        var currentCyclePool = countries.shuffled(Random(baseSeed + passCount))
        var poolIndex = 0

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 35) "MEDIUM" else "HARD"

            // Primary country uniqueness within level
            val levelPrimaryCountries = mutableListOf<Country>()
            while (levelPrimaryCountries.size < questionsCount) {
                if (poolIndex >= currentCyclePool.size) {
                    passCount++
                    currentCyclePool = countries.shuffled(Random(baseSeed + passCount))
                    poolIndex = 0
                }
                val candidate = currentCyclePool[poolIndex]
                poolIndex++
                if (!levelPrimaryCountries.contains(candidate)) {
                    levelPrimaryCountries.add(candidate)
                }
            }

            for (qIdx in 0 until questionsCount) {
                val country = levelPrimaryCountries[qIdx]
                val rand = Random(baseSeed + level * 1000L + qIdx)

                if (level <= 70) {
                    // Flag-to-Country Question
                    val sameContinent = countries.filter { it.id != country.id && it.continentEn == country.continentEn }
                    val otherCountries = countries.filter { it.id != country.id && it.continentEn != country.continentEn }

                    val distractorsNeeded = optionCount - 1
                    val chosenDistractors = mutableListOf<Country>()
                    val sameShuffled = sameContinent.shuffled(rand)
                    chosenDistractors.addAll(sameShuffled.take(distractorsNeeded))

                    if (chosenDistractors.size < distractorsNeeded) {
                        val remaining = distractorsNeeded - chosenDistractors.size
                        val otherShuffled = otherCountries.filter { !chosenDistractors.contains(it) }.shuffled(rand)
                        chosenDistractors.addAll(otherShuffled.take(remaining))
                    }

                    val options = (chosenDistractors + country).shuffled(rand)
                    val correctIndex = options.indexOf(country)

                    list.add(
                        Question(
                            id = "q_flags_lvl_${level}_$qIdx",
                            category = "FLAGS",
                            difficulty = difficulty,
                            questionTextEn = "Which country does this flag ${country.flagEmoji} belong to?",
                            questionTextAr = "إلى أي بلد ينتمي هذا العلم ${country.flagEmoji}؟",
                            questionTextDe = "Zu welchem Land gehört diese Flagge ${country.flagEmoji}?",
                            questionTextFr = "À quel pays appartient ce drapeau ${country.flagEmoji}?",
                            optionsEn = options.map { it.nameEn },
                            optionsAr = options.map { it.nameAr },
                            optionsDe = options.map { it.nameDe },
                            optionsFr = options.map { it.nameFr },
                            correctOptionIndex = correctIndex,
                            countryId = country.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                } else {
                    // Country-to-Continent Question
                    val continentEn = country.continentEn

                    val allContinents = countries.map { it.continentEn }.distinct()
                    val distractorContinents = allContinents.filter { it != continentEn }.shuffled(rand).take(optionCount - 1)
                    val finalContinentsEn = (distractorContinents + continentEn).shuffled(rand)
                    val correctIndex = finalContinentsEn.indexOf(continentEn)

                    val finalContinentsAr = finalContinentsEn.map { c -> countries.firstOrNull { it.continentEn == c }?.continentAr ?: c }
                    val finalContinentsDe = finalContinentsEn.map { c -> countries.firstOrNull { it.continentEn == c }?.continentDe ?: c }
                    val finalContinentsFr = finalContinentsEn.map { c -> countries.firstOrNull { it.continentEn == c }?.continentFr ?: c }

                    list.add(
                        Question(
                            id = "q_flags_lvl_${level}_$qIdx",
                            category = "FLAGS",
                            difficulty = difficulty,
                            questionTextEn = "Which continent is ${country.nameEn} ${country.flagEmoji} located in?",
                            questionTextAr = "في أي قارة تقع ${country.nameAr} ${country.flagEmoji}؟",
                            questionTextDe = "In welchem Kontinent liegt ${country.nameDe} ${country.flagEmoji}?",
                            questionTextFr = "Dans quel continent se trouve ${country.nameFr} ${country.flagEmoji}?",
                            optionsEn = finalContinentsEn,
                            optionsAr = finalContinentsAr,
                            optionsDe = finalContinentsDe,
                            optionsFr = finalContinentsFr,
                            correctOptionIndex = correctIndex,
                            countryId = country.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                }
            }
        }
        return list
    }

    private fun generateOrganizationsQuestions(
        countries: List<Country>,
        organizations: List<Organization>,
        count: Int = 50
    ): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(202L)
        val orgs = if (organizations.isEmpty()) getFallbackOrganizations() else organizations

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val org = orgs[(level + qIdx) % orgs.size]

                if (qIdx % 2 == 0) {
                    val memberIds = org.fullMembers
                    val validCountries = countries.filter { memberIds.contains(it.id) }
                    if (validCountries.isEmpty()) continue

                    val correctCountry = validCountries.shuffled(rand).first()
                    val distractorCountries = countries.filter { !memberIds.contains(it.id) && !org.candidates.contains(it.id) }
                        .shuffled(rand).take(optionCount - 1)

                    val finalCountries = (distractorCountries + correctCountry).shuffled(rand)
                    val correctIndex = finalCountries.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_org_lvl_${level}_$qIdx",
                            category = "ORGANIZATIONS",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries is a member of ${org.nameEn}?",
                            questionTextAr = "أي من هذه الدول عضو في ${org.nameAr}؟",
                            questionTextDe = "Welches dieser Länder ist Mitglied der ${org.nameDe}?",
                            questionTextFr = "Lequel de ces pays est membre de ${org.nameFr}?",
                            optionsEn = finalCountries.map { it.nameEn },
                            optionsAr = finalCountries.map { it.nameAr },
                            optionsDe = finalCountries.map { it.nameDe },
                            optionsFr = finalCountries.map { it.nameFr },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                } else {
                    val memberIds = org.fullMembers
                    val validMembers = countries.filter { memberIds.contains(it.id) }
                    if (validMembers.size < optionCount - 1) continue

                    val nonMemberCountries = countries.filter { !memberIds.contains(it.id) && !org.candidates.contains(it.id) }
                    if (nonMemberCountries.isEmpty()) continue

                    val correctCountry = nonMemberCountries.shuffled(rand).first()
                    val distractorCountries = validMembers.shuffled(rand).take(optionCount - 1)

                    val finalCountries = (distractorCountries + correctCountry).shuffled(rand)
                    val correctIndex = finalCountries.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_org_lvl_${level}_$qIdx",
                            category = "ORGANIZATIONS",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries is NOT a member of ${org.nameEn}?",
                            questionTextAr = "أي من هذه الدول ليست عضواً في ${org.nameAr}؟",
                            questionTextDe = "Welches dieser Länder ist KEIN Mitglied der ${org.nameDe}?",
                            questionTextFr = "Lequel de ces pays n'est PAS membre de ${org.nameFr}?",
                            optionsEn = finalCountries.map { it.nameEn },
                            optionsAr = finalCountries.map { it.nameAr },
                            optionsDe = finalCountries.map { it.nameDe },
                            optionsFr = finalCountries.map { it.nameFr },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                }
            }
        }
        return list
    }

    private fun generateAreaQuestions(countries: List<Country>, count: Int = 50): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(303L)

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val candidateList = countries.shuffled(rand).take(optionCount).sortedBy { it.areaSqKm }

                if (qIdx % 2 == 0) {
                    val correctCountry = candidateList.last()
                    val options = candidateList.shuffled(rand)
                    val correctIndex = options.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_area_lvl_${level}_$qIdx",
                            category = "AREA",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries has the LARGEST total area?",
                            questionTextAr = "أي من هذه الدول لديها المساحة الإجمالية الأكبر؟",
                            questionTextDe = "Welches dieser Länder hat die GRÖSSTE Gesamtfläche?",
                            questionTextFr = "Lequel de ces pays a la plus GRANDE superficie totale?",
                            optionsEn = options.map { "${it.nameEn} (${formatNumber(it.areaSqKm.toLong())} sq km)" },
                            optionsAr = options.map { "${it.nameAr} (${formatNumber(it.areaSqKm.toLong())} كم²)" },
                            optionsDe = options.map { "${it.nameDe} (${formatNumber(it.areaSqKm.toLong())} km²)" },
                            optionsFr = options.map { "${it.nameFr} (${formatNumber(it.areaSqKm.toLong())} km²)" },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                } else {
                    val correctCountry = candidateList.first()
                    val options = candidateList.shuffled(rand)
                    val correctIndex = options.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_area_lvl_${level}_$qIdx",
                            category = "AREA",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries has the SMALLEST total area?",
                            questionTextAr = "أي من هذه الدول لديها المساحة الإجمالية الأصغر؟",
                            questionTextDe = "Welches dieser Länder hat die KLEINSTE Gesamtfläche?",
                            questionTextFr = "Lequel de ces pays a la plus PETITE superficie totale?",
                            optionsEn = options.map { "${it.nameEn} (${formatNumber(it.areaSqKm.toLong())} sq km)" },
                            optionsAr = options.map { "${it.nameAr} (${formatNumber(it.areaSqKm.toLong())} كم²)" },
                            optionsDe = options.map { "${it.nameDe} (${formatNumber(it.areaSqKm.toLong())} km²)" },
                            optionsFr = options.map { "${it.nameFr} (${formatNumber(it.areaSqKm.toLong())} km²)" },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                }
            }
        }
        return list
    }

    private fun generatePopulationQuestions(countries: List<Country>, count: Int = 50): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(404L)

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val candidateList = countries.shuffled(rand).take(optionCount).sortedBy { it.population }

                if (qIdx % 2 == 0) {
                    val correctCountry = candidateList.last()
                    val options = candidateList.shuffled(rand)
                    val correctIndex = options.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_pop_lvl_${level}_$qIdx",
                            category = "POPULATION",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries has the LARGEST population?",
                            questionTextAr = "أي من هذه الدول لديها عدد السكان الأكبر؟",
                            questionTextDe = "Welches dieser Länder hat die GRÖSSTE Bevölkerung?",
                            questionTextFr = "Lequel de ces pays a la population la plus ÉLEVÉE?",
                            optionsEn = options.map { "${it.nameEn} (${formatNumber(it.population)})" },
                            optionsAr = options.map { "${it.nameAr} (${formatNumber(it.population)})" },
                            optionsDe = options.map { "${it.nameDe} (${formatNumber(it.population)})" },
                            optionsFr = options.map { "${it.nameFr} (${formatNumber(it.population)})" },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                } else {
                    val correctCountry = candidateList.first()
                    val options = candidateList.shuffled(rand)
                    val correctIndex = options.indexOf(correctCountry)

                    list.add(
                        Question(
                            id = "q_pop_lvl_${level}_$qIdx",
                            category = "POPULATION",
                            difficulty = difficulty,
                            questionTextEn = "Which of these countries has the SMALLEST population?",
                            questionTextAr = "أي من هذه الدول لديها عدد السكان الأصغر؟",
                            questionTextDe = "Welches dieser Länder hat die KLEINSTE Bevölkerung?",
                            questionTextFr = "Lequel de ces pays a la population la plus FAIBLE?",
                            optionsEn = options.map { "${it.nameEn} (${formatNumber(it.population)})" },
                            optionsAr = options.map { "${it.nameAr} (${formatNumber(it.population)})" },
                            optionsDe = options.map { "${it.nameDe} (${formatNumber(it.population)})" },
                            optionsFr = options.map { "${it.nameFr} (${formatNumber(it.population)})" },
                            correctOptionIndex = correctIndex,
                            countryId = correctCountry.id,
                            requiredLevel = level,
                            timerDuration = timerDuration
                        )
                    )
                }
            }
        }
        return list
    }

    private fun generateCapitalsQuestions(countries: List<Country>, count: Int = 50): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(505L)
        val shuffledPool = countries.shuffled(rand)

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val targetIndex = (level * questionsCount + qIdx) % shuffledPool.size
                val targetCountry = shuffledPool[targetIndex]

                val distractors = countries.filter { it.id != targetCountry.id && it.capitalEn != targetCountry.capitalEn }
                    .distinctBy { it.capitalEn }
                    .shuffled(rand)
                    .take(optionCount - 1)

                val options = (distractors + targetCountry).shuffled(rand)
                val correctIndex = options.indexOf(targetCountry)

                list.add(
                    Question(
                        id = "q_cap_lvl_${level}_$qIdx",
                        category = "CAPITALS",
                        difficulty = difficulty,
                        questionTextEn = "What is the capital city of ${targetCountry.nameEn} ${targetCountry.flagEmoji}?",
                        questionTextAr = "ما هي عاصمة ${targetCountry.nameAr} ${targetCountry.flagEmoji}؟",
                        questionTextDe = "Was ist die Hauptstadt von ${targetCountry.nameDe} ${targetCountry.flagEmoji}?",
                        questionTextFr = "Quelle est la capitale de ${targetCountry.nameFr} ${targetCountry.flagEmoji}?",
                        optionsEn = options.map { it.capitalEn },
                        optionsAr = options.map { it.capitalAr },
                        optionsDe = options.map { it.capitalDe },
                        optionsFr = options.map { it.capitalFr },
                        correctOptionIndex = correctIndex,
                        countryId = targetCountry.id,
                        requiredLevel = level,
                        timerDuration = timerDuration
                    )
                )
            }
        }
        return list
    }

    private fun generateCurrenciesQuestions(countries: List<Country>, count: Int = 50): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(606L)
        val shuffledPool = countries.shuffled(rand)

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val targetIndex = (level * questionsCount + qIdx) % shuffledPool.size
                val targetCountry = shuffledPool[targetIndex]

                val distractors = countries.filter { it.currencyEn != targetCountry.currencyEn }
                    .distinctBy { it.currencyEn }
                    .shuffled(rand)
                    .take(optionCount - 1)

                val optionsEn = (distractors.map { it.currencyEn } + targetCountry.currencyEn).shuffled(rand)
                val correctIndex = optionsEn.indexOf(targetCountry.currencyEn)

                val optionsAr = optionsEn.map { cEn -> countries.firstOrNull { it.currencyEn == cEn }?.currencyAr ?: cEn }
                val optionsDe = optionsEn.map { cEn -> countries.firstOrNull { it.currencyEn == cEn }?.currencyDe ?: cEn }
                val optionsFr = optionsEn.map { cEn -> countries.firstOrNull { it.currencyEn == cEn }?.currencyFr ?: cEn }

                list.add(
                    Question(
                        id = "q_curr_lvl_${level}_$qIdx",
                        category = "CURRENCIES",
                        difficulty = difficulty,
                        questionTextEn = "Which currency is officially used in ${targetCountry.nameEn} ${targetCountry.flagEmoji}?",
                        questionTextAr = "ما هي العملة الرسمية المستعملة في ${targetCountry.nameAr} ${targetCountry.flagEmoji}؟",
                        questionTextDe = "Welche Währung wird offiziell in ${targetCountry.nameDe} ${targetCountry.flagEmoji} verwendet?",
                        questionTextFr = "Quelle monnaie est officiellement utilisée en ${targetCountry.nameFr} ${targetCountry.flagEmoji}?",
                        optionsEn = optionsEn,
                        optionsAr = optionsAr,
                        optionsDe = optionsDe,
                        optionsFr = optionsFr,
                        correctOptionIndex = correctIndex,
                        countryId = targetCountry.id,
                        requiredLevel = level,
                        timerDuration = timerDuration
                    )
                )
            }
        }
        return list
    }

    private fun generateMapsQuestions(countries: List<Country>, count: Int = 50): List<Question> {
        val list = mutableListOf<Question>()
        val rand = Random(707L)
        val shuffledPool = countries.shuffled(rand)

        for (level in 1..count) {
            val questionsCount = when {
                level <= 10 -> 5
                level <= 20 -> 7
                else -> 10
            }
            val optionCount = if (level <= 10) 3 else 4
            val timerDuration = when {
                level <= 10 -> 0
                level <= 20 -> 20
                level <= 35 -> 15
                else -> 12
            }
            val difficulty = if (level <= 15) "EASY" else if (level <= 30) "MEDIUM" else "HARD"

            for (qIdx in 0 until questionsCount) {
                val targetIndex = (level * questionsCount + qIdx) % shuffledPool.size
                val targetCountry = shuffledPool[targetIndex]

                val distractors = countries.filter { it.id != targetCountry.id }
                    .shuffled(rand)
                    .take(optionCount - 1)

                val options = (distractors + targetCountry).shuffled(rand)
                val correctIndex = options.indexOf(targetCountry)

                list.add(
                    Question(
                        id = "q_maps_lvl_${level}_$qIdx",
                        category = "MAPS",
                        difficulty = difficulty,
                        questionTextEn = "Which country map outline corresponds to ${targetCountry.nameEn} ${targetCountry.flagEmoji}?",
                        questionTextAr = "أي خريطة جغرافية تنتمي لـ ${targetCountry.nameAr} ${targetCountry.flagEmoji}؟",
                        questionTextDe = "Welche Karte entspricht ${targetCountry.nameDe} ${targetCountry.flagEmoji}?",
                        questionTextFr = "Quelle carte correspond à ${targetCountry.nameFr} ${targetCountry.flagEmoji}?",
                        optionsEn = options.map { it.nameEn },
                        optionsAr = options.map { it.nameAr },
                        optionsDe = options.map { it.nameDe },
                        optionsFr = options.map { it.nameFr },
                        correctOptionIndex = correctIndex,
                        countryId = targetCountry.id,
                        requiredLevel = level,
                        timerDuration = timerDuration
                    )
                )
            }
        }
        return list
    }

    private fun formatNumber(num: Long): String {
        return java.text.NumberFormat.getInstance(java.util.Locale.US).format(num)
    }

    private fun getFallbackOrganizations(): List<Organization> {
        return listOf(
            Organization(
                id = "EU",
                nameEn = "European Union",
                nameAr = "الاتحاد الأوروبي",
                nameDe = "Europäische Union",
                nameFr = "Union européenne",
                fullMembers = listOf("FR", "DE", "IT", "ES", "SE", "FI", "AT", "BE", "NL", "PL", "PT", "RO"),
                candidates = listOf("TR", "UA"),
                observers = emptyList(),
                suspended = emptyList()
            ),
            Organization(
                id = "UN",
                nameEn = "United Nations",
                nameAr = "الأمم المتحدة",
                nameDe = "Vereinte Nationen",
                nameFr = "Nations Unies",
                fullMembers = listOf("US", "FR", "GB", "CN", "RU", "DE", "JP", "IN", "BR", "ZA", "EG", "SA"),
                candidates = emptyList(),
                observers = listOf("VA", "PS"),
                suspended = emptyList()
            ),
            Organization(
                id = "NATO",
                nameEn = "NATO",
                nameAr = "حلف الناتو",
                nameDe = "NATO",
                nameFr = "OTAN",
                fullMembers = listOf("US", "FR", "DE", "GB", "IT", "ES", "SE", "NO", "FI", "IS", "CA", "TR"),
                candidates = emptyList(),
                observers = emptyList(),
                suspended = emptyList()
            )
        )
    }
}
