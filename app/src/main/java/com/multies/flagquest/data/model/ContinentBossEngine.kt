package com.multies.flagquest.data.model

import com.multies.flagquest.data.repository.CountryMapRepository
import java.util.Random

object ContinentBossEngine {

    val supportedBosses = listOf(
        BossContinent("boss_europe", "Europe", "🇪🇺", 0xFF3F51B5, requiredDiscoveries = 3, requiredStars = 10),
        BossContinent("boss_africa", "Africa", "🌍", 0xFFE65100, requiredDiscoveries = 4, requiredStars = 15),
        BossContinent("boss_asia", "Asia", "🌏", 0xFF8E24AA, requiredDiscoveries = 5, requiredStars = 20),
        BossContinent("boss_north_america", "North America", "🌎", 0xFF1976D2, requiredDiscoveries = 3, requiredStars = 10),
        BossContinent("boss_south_america", "South America", "🌎", 0xFF388E3C, requiredDiscoveries = 3, requiredStars = 12),
        BossContinent("boss_oceania", "Oceania", "🌏", 0xFF0097A7, requiredDiscoveries = 3, requiredStars = 12)
    )

    fun getBossById(bossId: String): BossContinent? {
        return supportedBosses.find { it.bossId.equals(bossId, ignoreCase = true) }
    }

    fun getPhasesForBoss(): List<BossPhaseDefinition> {
        return listOf(
            BossPhaseDefinition(BossPhaseType.FLAG_MASTERY, "boss_phase_flag_title", "boss_phase_flag_desc", questionCount = 4, timerSecondsPerQuestion = 0),
            BossPhaseDefinition(BossPhaseType.MAP_MASTERY, "boss_phase_map_title", "boss_phase_map_desc", questionCount = 4, timerSecondsPerQuestion = 0),
            BossPhaseDefinition(BossPhaseType.CAPITAL_MASTERY, "boss_phase_capital_title", "boss_phase_capital_desc", questionCount = 4, timerSecondsPerQuestion = 0),
            BossPhaseDefinition(BossPhaseType.KNOWLEDGE_MASTERY, "boss_phase_knowledge_title", "boss_phase_knowledge_desc", questionCount = 4, timerSecondsPerQuestion = 0),
            BossPhaseDefinition(BossPhaseType.BORDERS_MASTERY, "boss_phase_borders_title", "boss_phase_borders_desc", questionCount = 4, timerSecondsPerQuestion = 0),
            BossPhaseDefinition(BossPhaseType.SPEED_FINALE, "boss_phase_speed_title", "boss_phase_speed_desc", questionCount = 5, timerSecondsPerQuestion = 15)
        )
    }

    fun generateBossQuestions(
        boss: BossContinent,
        allCountries: List<Country>,
        seed: Long = System.currentTimeMillis()
    ): Map<BossPhaseType, List<BossQuestion>> {
        val rand = Random(seed + boss.bossId.hashCode())
        val continentCountries = allCountries.filter {
            it.continentEn.equals(boss.continentEn, ignoreCase = true)
        }.ifEmpty {
            allCountries.take(8) // Fallback if list is small or filtered
        }

        val result = mutableMapOf<BossPhaseType, List<BossQuestion>>()

        // Phase 1: Flag Mastery (4 questions)
        result[BossPhaseType.FLAG_MASTERY] = generateFlagMasteryQuestions(boss, continentCountries, allCountries, rand)

        // Phase 2: Map Mastery (4 questions)
        result[BossPhaseType.MAP_MASTERY] = generateMapMasteryQuestions(boss, continentCountries, allCountries, rand)

        // Phase 3: Capital Mastery (4 questions)
        result[BossPhaseType.CAPITAL_MASTERY] = generateCapitalMasteryQuestions(boss, continentCountries, allCountries, rand)

        // Phase 4: Knowledge Mastery (4 questions)
        result[BossPhaseType.KNOWLEDGE_MASTERY] = generateKnowledgeMasteryQuestions(boss, continentCountries, allCountries, rand)

        // Phase 5: Borders Mastery (4 questions)
        result[BossPhaseType.BORDERS_MASTERY] = generateBordersMasteryQuestions(boss, continentCountries, allCountries, rand)

        // Phase 6: Speed Finale (5 questions)
        result[BossPhaseType.SPEED_FINALE] = generateSpeedFinaleQuestions(boss, continentCountries, allCountries, rand)

        return result
    }

