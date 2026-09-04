package com.multies.flagquest.features.whoami.engine

import com.multies.flagquest.data.model.Country
import kotlin.math.roundToInt
import kotlin.random.Random

object WhoAmIEngine {

    // Curated rich clues for all 36 core countries in countries.json
    private val CURATED_CLUES = mapOf(
        "US" to listOf(
            WhoAmIClue("US_1", "I am a nation in North America bounded by the Atlantic and Pacific Oceans.", "أنا دولة تقع في أمريكا الشمالية ويحدني المحيطين الأطلسي والهادئ.", "Ich bin eine Nation in Nordamerika, die vom Atlantischen und Pazifischen Ozean begrenzt wird.", "Je suis une nation d'Amérique du Nord bordée par l'océan Atlantique et l'océan Pacifique."),
            WhoAmIClue("US_2", "My territory includes 50 federal states and the Grand Canyon.", "يتكون إقليمي الفيدرالي من 50 ولاية ويضم الأخدود العظيم.", "Mein Territorium umfasst 50 Bundesstaaten und den Grand Canyon.", "Mon territoire comprend 50 États fédéraux et le Grand Canyon."),
            WhoAmIClue("US_3", "My official currency is the US Dollar ($).", "عملتي الرسمية هي الدولار الأمريكي ($).", "Meine offizielle Währung ist der US-Dollar ($).", "Ma monnaie officielle est le dollar américain ($)."),
            WhoAmIClue("US_4", "My flag features 50 white stars and 13 red and white stripes.", "يتكون علمي من 50 نجمة بيضاء و13 خطاً أحمر وأبيض.", "Meine Flagge hat 50 weiße Sterne und 13 rote und weiße Streifen.", "Mon drapeau comporte 50 étoiles blanches et 13 bandes rouges et blanches."),
            WhoAmIClue("US_5", "My federal capital city is Washington, D.C.", "عاصمتي الفيدرالية هي واشنطن العاصمة.", "Meine Bundeshauptstadt ist Washington, D.C.", "Ma capitale fédérale est Washington, D.C.")
        ),
        "SA" to listOf(
            WhoAmIClue("SA_1", "I am situated in Western Asia on the Arabian Peninsula.", "أقع في غرب آسيا على شبه الجزيرة العربية.", "Ich liege in Westasien auf der Arabischen Halbinsel.", "Je suis situé en Asie de l'Ouest sur la péninsule Arabique."),
            WhoAmIClue("SA_2", "I am bordered by the Red Sea to the west and Persian Gulf to the east.", "يحدني البحر الأحمر غرباً والخليج العربي شرقاً.", "Ich grenze im Westen an das Rote Meer und im Osten an den Persischen Golf.", "Je suis bordé par la mer Rouge à l'ouest et le golfe Persique à l'est."),
            WhoAmIClue("SA_3", "My official currency is the Saudi Riyal (SAR).", "عملتي الرسمية هي الريال السعودي.", "Meine offizielle Währung ist der Saudi-Rial.", "Ma monnaie officielle est le riyal saoudien."),
            WhoAmIClue("SA_4", "My national flag features a green field with white Arabic script and a sword.", "علمي الوطني أخضر اللون ويحمل الشهادة بالخط العربي والسيف.", "Meine Nationalflagge zeigt ein grünes Feld mit weißer arabischer Schrift und einem Schwert.", "Mon drapeau national est vert avec de la calligraphie arabe blanche et un sabre."),
            WhoAmIClue("SA_5", "My capital city is Riyadh.", "عاصمتي هي الرياض.", "Meine Hauptstadt ist Riad.", "Ma capitale est Riyad.")
        ),
        "FR" to listOf(
            WhoAmIClue("FR_1", "I am located in Western Europe and bordered by the Mediterranean Sea and Atlantic Ocean.", "أقع في غرب أوروبا ويحدني البحر الأبيض المتوسط والمحيط الأطلسي.", "Ich liege in Westeuropa und grenze an das Mittelmeer und den Atlantischen Ozean.", "Je suis situé en Europe occidentale et bordé par la Méditerranée et l'Atlantique."),
            WhoAmIClue("FR_2", "I am a founding member of the European Union.", "أنا دولة مؤسسة للاتحاد الأوروبي.", "Ich bin ein Gründungsmitglied der Europäischen Union.", "Je suis un membre fondateur de l'Union européenne."),
            WhoAmIClue("FR_3", "My official language is French and my currency is the Euro.", "لغتي الرسمية هي الفرنسية وعملتي هي اليورو.", "Meine Amtssprache ist Französisch und meine Währung ist der Euro.", "Ma langue officielle est le français et ma monnaie est l'Euro."),
            WhoAmIClue("FR_4", "The Eiffel Tower and Louvre Museum are iconic landmarks in my country.", "برج إيفل ومتحف اللوفر هما معلمان بارزان في بلادي.", "Der Eiffelturm und das Louvre-Museum sind Wahrzeichen meines Landes.", "La tour Eiffel et le musée du Louvre sont des monuments emblématiques de mon pays."),
            WhoAmIClue("FR_5", "My capital city is Paris.", "عاصمتي هي باريس.", "Meine Hauptstadt ist Paris.", "Ma capitale est Paris.")
        ),
        "DE" to listOf(
            WhoAmIClue("DE_1", "I am located in Central Europe, sharing borders with 9 other countries.", "أقع في وسط أوروبا وتشترك حدودي مع 9 دول أخرى.", "Ich liege in Mitteleuropa und teile Grenzen mit 9 Nachbarländern.", "Je suis situé en Europe centrale et partage des frontières avec 9 pays."),
            WhoAmIClue("DE_2", "I am the most populous member state of the European Union.", "أنا أكثر دول الاتحاد الأوروبي سكاناً.", "Ich bin der bevölkerungsreichste Mitgliedstaat der Europäischen Union.", "Je suis l'État membre le plus peuplé de l'Union européenne."),
            WhoAmIClue("DE_3", "My flag is composed of three equal horizontal bands of black, red, and gold.", "علمي يتكون من ثلاثة أشرطة أفقية متساوية بالألوان الأسود والأحمر والذهبي.", "Meine Flagge besteht aus drei gleichen horizontalen Streifen in Schwarz, Rot und Gold.", "Mon drapeau est composé de trois bandes horizontales noire, rouge et d'or."),
            WhoAmIClue("DE_4", "The Brandenburg Gate and Neuschwanstein Castle are world-famous landmarks here.", "بوابة براندنبورغ وقصر نويشفانشتاين من أشهر معالم بلادي.", "Das Brandenburger Tor und Schloss Neuschwanstein sind weltberühmte Wahrzeichen.", "La porte de Brandebourg et le château de Neuschwanstein sont célèbres ici."),
            WhoAmIClue("DE_5", "My capital city is Berlin.", "عاصمتي هي برلين.", "Meine Hauptstadt ist Berlin.", "Ma capitale est Berlin.")
        ),
        "JP" to listOf(
            WhoAmIClue("JP_1", "I am an island nation in East Asia located in the Pacific Ocean.", "أنا دولة جزرية في شرق آسيا تقع في المحيط الهادئ.", "Ich bin ein Inselstaat in Ostasien im Pazifischen Ozean.", "Je suis une nation insulaire d'Asie de l'Est située dans l'océan Pacifique."),
            WhoAmIClue("JP_2", "My archipelago consists of nearly 7,000 islands, with four main islands.", "يتكون أرخبيلي من حوالي 7000 جزيرة، بينها أربع جزر رئيسية.", "Mein Archipel besteht aus fast 7.000 Inseln mit vier Hauptinseln.", "Mon archipel se compose de près de 7 000 îles, dont quatre principales."),
            WhoAmIClue("JP_3", "Mount Fuji is the highest mountain peak in my country.", "جبل فوجي هو أعلى قمة جبلية في بلادي.", "Der Mount Fuji ist der höchste Berg meines Landes.", "Le mont Fuji est le plus haut sommet de mon pays."),
            WhoAmIClue("JP_4", "My flag features a red circle representing the rising sun on a white background.", "يحمل علمي قرصاً أحمر يمثل الشمس المشرقة على خلفية بيضاء.", "Meine Flagge zeigt einen roten Kreis für die aufgehende Sonne auf weißem Grund.", "Mon drapeau comporte un cercle rouge représentant le soleil levant sur fond blanc."),
            WhoAmIClue("JP_5", "My capital city is Tokyo.", "عاصمتي هي طوكيو.", "Meine Hauptstadt ist Tokio.", "Ma capitale est Tokyo.")
        ),
        "DZ" to listOf(
            WhoAmIClue("DZ_1", "I am located in North Africa along the Mediterranean coastline.", "أقع في شمال إفريقيا على ساحل البحر الأبيض المتوسط.", "Ich liege in Nordafrika an der Mittelmeerküste.", "Je suis situé en Afrique du Nord le long de la côte méditerranéenne."),
            WhoAmIClue("DZ_2", "I am the largest country by land area in Africa.", "أنا أكبر دولة مساحةً في قارة إفريقيا.", "Ich bin flächenmäßig das größte Land Afrikas.", "Je suis le plus grand pays d'Afrique par sa superficie."),
            WhoAmIClue("DZ_3", "More than 80% of my territory is covered by the Sahara Desert.", "أكثر من 80% من مساحتي تغطيها الصحراء الكبرى.", "Mehr als 80% meines Territoriums sind von der Sahara bedeckt.", "Plus de 80% de mon territoire est recouvert par le désert du Sahara."),
            WhoAmIClue("DZ_4", "My capital city shares its name with my nation.", "عاصمتي تشترك معي في نفس الاسم.", "Meine Hauptstadt trägt denselben Namen wie mein Land.", "Ma capitale porte le même nom que mon pays."),
            WhoAmIClue("DZ_5", "My capital is Algiers.", "عاصمتي هي الجزائر.", "Meine Hauptstadt ist Algier.", "Ma capitale est Alger.")
        ),
        "EG" to listOf(
            WhoAmIClue("EG_1", "I am a transcontinental nation linking northeast Africa with the Sinai Peninsula.", "أنا دولة عابرة للقارات أربط شمال شرق إفريقيا بشبه جزيرة سيناء.", "Ich bin eine transkontinentale Nation, die Nordostafrika mit der Sinai-Halbinsel verbindet.", "Je suis une nation transcontinentale reliant l'Afrique du Nord-Est à la péninsule du Sinaï."),
            WhoAmIClue("EG_2", "The Great Nile River flows through the entire length of my country.", "يمر نهر النيل العظيم عبر بطول بلادي كاملاً.", "Der große Nil fließt durch die gesamte Länge meines Landes.", "Le grand fleuve Nil traverse toute la longueur de mon pays."),
            WhoAmIClue("EG_3", "The Great Pyramids of Giza and Sphinx are world-renowned monuments here.", "أهرامات الجيزة والأهرامات وأبو الهول من أشهر الآثار العالمية في بلادي.", "Die Pyramiden von Gizeh und die Sphinx sind weltberühmte Denkmäler hier.", "Les grandes pyramides de Gizeh et le Sphinx sont des monuments célèbres ici."),
            WhoAmIClue("EG_4", "My flag features black, white, and red bands with the Golden Eagle of Saladin.", "يحمل علمي شريطاً أحمر وأبيض وأسود مع نسر صلاح الدين الذهبي.", "Meine Flagge zeigt schwarze, weiße und rote Streifen mit dem goldenen Adler Saladins.", "Mon drapeau comporte des bandes rouge, blanche et noire avec l'Aigle de Saladin."),
            WhoAmIClue("EG_5", "My capital city is Cairo.", "عاصمتي هي القاهرة.", "Meine Hauptstadt ist Kairo.", "Ma capitale est Le Caire.")
        ),
        "BR" to listOf(
            WhoAmIClue("BR_1", "I am the largest country in South America and Latin America.", "أنا أكبر دولة في أمريكا الجنوبية واللاتينية مساحةً ورقعة.", "Ich bin das größte Land in Südamerika und Lateinamerika.", "Je suis le plus grand pays d'Amérique du Sud et d'Amérique latine."),
            WhoAmIClue("BR_2", "I contain the vast majority of the Amazon Rainforest and Amazon River basin.", "أضم الجزء الأكبر من غابات الأمازون المطيرة وحوض نهر الأمازون.", "Ich enthalte den größten Teil des Amazonasmischwalds und Amazonasbeckens.", "Je abrite la majeure partie de la forêt amazonienne et du bassin de l'Amazone."),
            WhoAmIClue("BR_3", "I am the only Portuguese-speaking nation in the Americas.", "أنا الدولة الوحيدة الناطقة باللغة البرتغالية في الأمريكتين.", "Ich bin das einzige portugiesischsprachige Land in Amerika.", "Je suis le seul pays lusophone des Amériques."),
            WhoAmIClue("BR_4", "My green and yellow flag features a blue celestial globe with 27 stars.", "يتألف علمي الأخضر والأصفر من قرص سماوي أزرق يضم 27 نجمة.", "Meine grün-gelbe Flagge zeigt eine blaue Himmelskugel mit 27 Sternen.", "Mon drapeau vert et jaune comporte un globe céleste bleu avec 27 étoiles."),
            WhoAmIClue("BR_5", "My capital city is Brasília.", "عاصمتي هي برازيليا.", "Meine Hauptstadt ist Brasília.", "Ma capitale est Brasília.")
        ),
        "IN" to listOf(
            WhoAmIClue("IN_1", "I am located in South Asia and bounded by the Indian Ocean.", "أقع في جنوب آسيا ويحدني المحيط الهندي.", "Ich liege in Südasien und werde vom Indischen Ozean begrenzt.", "Je suis situé en Asie du Sud et bordé par l'océan Indien."),
            WhoAmIClue("IN_2", "I am the most populous country in the world.", "أنا الدولة الأكثر سكاناً في العالم.", "Ich bin das bevölkerungsreichste Land der Welt.", "Je suis le pays le plus peuplé du monde."),
            WhoAmIClue("IN_3", "The iconic Taj Mahal is located in my city of Agra.", "يقع معبد تاج محل الشهير في مدينة أذرة ببلادي.", "Das berühmte Taj Mahal befindet sich in meiner Stadt Agra.", "L'emblématique Taj Mahal est situé dans ma ville d'Agra."),
            WhoAmIClue("IN_4", "My tricolor flag features a navy blue Ashoka Chakra wheel in the center.", "يحمل علمي المثلث الألوان عجلة أشوكا تشاكرا الكحلية في المنتصف.", "Meine Trikolore zeigt ein dunkelblaues Ashoka Chakra Rad in der Mitte.", "Mon drapeau tricolore comporte la roue d'Ashoka Chakra au centre."),
            WhoAmIClue("IN_5", "My capital city is New Delhi.", "عاصمتي هي نيودلهي.", "Meine Hauptstadt ist Neu-Delhi.", "Ma capitale est New Delhi.")
        ),
        "CN" to listOf(
            WhoAmIClue("CN_1", "I am located in East Asia and border 14 different countries.", "أقع في شرق آسيا وأشترك في الحدود مع 14 دولة.", "Ich liege in Ostasien und grenze an 14 verschiedene Länder.", "Je suis situé en Asie de l'Est et borde 14 pays différents."),
            WhoAmIClue("CN_2", "The Great Wall, stretching thousands of kilometers, was built across my northern border.", "سور الصين العظيم يمتد آلاف الكيلومترات عبر حدودي الشمالية.", "Die Große Mauer erstreckt sich über Tausende von Kilometern an meiner Nordgrenze.", "La Grande Muraille s'étend sur des milliers de kilomètres sur ma frontière nord."),
            WhoAmIClue("CN_3", "My official language is Mandarin Chinese.", "لغتي الرسمية هي الصينية الماندرين.", "Meine Amtssprache ist Hochchinesisch (Mandarin).", "Ma langue officielle est le chinois mandarin."),
            WhoAmIClue("CN_4", "My red national flag features five golden stars in the upper canton.", "يحمل علمي الأحمر الوطني خمس نجوم ذهبية في الزاوية العلوية.", "Meine rote Nationalflagge zeigt fünf goldene Sterne in der oberen Ecke.", "Mon drapeau national rouge comporte cinq étoiles d'or dans le canton supérieur."),
            WhoAmIClue("CN_5", "My capital city is Beijing.", "عاصمتي هي بكين.", "Meine Hauptstadt ist Peking.", "Ma capitale est Pékin.")
        ),
        "GB" to listOf(
            WhoAmIClue("GB_1", "I am an island country off the northwestern coast of mainland Europe.", "أنا دولة جزرية تقع قبالة الساحل الشمالي الغربي لأوروبا.", "Ich bin ein Inselstaat vor der Nordwestküste des europäischen Festlands.", "Je suis un pays insulaire situé au large de la côte nord-ouest de l'Europe."),
            WhoAmIClue("GB_2", "I am made up of four constituent countries: England, Scotland, Wales, and Northern Ireland.", "أتكون من أربع دول متحدة: إنجلترا، إسكتلندا، ويلز، وإيرلندا الشمالية.", "Ich bestehe aus vier Landesteilen: England, Schottland, Wales und Nordirland.", "Je suis composé de quatre pays constitutifs : l'Angleterre, l'Écosse, le Pays de Galles et l'Irlande du Nord."),
            WhoAmIClue("GB_3", "Big Ben, Stonehenge, and Buckingham Palace are famous landmarks here.", "ساعة بيغ بن، وستونهنج، وبقصر باكنغهام من أشهر المعالم في بلادي.", "Big Ben, Stonehenge und der Buckingham Palace sind berühmte Wahrzeichen.", "Big Ben, Stonehenge et le palais de Buckingham sont des monuments célèbres ici."),
            WhoAmIClue("GB_4", "My national flag is known as the Union Jack.", "يعرف علمي الوطني باسم علم الاتحاد (Union Jack).", "Meine Nationalflagge ist als Union Jack bekannt.", "Mon drapeau national est connu sous le nom de Union Jack."),
            WhoAmIClue("GB_5", "My capital city is London.", "عاصمتي هي لندن.", "Meine Hauptstadt ist London.", "Ma capitale est Londres.")
        ),
        "CA" to listOf(
            WhoAmIClue("CA_1", "I am located in northern North America, extending from the Atlantic to the Pacific.", "أقع في شمال أمريكا الشمالية وأمتد من المحيط الأطلسي إلى المحيط الهادئ.", "Ich liege im nördlichen Nordamerika und reiche vom Atlantik bis zum Pazifik.", "Je suis situé dans le nord de l'Amérique du Nord, s'étendant de l'Atlantique au Pacifique."),
            WhoAmIClue("CA_2", "I am the second-largest country in the world by total land area.", "أنا ثاني أكبر دولة في العالم من حيث المساحة الإجمالية.", "Ich bin das zweitgrößte Land der Welt nach Gesamtfläche.", "Je suis le deuxième plus grand pays du monde par sa superficie totale."),
            WhoAmIClue("CA_3", "My country officially uses both English and French as national languages.", "تستخدم بلادي رسمياً كلاً من اللغتين الإنجليزية والفرنسية.", "Mein Land nutzt offiziell sowohl Englisch als auch Französisch.", "Mon pays utilise officiellement l'anglais et le français."),
            WhoAmIClue("CA_4", "My flag features a stylized red maple leaf at its center.", "يحمل علمي ورقة شجر شجر القيقب الحمراء المصممة في المنتصف.", "Meine Flagge zeigt ein stilisiertes rotes Ahornblatt in der Mitte.", "Mon drapeau comporte une feuille d'érable rouge stylisée au centre."),
            WhoAmIClue("CA_5", "My capital city is Ottawa.", "عاصمتي هي أوتاوا.", "Meine Hauptstadt ist Ottawa.", "Ma capitale est Ottawa.")
        ),
        "AU" to listOf(
            WhoAmIClue("AU_1", "I am both a sovereign country and the world's smallest continent.", "أنا دولة سيادية وأصغر قارة في العالم في نفس الوقت.", "Ich bin sowohl ein souveränes Land als auch der kleinste Kontinent der Welt.", "Je suis à la fois un pays souverain et le plus petit continent du monde."),
            WhoAmIClue("AU_2", "I am home to the Great Barrier Reef and native kangaroos.", "أنا موطن الحجز المرجاني العظيم وحيوان الكنغر الشهير.", "Ich bin die Heimat des Great Barrier Reefs und einheimischer Kängurus.", "Je abrite la Grande Barrière de Corail et les kangourous indigènes."),
            WhoAmIClue("AU_3", "My country is entirely surrounded by the Indian and Pacific Oceans.", "تحيط ببلادي المحيط الهندي والمحيط الهادئ بالكامل.", "Mein Land ist vollständig vom Indischen und Pazifischen Ozean umgeben.", "Mon pays est entièrement entouré par l'océan Indien et l'océan Pacifique."),
            WhoAmIClue("AU_4", "The iconic Sydney Opera House is located in my largest city.", "توجد دار أوبرا سيدني الشهيرة في أكبر مدني.", "Das ikonische Sydney Opera House befindet sich in meiner größten Stadt.", "L'emblématique opéra de Sydney est situé dans ma plus grande ville."),
            WhoAmIClue("AU_5", "My capital city is Canberra.", "عاصمتي هي كانبرا.", "Meine Hauptstadt ist Canberra.", "Ma capitale est Canberra.")
        ),
        "MA" to listOf(
            WhoAmIClue("MA_1", "I am located in the Maghreb region of North Africa.", "أقع في منطقة المغرب العربي بشمال إفريقيا.", "Ich liege in der Maghreb-Region in Nordafrika.", "Je suis situé dans la région du Maghreb en Afrique du Nord."),
            WhoAmIClue("MA_2", "I have coastlines along both the Atlantic Ocean and Mediterranean Sea.", "أمتلك سواحل على كل من المحيط الأطلسي والبحر الأبيض المتوسط.", "Ich habe Küsten sowohl am Atlantischen Ozean als auch am Mittelmeer.", "J'ai des côtes le long de l'océan Atlantique et de la mer Méditerranée."),
            WhoAmIClue("MA_3", "The Atlas Mountains run through the central part of my land.", "تمر جبال الأطلس عبر الجزء الأوسط من أراضي بلادي.", "Das Atlasgebirge verläuft durch den zentralen Teil meines Landes.", "La chaîne de l'Atlas traverse la partie centrale de mon pays."),
            WhoAmIClue("MA_4", "My red national flag features a green pentagram star in the center.", "يحمل علمي الأحمر الوطني نجمة خماسية الخضراء في المنتصف.", "Meine rote Nationalflagge zeigt einen grünen Fünfstern in der Mitte.", "Mon drapeau national rouge comporte une étoile pentagramme verte au centre."),
            WhoAmIClue("MA_5", "My capital city is Rabat.", "عاصمتي هي الرباط.", "Meine Hauptstadt ist Rabat.", "Ma capitale est Rabat.")
        ),
        "IT" to listOf(
            WhoAmIClue("IT_1", "I am a boot-shaped peninsula located in Southern Europe on the Mediterranean.", "أنا شبه جزيرة على شكل حذاء تقع في جنوب أوروبا على البحر المتوسط.", "Ich bin eine stiefelförmige Halbinsel in Südeuropa am Mittelmeer.", "Je suis une péninsule en forme de botte située en Europe du Sud sur la Méditerranée."),
            WhoAmIClue("IT_2", "The ancient Roman Colosseum and Leaning Tower of Pisa are in my country.", "كولوسيوم روما القديم وبرج بيزا المائل يقعان في بلادي.", "Das antike Kolosseum und der Schiefe Turm von Pisa befinden sich in meinem Land.", "Le Colisée romain antique et la tour de Pise se trouvent dans mon pays."),
            WhoAmIClue("IT_3", "Two independent microstates, San Marino and Vatican City, are enclaved within me.", "توجد دولتان مستقلتان صغيرتان داخل حدودي: سان مارينو والفاتيكان.", "Zwei unabhängige Mikrostaaten, San Marino und Vatikanstadt, liegen als Enklaven in mir.", "Deux micro-États indépendants, Saint-Marin et le Vatican, sont enclavés chez moi."),
            WhoAmIClue("IT_4", "My tricolor flag features green, white, and red vertical stripes.", "يتكون علمي ثلاثي الألوان من أشرطة رأسية باللون الأخضر والأبيض والأحمر.", "Meine Trikolore hat vertikale Streifen in Grün, Weiß und Rot.", "Mon drapeau tricolore comporte des bandes verticales verte, blanche et rouge."),
            WhoAmIClue("IT_5", "My capital city is Rome.", "عاصمتي هي روما.", "Meine Hauptstadt ist Rom.", "Ma capitale est Rome.")
        ),
        "ES" to listOf(
            WhoAmIClue("ES_1", "I occupy the majority of the Iberian Peninsula in Southwestern Europe.", "أحتل معظم شبه الجزيرة الإيبيرية في جنوب غرب أوروبا.", "Ich nehme den größten Teil der Iberischen Halbinsel in Südwesteuropa ein.", "J'occupe la majeure partie de la péninsule Ibérique en Europe du Sud-Ouest."),
            WhoAmIClue("ES_2", "I am bordered by France to the northeast and Portugal to the west.", "تحدني فرنسا من الشمال الشرق والبرتغال من الغرب.", "Ich grenze im Nordosten an Frankreich und im Westen an Portugal.", "Je suis bordé par la France au nord-est et le Portugal à l'ouest."),
            WhoAmIClue("ES_3", "The Sagrada Família in Barcelona and Alhambra in Granada are in my country.", "كنيسة ساغرادا فاميليا في برشلونا وقصر الحمراء في غرناطة يقعان في بلادي.", "Die Sagrada Família und die Alhambra in Granada befinden sich in meinem Land.", "La Sagrada Família à Barcelone et l'Alhambra à Grenade sont dans mon pays."),
            WhoAmIClue("ES_4", "My flag has red and yellow horizontal bands with the Royal Coat of Arms.", "يحمل علمي أشرطة أفقية باللونين الأحمر والأصفر مع شعار النبالة الملكي.", "Meine Flagge hat rote und gelbe horizontale Streifen mit dem königlichen Wappen.", "Mon drapeau comporte des bandes horizontales rouge et jaune avec les armoiries royales."),
            WhoAmIClue("ES_5", "My capital city is Madrid.", "عاصمتي هي مدريد.", "Meine Hauptstadt ist Madrid.", "Ma capitale est Madrid.")
        )
    )

