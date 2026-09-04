package com.multies.flagquest.data.model

object SpotTheFakeLevelRepository {

    private val all30Levels: List<SpotTheFakeLevelConfig> = listOf(
        // Levels 1-5: Famous Flags, Obvious Errors, 20s
        SpotTheFakeLevelConfig(
            levelIndex = 1,
            titleKey = "spot_fake_level_1_title",
            roundsCount = 5,
            timerSecPerRound = 20,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("DE", "FR", "JP", "CA", "US"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.MISSING_SYMBOL),
            descriptionEn = "Famous European and Western flags with basic color and symbol changes.",
            descriptionAr = "أعلام أوروبية وغربية شهيرة مع تغييرات بسيطة في الألوان والرموز.",
            descriptionDe = "Berühmte europäische und westliche Flaggen mit einfachen Farb- und Symboländerungen.",
            descriptionFr = "Drapeaux européens et occidentaux célèbres avec des changements simples de couleur et de symbole."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 2,
            titleKey = "spot_fake_level_2_title",
            roundsCount = 5,
            timerSecPerRound = 20,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("IT", "BR", "ES", "IN", "IE"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.EXTRA_SYMBOL),
            descriptionEn = "Spot missing symbols and color shifts in world flags.",
            descriptionAr = "اكتشف الرموز المفقودة والتغيرات اللونية في أعلام العالم.",
            descriptionDe = "Erkenne fehlende Symbole und Farbverschiebungen in Weltflaggen.",
            descriptionFr = "Repérez les symboles manquants et les changements de couleur dans les drapeaux du monde."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 3,
            titleKey = "spot_fake_level_3_title",
            roundsCount = 5,
            timerSecPerRound = 20,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("SE", "NO", "DK", "FI", "AT"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.MISSING_SYMBOL),
            descriptionEn = "Nordic and Alpine flags with cross and color alterations.",
            descriptionAr = "أعلام بلدان الشمال والألب مع تعديلات على الصلبان والألوان.",
            descriptionDe = "Nordische und alpine Flaggen mit Kreuz- und Farbänderungen.",
            descriptionFr = "Drapeaux nordiques et alpins avec des modifications de croix et de couleurs."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 4,
            titleKey = "spot_fake_level_4_title",
            roundsCount = 5,
            timerSecPerRound = 20,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("NL", "PL", "TR", "SA", "EG"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.MISSING_SYMBOL, FakeMutationType.SWAP_STRIPE_COLORS),
            descriptionEn = "Middle Eastern and European flag alterations.",
            descriptionAr = "تعديلات على أعلام الشرق الأوسط وأوروبا.",
            descriptionDe = "Veränderungen an mittelöstlichen und europäischen Flaggen.",
            descriptionFr = "Modifications des drapeaux du Moyen-Orient et d'Europe."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 5,
            titleKey = "spot_fake_level_5_title",
            roundsCount = 5,
            timerSecPerRound = 20,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("VN", "CN", "AU", "NZ", "AR"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_STAR_COUNT, FakeMutationType.INCORRECT_COLOR),
            descriptionEn = "Asian and Pacific flags with star count and color changes.",
            descriptionAr = "أعلام آسيا والمحيط الهادئ مع تغييرات في عدد النجوم والألوان.",
            descriptionDe = "Asiatische und pazifische Flaggen mit Änderungen der Sternanzahl und Farben.",
            descriptionFr = "Drapeaux d'Asie et du Pacifique avec des changements de nombre d'étoiles et de couleurs."
        ),

        // Levels 6-10: Stripe-order & Symbol-position errors, 16s
        SpotTheFakeLevelConfig(
            levelIndex = 6,
            titleKey = "spot_fake_level_6_title",
            roundsCount = 7,
            timerSecPerRound = 16,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("DE", "NL", "EG", "AT", "PL", "FR", "IT"),
            allowedMutations = listOf(FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.INCORRECT_STRIPE_ORIENTATION),
            descriptionEn = "Focus on horizontal and vertical stripe order.",
            descriptionAr = "التركيز على ترتيب الأشرطة الأفقية والرأسية.",
            descriptionDe = "Fokus auf die Reihenfolge waagerechter und senkrechter Streifen.",
            descriptionFr = "Focus sur l'ordre des bandes horizontales et verticales."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 7,
            titleKey = "spot_fake_level_7_title",
            roundsCount = 7,
            timerSecPerRound = 16,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("CA", "ES", "IN", "BR", "LB", "GR", "US"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_SYMBOL_POSITION, FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL),
            descriptionEn = "Symbol positions and emblem orientations.",
            descriptionAr = "مواقع الرموز واتجاهات الشعارات.",
            descriptionDe = "Symbolpositionen und Emblem-Ausrichtungen.",
            descriptionFr = "Positions des symboles et orientations des emblèmes."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 8,
            titleKey = "spot_fake_level_8_title",
            roundsCount = 7,
            timerSecPerRound = 16,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("SE", "NO", "DK", "FI", "CH", "JM", "GR"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.INCORRECT_STRIPE_ORIENTATION),
            descriptionEn = "Crosses, saltires, and geometric flag errors.",
            descriptionAr = "أخطاء الصلبان والصلبان المائلة والأعلام الهندسية.",
            descriptionDe = "Fehler bei Kreuzen, Andreaskreuzen und geometrischen Flaggen.",
            descriptionFr = "Erreurs sur les croix, sautoirs et drapeaux géométriques."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 9,
            titleKey = "spot_fake_level_9_title",
            roundsCount = 7,
            timerSecPerRound = 16,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("US", "CN", "AU", "NZ", "VN", "TR", "BR"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_STAR_COUNT, FakeMutationType.EXTRA_SYMBOL),
            descriptionEn = "Star count challenges and added symbols.",
            descriptionAr = "تحديات عدد النجوم والرموز المضافة.",
            descriptionDe = "Herausforderungen bei Sternanzahl und hinzugefügten Symbolen.",
            descriptionFr = "Défis sur le nombre d'étoiles et symboles ajoutés."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 10,
            titleKey = "spot_fake_level_10_title",
            roundsCount = 7,
            timerSecPerRound = 16,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("RO", "TD", "ID", "MC", "IE", "CI", "NL"),
            allowedMutations = listOf(FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.INCORRECT_COLOR, FakeMutationType.SUBTLE_PROPORTION_CHANGE),
            descriptionEn = "Introduction to easily confused twin country flags.",
            descriptionAr = "مقدمة للأعلام المزدوجة المتشابهة لبعض الدول.",
            descriptionDe = "Einführung in leicht verwechselbare Zwillingsflaggen.",
            descriptionFr = "Introduction aux drapeaux jumeaux facilement confondus."
        ),

        // Levels 11-20: Regional themes & subtle fair changes, 12s
        SpotTheFakeLevelConfig(
            levelIndex = 11,
            titleKey = "spot_fake_level_11_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("DE", "FR", "IT", "ES", "NL", "LU", "AT", "PL"),
            allowedMutations = listOf(FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.INCORRECT_COLOR, FakeMutationType.SUBTLE_PROPORTION_CHANGE),
            descriptionEn = "European Tricolors & Proportions.",
            descriptionAr = "الأعلام الأوروبية ثلاثية الألوان والنسب.",
            descriptionDe = "Europäische Trikoloren & Proportionen.",
            descriptionFr = "Tricolores européens et proportions."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 12,
            titleKey = "spot_fake_level_12_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("SA", "EG", "TR", "LB", "IN", "JP", "CN", "VN"),
            allowedMutations = listOf(FakeMutationType.MISSING_SYMBOL, FakeMutationType.INCORRECT_SYMBOL_POSITION, FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL),
            descriptionEn = "Asian & Middle Eastern Crests.",
            descriptionAr = "الشعارات والرموز لآسيا والشرق الأوسط.",
            descriptionDe = "Asiatische & mittelöstliche Wappen.",
            descriptionFr = "Blasons d'Asie et du Moyen-Orient."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 13,
            titleKey = "spot_fake_level_13_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("US", "CA", "BR", "AR", "JM", "ES", "FR", "DE"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_FIELD_OR_BORDER, FakeMutationType.SUBTLE_PROPORTION_CHANGE, FakeMutationType.EXTRA_SYMBOL),
            descriptionEn = "Pan-American & Atlantic flags.",
            descriptionAr = "أعلام الأطلسي والأمريكتين.",
            descriptionDe = "Panamerikanische & atlantische Flaggen.",
            descriptionFr = "Drapeaux panaméricains et atlantiques."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 14,
            titleKey = "spot_fake_level_14_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("SE", "NO", "DK", "FI", "CH", "AT", "GR", "NO"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_COLOR, FakeMutationType.INCORRECT_STRIPE_ORIENTATION, FakeMutationType.SUBTLE_PROPORTION_CHANGE),
            descriptionEn = "Nordic & Alpine Masters.",
            descriptionAr = "خبراء أعلام الشمال والألب.",
            descriptionDe = "Nordische & alpine Meister.",
            descriptionFr = "Maîtres nordiques et alpins."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 15,
            titleKey = "spot_fake_level_15_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("AU", "NZ", "US", "CN", "VN", "BR", "TR", "JP"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_STAR_COUNT, FakeMutationType.INCORRECT_SYMBOL_POSITION, FakeMutationType.MISSING_SYMBOL),
            descriptionEn = "Stars & Constellations.",
            descriptionAr = "النجوم والمجموعات النجمية.",
            descriptionDe = "Sterne & Sternbilder.",
            descriptionFr = "Étoiles et constellations."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 16,
            titleKey = "spot_fake_level_16_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("IE", "CI", "RO", "TD", "ID", "MC", "NL", "LU"),
            allowedMutations = listOf(FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.INCORRECT_COLOR),
            descriptionEn = "Lookalike Twins: Part 1.",
            descriptionAr = "التوائم المتشابهة: الجزء الأول.",
            descriptionDe = "Ähnliche Zwillingsflaggen: Teil 1.",
            descriptionFr = "Jumeaux semblables : Partie 1."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 17,
            titleKey = "spot_fake_level_17_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("EG", "SA", "LB", "TR", "IN", "JP", "VN", "CN"),
            allowedMutations = listOf(FakeMutationType.MIRRORED_ASYMMETRIC_SYMBOL, FakeMutationType.INCORRECT_SYMBOL_POSITION, FakeMutationType.MISSING_SYMBOL),
            descriptionEn = "Orientation & Asymmetry.",
            descriptionAr = "الاتجاه والتناظر للرموز.",
            descriptionDe = "Ausrichtung & Asymmetrie.",
            descriptionFr = "Orientation et dissymétrie."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 18,
            titleKey = "spot_fake_level_18_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("DE", "FR", "IT", "ES", "AT", "PL", "SE", "NO"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_STRIPE_ORIENTATION, FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.SUBTLE_PROPORTION_CHANGE),
            descriptionEn = "Orientation Swaps & Proportion Shifts.",
            descriptionAr = "تبديل الاتجاهات وتغيرات النسب.",
            descriptionDe = "Tausch von Ausrichtungen & Proportionen.",
            descriptionFr = "Inversions d'orientation et proportions."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 19,
            titleKey = "spot_fake_level_19_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf("US", "CA", "BR", "AR", "AU", "NZ", "GR", "JM"),
            allowedMutations = listOf(FakeMutationType.INCORRECT_FIELD_OR_BORDER, FakeMutationType.EXTRA_SYMBOL, FakeMutationType.INCORRECT_STAR_COUNT),
            descriptionEn = "Fields, Cantons & Borders.",
            descriptionAr = "الحقول والخانة العليا والإطارات.",
            descriptionDe = "Felder, Kantone & Ränder.",
            descriptionFr = "Champs, cantons et bordures."
        ),
        SpotTheFakeLevelConfig(
            levelIndex = 20,
            titleKey = "spot_fake_level_20_title",
            roundsCount = 8,
            timerSecPerRound = 12,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B),
            targetCountries = listOf("RO", "TD", "ID", "MC", "IE", "CI", "NL", "LU"),
            allowedMutations = listOf(FakeMutationType.SUBTLE_PROPORTION_CHANGE, FakeMutationType.SWAP_STRIPE_COLORS, FakeMutationType.INCORRECT_COLOR),
            descriptionEn = "Lookalike Twins: Part 2.",
            descriptionAr = "التوائم المتشابهة: الجزء الثاني.",
            descriptionDe = "Ähnliche Zwillingsflaggen: Teil 2.",
            descriptionFr = "Jumeaux semblables : Partie 2."
        )
    )

    // Build levels 21-100 with 10 rounds, 10s timer, mixed mutations
    private val advancedLevels: List<SpotTheFakeLevelConfig> = (21..100).map { lvlIdx ->
        SpotTheFakeLevelConfig(
            levelIndex = lvlIdx,
            titleKey = "spot_fake_level_${lvlIdx}_title",
            roundsCount = 10,
            timerSecPerRound = 10,
            allowedFormats = listOf(GameFormat.FORMAT_A, GameFormat.FORMAT_B, GameFormat.FORMAT_C),
            targetCountries = listOf(
                "RO", "TD", "ID", "MC", "IE", "CI", "NL", "LU", "AU", "NZ",
                "DE", "FR", "IT", "ES", "SE", "NO", "DK", "FI", "US", "CA",
                "JP", "BR", "IN", "TR", "SA", "EG", "AR", "LB", "GR", "JM"
            ).shuffled(),
            allowedMutations = FakeMutationType.values().toList(),
            descriptionEn = "Master Level $lvlIdx: Mixed mutations, strict timer, and twin flag groups.",
            descriptionAr = "مستوى الاحتراف $lvlIdx: تعديلات متنوعة، مؤقت صارم، ومجموعات أعلام متشابهة.",
            descriptionDe = "Meister-Level $lvlIdx: Gemischte Mutationen, strenger Timer und Zwillingsgruppen.",
            descriptionFr = "Niveau Maître $lvlIdx : Mutations mixtes, chrono strict et groupes de drapeaux jumeaux."
        )
    }

    val levels: List<SpotTheFakeLevelConfig> = all30Levels + advancedLevels

    fun getLevel(levelIndex: Int): SpotTheFakeLevelConfig {
        return levels.find { it.levelIndex == levelIndex } ?: levels.first()
    }
}