    private fun generateFlagMasteryQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val shuffled = continentCountries.shuffled(rand)

        for (i in 0 until 4) {
            val target = shuffled[i % shuffled.size]
            val distractors = (continentCountries + allCountries)
                .filter { it.id != target.id }
                .distinctBy { it.id }
                .shuffled(rand)
                .take(3)

            val options = (distractors + target).shuffled(rand)
            val correctIdx = options.indexOf(target)

            questions.add(
                BossQuestion(
                    id = "${boss.bossId}_p1_q${i + 1}",
                    phaseType = BossPhaseType.FLAG_MASTERY,
                    bossId = boss.bossId,
                    countryId = target.id,
                    promptEn = "Which country in ${boss.continentEn} does this flag belong to? ${target.flagEmoji}",
                    promptAr = "إلى أي بلد في ${getArContinent(boss.continentEn)} ينتمي هذا العلم؟ ${target.flagEmoji}",
                    promptDe = "Zu welchem Land in ${getDeContinent(boss.continentEn)} gehört diese Flagge? ${target.flagEmoji}",
                    promptFr = "À quel pays de ${getFrContinent(boss.continentEn)} appartient ce drapeau ? ${target.flagEmoji}",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIdx,
                    visualType = "FLAG",
                    visualData = target.flagEmoji,
                    explanationEn = "${target.nameEn} is located in ${target.continentEn} with capital ${target.capitalEn}.",
                    explanationAr = "تقع ${target.nameAr} في قارة ${target.continentAr} وعاصمتها ${target.capitalAr}.",
                    explanationDe = "${target.nameDe} liegt in ${target.continentDe} mit der Hauptstadt ${target.capitalDe}.",
                    explanationFr = "${target.nameFr} est situé(e) en ${target.continentFr} avec pour capitale ${target.capitalFr}."
                )
            )
        }
        return questions
    }

    private fun generateMapMasteryQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val mapCountries = continentCountries.filter { CountryMapRepository.verifiedCountryCodes.contains(it.id.uppercase()) }
            .ifEmpty { continentCountries }
            .shuffled(rand)

        for (i in 0 until 4) {
            val target = mapCountries[i % mapCountries.size]
            val distractors = continentCountries
                .filter { it.id != target.id }
                .distinctBy { it.id }
                .shuffled(rand)
                .take(3)

            val options = (distractors + target).shuffled(rand)
            val correctIdx = options.indexOf(target)

            questions.add(
                BossQuestion(
                    id = "${boss.bossId}_p2_q${i + 1}",
                    phaseType = BossPhaseType.MAP_MASTERY,
                    bossId = boss.bossId,
                    countryId = target.id,
                    promptEn = "Identify this geographic country map outline from ${boss.continentEn}:",
                    promptAr = "تعرف على الخريطة الجغرافية لهذه الدولة في ${getArContinent(boss.continentEn)}:",
                    promptDe = "Identifizieren Sie diesen geografischen Kartenumriss aus ${getDeContinent(boss.continentEn)}:",
                    promptFr = "Identifiez le contour géographique de ce pays d'Europe/continent (${getFrContinent(boss.continentEn)}) :",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIdx,
                    visualType = "MAP",
                    visualData = target.id,
                    explanationEn = "This is the geographic silhouette map of ${target.nameEn} (${target.id}).",
                    explanationAr = "هذه هي الخريطة الجغرافية لدولة ${target.nameAr} (${target.id}).",
                    explanationDe = "Dies ist der geografische Kartenumriss von ${target.nameDe} (${target.id}).",
                    explanationFr = "Ceci est la carte géographique de ${target.nameFr} (${target.id})."
                )
            )
        }
        return questions
    }

    private fun generateCapitalMasteryQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val shuffled = continentCountries.shuffled(rand)

        for (i in 0 until 4) {
            val target = shuffled[i % shuffled.size]
            val distractors = (continentCountries + allCountries)
                .filter { it.capitalEn != target.capitalEn }
                .distinctBy { it.capitalEn }
                .shuffled(rand)
                .take(3)

            val optionsCapitalsEn = (distractors.map { it.capitalEn } + target.capitalEn).shuffled(rand)
            val correctIdx = optionsCapitalsEn.indexOf(target.capitalEn)

            val optionsCapitalsAr = optionsCapitalsEn.map { capEn ->
                (continentCountries + allCountries).firstOrNull { it.capitalEn == capEn }?.capitalAr ?: capEn
            }
            val optionsCapitalsDe = optionsCapitalsEn.map { capEn ->
                (continentCountries + allCountries).firstOrNull { it.capitalEn == capEn }?.capitalDe ?: capEn
            }
            val optionsCapitalsFr = optionsCapitalsEn.map { capEn ->
                (continentCountries + allCountries).firstOrNull { it.capitalEn == capEn }?.capitalFr ?: capEn
            }

            questions.add(
                BossQuestion(
                    id = "${boss.bossId}_p3_q${i + 1}",
                    phaseType = BossPhaseType.CAPITAL_MASTERY,
                    bossId = boss.bossId,
                    countryId = target.id,
                    promptEn = "What is the capital city of ${target.nameEn} ${target.flagEmoji}?",
                    promptAr = "ما هي عاصمة ${target.nameAr} ${target.flagEmoji}؟",
                    promptDe = "Was ist die Hauptstadt von ${target.nameDe} ${target.flagEmoji}?",
                    promptFr = "Quelle est la capitale de ${target.nameFr} ${target.flagEmoji} ?",
                    optionsEn = optionsCapitalsEn,
                    optionsAr = optionsCapitalsAr,
                    optionsDe = optionsCapitalsDe,
                    optionsFr = optionsCapitalsFr,
                    correctOptionIndex = correctIdx,
                    visualType = "CAPITAL_BADGE",
                    visualData = target.flagEmoji,
                    explanationEn = "${target.capitalEn} is the official capital city of ${target.nameEn}.",
                    explanationAr = "مدينة ${target.capitalAr} هي العاصمة الرسمية لدولة ${target.nameAr}.",
                    explanationDe = "${target.capitalDe} ist die offizielle Hauptstadt von ${target.nameDe}.",
                    explanationFr = "${target.capitalFr} est la capitale officielle de ${target.nameFr}."
                )
            )
        }
        return questions
    }

    private fun generateKnowledgeMasteryQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val shuffled = continentCountries.shuffled(rand)

        for (i in 0 until 4) {
            val target = shuffled[i % shuffled.size]
            val type = i % 4
            when (type) {
                0 -> {
                    // Currency question
                    val distractors = (continentCountries + allCountries)
                        .filter { it.currencyEn != target.currencyEn }
                        .distinctBy { it.currencyEn }
                        .shuffled(rand)
                        .take(3)

                    val optsEn = (distractors.map { it.currencyEn } + target.currencyEn).shuffled(rand)
                    val correctIdx = optsEn.indexOf(target.currencyEn)

                    val optsAr = optsEn.map { cEn -> (continentCountries + allCountries).firstOrNull { it.currencyEn == cEn }?.currencyAr ?: cEn }
                    val optsDe = optsEn.map { cEn -> (continentCountries + allCountries).firstOrNull { it.currencyEn == cEn }?.currencyDe ?: cEn }
                    val optsFr = optsEn.map { cEn -> (continentCountries + allCountries).firstOrNull { it.currencyEn == cEn }?.currencyFr ?: cEn }

                    questions.add(
                        BossQuestion(
                            id = "${boss.bossId}_p4_q${i + 1}",
                            phaseType = BossPhaseType.KNOWLEDGE_MASTERY,
                            bossId = boss.bossId,
                            countryId = target.id,
                            promptEn = "Which currency is officially used in ${target.nameEn} ${target.flagEmoji}?",
                            promptAr = "ما هي العملة الرسمية المستعملة في ${target.nameAr} ${target.flagEmoji}؟",
                            promptDe = "Welche Währung wird offiziell in ${target.nameDe} ${target.flagEmoji} verwendet?",
                            promptFr = "Quelle monnaie est officiellement utilisée en ${target.nameFr} ${target.flagEmoji} ?",
                            optionsEn = optsEn,
                            optionsAr = optsAr,
                            optionsDe = optsDe,
                            optionsFr = optsFr,
                            correctOptionIndex = correctIdx,
                            visualType = "FACT_CARD",
                            visualData = target.flagEmoji,
                            explanationEn = "The official currency of ${target.nameEn} is the ${target.currencyEn}.",
                            explanationAr = "العملة الرسمية لـ ${target.nameAr} هي ${target.currencyAr}.",
                            explanationDe = "Die offizielle Währung von ${target.nameDe} ist der/die ${target.currencyDe}.",
                            explanationFr = "La monnaie officielle de ${target.nameFr} est le/la ${target.currencyFr}."
                        )
                    )
                }
                1 -> {
                    // Area question
                    val distractors = continentCountries
                        .filter { it.id != target.id }
                        .shuffled(rand)
                        .take(3)

                    val options = (distractors + target).shuffled(rand)
                    val correctIdx = options.indexOf(target)

                    questions.add(
                        BossQuestion(
                            id = "${boss.bossId}_p4_q${i + 1}",
                            phaseType = BossPhaseType.KNOWLEDGE_MASTERY,
                            bossId = boss.bossId,
                            countryId = target.id,
                            promptEn = "Which of these countries in ${boss.continentEn} has a total area of approx. ${target.areaSqKm.toInt()} km²?",
                            promptAr = "أيّ من هذه الدول في ${getArContinent(boss.continentEn)} تبلغ مساحتها الإجمالية حوالي ${target.areaSqKm.toInt()} كم²؟",
                            promptDe = "Welches dieser Länder in ${getDeContinent(boss.continentEn)} hat eine Gesamtfläche von ca. ${target.areaSqKm.toInt()} km²?",
                            promptFr = "Lequel de ces pays de ${getFrContinent(boss.continentEn)} a une superficie totale d'environ ${target.areaSqKm.toInt()} km² ?",
                            optionsEn = options.map { it.nameEn },
                            optionsAr = options.map { it.nameAr },
                            optionsDe = options.map { it.nameDe },
                            optionsFr = options.map { it.nameFr },
                            correctOptionIndex = correctIdx,
                            visualType = "FACT_CARD",
                            visualData = target.flagEmoji,
                            explanationEn = "${target.nameEn} covers ${target.areaSqKm.toInt()} km² according to official statistics.",
                            explanationAr = "تبلغ مساحة ${target.nameAr} حوالي ${target.areaSqKm.toInt()} كم² وفقاً للإحصاءات الرسمية.",
                            explanationDe = "${target.nameDe} umfasst laut offizieller Statistik ${target.areaSqKm.toInt()} km².",
                            explanationFr = "${target.nameFr} couvre ${target.areaSqKm.toInt()} km² selon les statistiques officielles."
                        )
                    )
                }
                2 -> {
                    // Fact Question
                    val distractors = continentCountries
                        .filter { it.id != target.id }
                        .shuffled(rand)
                        .take(3)

                    val options = (distractors + target).shuffled(rand)
                    val correctIdx = options.indexOf(target)

                    questions.add(
                        BossQuestion(
                            id = "${boss.bossId}_p4_q${i + 1}",
                            phaseType = BossPhaseType.KNOWLEDGE_MASTERY,
                            bossId = boss.bossId,
                            countryId = target.id,
                            promptEn = "Geographic Fact: \"${target.funFactEn}\" — Which country is this?",
                            promptAr = "حقيقة جغرافية: \"${target.funFactAr}\" — أي بلد هذا؟",
                            promptDe = "Geografischer Fakt: „${target.funFactDe}“ — Welches Land ist das?",
                            promptFr = "Fait géographique : « ${target.funFactFr} » — De quel pays s'agit-il ?",
                            optionsEn = options.map { it.nameEn },
                            optionsAr = options.map { it.nameAr },
                            optionsDe = options.map { it.nameDe },
                            optionsFr = options.map { it.nameFr },
                            correctOptionIndex = correctIdx,
                            visualType = "FACT_CARD",
                            visualData = target.flagEmoji,
                            explanationEn = target.funFactEn,
                            explanationAr = target.funFactAr,
                            explanationDe = target.funFactDe,
                            explanationFr = target.funFactFr
                        )
                    )
                }
                else -> {
                    // Languages Question
                    val distractors = continentCountries
                        .filter { it.id != target.id && it.languagesEn != target.languagesEn }
                        .shuffled(rand)
                        .take(3)

                    val options = (distractors + target).shuffled(rand)
                    val correctIdx = options.indexOf(target)

                    questions.add(
                        BossQuestion(
                            id = "${boss.bossId}_p4_q${i + 1}",
                            phaseType = BossPhaseType.KNOWLEDGE_MASTERY,
                            bossId = boss.bossId,
                            countryId = target.id,
                            promptEn = "In which country is ${target.languagesEn} spoken as a primary/official language?",
                            promptAr = "في أي دولة يُتحدث بـ ${target.languagesAr} كلغة رسمية/رئيسية؟",
                            promptDe = "In welchem Land wird ${target.languagesDe} als Haupt-/Amtssprache gesprochen?",
                            promptFr = "Dans quel pays le/l' ${target.languagesFr} est-il parlé comme langue officielle ?",
                            optionsEn = options.map { it.nameEn },
                            optionsAr = options.map { it.nameAr },
                            optionsDe = options.map { it.nameDe },
                            optionsFr = options.map { it.nameFr },
                            correctOptionIndex = correctIdx,
                            visualType = "FACT_CARD",
                            visualData = target.flagEmoji,
                            explanationEn = "Primary languages in ${target.nameEn}: ${target.languagesEn}.",
                            explanationAr = "اللغات الرئيسية في ${target.nameAr}: ${target.languagesAr}.",
                            explanationDe = "Hauptsprachen in ${target.nameDe}: ${target.languagesDe}.",
                            explanationFr = "Langues principales en ${target.nameFr} : ${target.languagesFr}."
                        )
                    )
                }
            }
        }
        return questions
    }

    private fun generateBordersMasteryQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val shuffled = continentCountries.shuffled(rand)

        for (i in 0 until 4) {
            val target = shuffled[i % shuffled.size]
            val distractors = continentCountries
                .filter { it.id != target.id }
                .shuffled(rand)
                .take(3)

            val options = (distractors + target).shuffled(rand)
            val correctIdx = options.indexOf(target)

            questions.add(
                BossQuestion(
                    id = "${boss.bossId}_p5_q${i + 1}",
                    phaseType = BossPhaseType.BORDERS_MASTERY,
                    bossId = boss.bossId,
                    countryId = target.id,
                    promptEn = "Which country in ${boss.continentEn} has the following neighboring land borders: ${target.neighborsEn}?",
                    promptAr = "أي دولة في ${getArContinent(boss.continentEn)} تتمتع بالحدود البرية المجاورة التالية: ${target.neighborsAr}؟",
                    promptDe = "Welches Land in ${getDeContinent(boss.continentEn)} hat folgende Landgrenzen: ${target.neighborsDe}?",
                    promptFr = "Lequel de ces pays de ${getFrContinent(boss.continentEn)} possède les frontières terrestres suivantes : ${target.neighborsFr} ?",
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIdx,
                    visualType = "FACT_CARD",
                    visualData = target.flagEmoji,
                    explanationEn = "Land neighbors of ${target.nameEn}: ${target.neighborsEn}.",
                    explanationAr = "الحدود المجاورة لـ ${target.nameAr}: ${target.neighborsAr}.",
                    explanationDe = "Landgrenzen von ${target.nameDe}: ${target.neighborsDe}.",
                    explanationFr = "Frontières terrestres de ${target.nameFr} : ${target.neighborsFr}."
                )
            )
        }
        return questions
    }

    private fun generateSpeedFinaleQuestions(
        boss: BossContinent,
        continentCountries: List<Country>,
        allCountries: List<Country>,
        rand: Random
    ): List<BossQuestion> {
        val questions = mutableListOf<BossQuestion>()
        val shuffled = continentCountries.shuffled(rand)

        for (i in 0 until 5) {
            val target = shuffled[i % shuffled.size]
            val distractors = continentCountries
                .filter { it.id != target.id }
                .shuffled(rand)
                .take(3)

            val options = (distractors + target).shuffled(rand)
            val correctIdx = options.indexOf(target)

            val promptEn = when (i % 3) {
                0 -> "SPEED FINALE: Identify the country for flag ${target.flagEmoji}!"
                1 -> "SPEED FINALE: Capital is ${target.capitalEn}. Which country?"
                else -> "SPEED FINALE: Country with area ${target.areaSqKm.toInt()} km²?"
            }
            val promptAr = when (i % 3) {
                0 -> "المرحلة السريعة: تعرف على الدولة من العلم ${target.flagEmoji}!"
                1 -> "المرحلة السريعة: العاصمة هي ${target.capitalAr}. أي دولة؟"
                else -> "المرحلة السريعة: دولة بمساحة ${target.areaSqKm.toInt()} كم²؟"
            }
            val promptDe = when (i % 3) {
                0 -> "SCHNELLER FINALE: Identifizieren Sie das Land für Flagge ${target.flagEmoji}!"
                1 -> "SCHNELLER FINALE: Hauptstadt ist ${target.capitalDe}. Welches Land?"
                else -> "SCHNELLER FINALE: Land mit einer Fläche von ${target.areaSqKm.toInt()} km²?"
            }
            val promptFr = when (i % 3) {
                0 -> "FINALE RAPIDE : Identifiez le pays du drapeau ${target.flagEmoji} !"
                1 -> "FINALE RAPIDE : La capitale est ${target.capitalFr}. Quel pays ?"
                else -> "FINALE RAPIDE : Pays ayant une superficie de ${target.areaSqKm.toInt()} km² ?"
            }

            questions.add(
                BossQuestion(
                    id = "${boss.bossId}_p6_q${i + 1}",
                    phaseType = BossPhaseType.SPEED_FINALE,
                    bossId = boss.bossId,
                    countryId = target.id,
                    promptEn = promptEn,
                    promptAr = promptAr,
                    promptDe = promptDe,
                    promptFr = promptFr,
                    optionsEn = options.map { it.nameEn },
                    optionsAr = options.map { it.nameAr },
                    optionsDe = options.map { it.nameDe },
                    optionsFr = options.map { it.nameFr },
                    correctOptionIndex = correctIdx,
                    visualType = if (i % 2 == 0) "FLAG" else "CAPITAL_BADGE",
                    visualData = target.flagEmoji,
                    explanationEn = "Correct answer: ${target.nameEn}.",
                    explanationAr = "الإجابة الصحيحة: ${target.nameAr}.",
                    explanationDe = "Richtige Antwort: ${target.nameDe}.",
                    explanationFr = "Bonne réponse : ${target.nameFr}."
                )
            )
        }
        return questions
    }

    private fun getArContinent(en: String): String = when (en.lowercase()) {
        "europe" -> "أوروبا"
        "africa" -> "أفريقيا"
        "asia" -> "آسيا"
        "north america" -> "أمريكا الشمالية"
        "south america" -> "أمريكا الجنوبية"
        "oceania" -> "أوقيانوسيا"
        else -> en
    }

    private fun getDeContinent(en: String): String = when (en.lowercase()) {
        "europe" -> "Europa"
        "africa" -> "Afrika"
        "asia" -> "Asien"
        "north america" -> "Nordamerika"
        "south america" -> "Südamerika"
        "oceania" -> "Ozeanien"
        else -> en
    }

    private fun getFrContinent(en: String): String = when (en.lowercase()) {
        "europe" -> "l'Europe"
        "africa" -> "l'Afrique"
        "asia" -> "l'Asie"
        "north america" -> "l'Amérique du Nord"
        "south america" -> "l'Amérique du Sud"
        "oceania" -> "l'Océanie"
        else -> en
    }
}