    fun createQuestionsForLevel(
        levelIndex: Int,
        allCountries: List<Country>,
        lang: String,
        seedModifier: Long = System.currentTimeMillis()
    ): List<WhoAmIQuestion> {
        if (allCountries.isEmpty()) return emptyList()

        val config = WhoAmILevelConfig.getConfig(levelIndex)
        val random = Random(levelIndex * 1000L + seedModifier)

        val countryPool = when (config.poolType) {
            "FAMOUS" -> {
                val famousIds = setOf("US", "SA", "FR", "DE", "JP", "BR", "EG", "AU", "CA", "GB", "IN", "CN", "IT", "ES", "MA", "DZ")
                allCountries.filter { it.id in famousIds }.ifEmpty { allCountries }
            }
            "CONTINENT_SAME" -> allCountries
            "REGIONAL_NEIGHBORS" -> allCountries
            else -> allCountries
        }.shuffled(random)

        val selectedTargets = countryPool.take(config.questionCount.coerceAtMost(allCountries.size))

        return selectedTargets.mapIndexed { qIdx, targetCountry ->
            val clues = getCluesForCountry(targetCountry, config.maxCluesPerQuestion)
            val distractors = generateDistractors(targetCountry, allCountries, random)

            val options = (distractors + targetCountry).shuffled(random)
            val correctIndex = options.indexOf(targetCountry)

            WhoAmIQuestion(
                id = "WHO_L${levelIndex}_Q${qIdx + 1}_${targetCountry.id}",
                targetCountry = targetCountry,
                clues = clues,
                options = options,
                correctIndex = correctIndex,
                difficulty = levelIndex,
                timeTargetSec = config.timeTargetSec,
                educationalFactEn = targetCountry.funFactEn,
                educationalFactAr = targetCountry.funFactAr,
                educationalFactDe = targetCountry.funFactDe,
                educationalFactFr = targetCountry.funFactFr
            )
        }
    }

    private fun getCluesForCountry(country: Country, maxClues: Int): List<WhoAmIClue> {
        val curated = CURATED_CLUES[country.id]
        if (curated != null && curated.isNotEmpty()) {
            return curated.take(maxClues)
        }

        // Dynamic fallback clues generator based on country fields
        val fallback = mutableListOf<WhoAmIClue>()

        // Clue 1: Continent
        fallback.add(
            WhoAmIClue(
                "${country.id}_FB1",
                "I am located in ${country.continentEn} (${country.subregionEn}).",
                "أقع في قارة ${country.continentAr} (${country.subregionAr}).",
                "Ich befinde mich auf dem Kontinent ${country.continentDe} (${country.subregionDe}).",
                "Je suis situé sur le continent ${country.continentFr} (${country.subregionFr})."
            )
        )

        // Clue 2: Language & Org
        fallback.add(
            WhoAmIClue(
                "${country.id}_FB2",
                "My primary official language is ${country.languagesEn}.",
                "اللغة الرسمية الرئيسية في بلادي هي ${country.languagesAr}.",
                "Meine wichtigste Amtssprache ist ${country.languagesDe}.",
                "Ma principale langue officielle est ${country.languagesFr}."
            )
        )

        // Clue 3: Currency
        fallback.add(
            WhoAmIClue(
                "${country.id}_FB3",
                "My official national currency is ${country.currencyEn}.",
                "العملة الوطنية الرسمية لبلادي هي ${country.currencyAr}.",
                "Meine offizielle Landeswährung ist ${country.currencyDe}.",
                "Ma monnaie nationale officielle est ${country.currencyFr}."
            )
        )

        // Clue 4: Flag Description / Fun fact snippet
        fallback.add(
            WhoAmIClue(
                "${country.id}_FB4",
                "My national flag features the emoji symbol ${country.flagEmoji}.",
                "رمز علمي الوطني هو ${country.flagEmoji}.",
                "Meine Nationalflagge hat das Symbol ${country.flagEmoji}.",
                "Mon drapeau national comporte le symbole ${country.flagEmoji}."
            )
        )

        // Clue 5: Capital
        fallback.add(
            WhoAmIClue(
                "${country.id}_FB5",
                "My capital city is ${country.capitalEn}.",
                "عاصمتي هي ${country.capitalAr}.",
                "Meine Hauptstadt ist ${country.capitalDe}.",
                "Ma capitale est ${country.capitalFr}."
            )
        )

        return fallback.take(maxClues)
    }

    private fun generateDistractors(target: Country, allCountries: List<Country>, random: Random): List<Country> {
        val sameContinent = allCountries.filter { it.id != target.id && it.continentEn == target.continentEn }
        val pool = if (sameContinent.size >= 3) sameContinent else allCountries.filter { it.id != target.id }

        return pool.shuffled(random).distinctBy { it.id }.take(3)
    }

    fun calculateScore(
        levelIndex: Int,
        totalQuestions: Int,
        correctCount: Int,
        cluesRevealedPerQuestion: List<Int>, // 1..5 for each question
        elapsedTimeSec: Long,
        bestEarlyStreak: Int,
        isNewRecord: Boolean
    ): WhoAmIScoreResult {
        var baseScore = 0

        cluesRevealedPerQuestion.forEach { clueCount ->
            val pointsForQuestion = when (clueCount) {
                1 -> 500
                2 -> 400
                3 -> 300
                4 -> 200
                else -> 100
            }
            baseScore += pointsForQuestion
        }

        val accuracy = if (totalQuestions > 0) ((correctCount.toDouble() / totalQuestions) * 100).roundToInt() else 0
        val avgClues = if (cluesRevealedPerQuestion.isNotEmpty()) cluesRevealedPerQuestion.average() else 1.0

        val earlyStreakBonus = bestEarlyStreak * 50
        val finalScore = baseScore + earlyStreakBonus

        val starsEarned = when {
            accuracy >= 80 && avgClues <= 2.8 -> 3
            accuracy >= 60 -> 2
            accuracy >= 40 -> 1
            else -> 0
        }

        val coinsEarned = starsEarned * 35 + (bestEarlyStreak * 10)

        return WhoAmIScoreResult(
            finalScore = finalScore,
            starsEarned = starsEarned,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            accuracyPercentage = accuracy,
            averageCluesUsed = (avgClues * 10.0).roundToInt() / 10.0,
            bestEarlyStreak = bestEarlyStreak,
            elapsedTimeSec = elapsedTimeSec,
            coinsEarned = coinsEarned,
            isNewRecord = isNewRecord
        )
    }
}
