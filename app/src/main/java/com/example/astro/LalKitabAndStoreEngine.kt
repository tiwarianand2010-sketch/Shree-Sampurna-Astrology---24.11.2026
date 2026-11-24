package com.example.astro

import com.example.model.KarmicRina
import com.example.model.KundaliSummary
import com.example.model.LalKitabAnalysis
import com.example.model.LalKitabPlanetEntry
import com.example.model.PlanetId
import com.example.model.PlanetPosition
import com.example.model.Rashi
import com.example.model.RemedialStoreItem
import com.example.model.StoreCategory

object LalKitabAndStoreEngine {

    private val blindHousesByPlanet = mapOf(
        PlanetId.SUN to setOf(6, 7, 10),
        PlanetId.MOON to setOf(6, 8, 12),
        PlanetId.MARS to setOf(1, 4, 8),
        PlanetId.MERCURY to setOf(3, 8, 9, 12),
        PlanetId.JUPITER to setOf(6, 7, 10),
        PlanetId.VENUS to setOf(1, 6, 9),
        PlanetId.SATURN to setOf(1, 4, 6),
        PlanetId.RAHU to setOf(7, 8, 11),
        PlanetId.KETU to setOf(3, 6, 8)
    )

    private fun houseRemedyPair(planet: PlanetId, house: Int): Pair<String, String> = when (planet) {
        PlanetId.SUN -> "रविवार को गुड़ व तांबा दान करें एवं सूर्य को जल अर्पित करें।" to
            "Offer water to rising Sun and donate jaggery/copper on Sunday."
        PlanetId.MOON -> "सोमवार को दूध या चावल का दान करें और माता का आशीर्वाद लें।" to
            "Offer milk/rice at a temple on Monday and seek mother's blessings."
        PlanetId.MARS -> when (house) {
            1, 4, 8 -> "मंगलवार को मीठी रोटी या मसूर की दाल दान करें; क्रोध पर संयम रखें।" to
                "Donate red lentils or sweet flatbread on Tuesday; practice patience."
            else -> "हनुमान चालीसा का पाठ करें और भाई-बहनों से सद्भाव रखें।" to
                "Recite Hanuman Chalisa and maintain harmony with siblings."
        }
        PlanetId.MERCURY -> "बुधवार को हरी मूंग व पालक गौशाला में दान करें, कन्याओं का सम्मान करें।" to
            "Offer green moong or spinach on Wednesday and respect young daughters."
        PlanetId.JUPITER -> "गुरुवार को केसर/हल्दी का तिलक लगाएँ एवं पीपल के वृक्ष को जल दें।" to
            "Apply saffron/turmeric tilak on Thursday and water a Peepal tree."
        PlanetId.VENUS -> "शुक्रवार को सफेद वस्त्र/दही का दान करें और स्वच्छता बनाए रखें।" to
            "Donate white sweets/curd on Friday and keep your home clean and fragrant."
        PlanetId.SATURN -> "शनिवार को सरसों का तेल और काले तिल छाया दान करें।" to
            "Donate mustard oil and black sesame seeds on Saturday."
        PlanetId.RAHU -> "पक्षियों को सतनाजा डालें और घर की दहलीज स्वच्छ रखें।" to
            "Feed seven-grain mix to birds and keep the home entrance clutter-free."
        PlanetId.KETU -> "काले-सफेद श्वान को रोटी खिलाएँ और गणपति आराधना करें।" to
            "Feed stray dogs with milk/bread and worship Lord Ganesha."
    }

    fun calculateLalKitab(planets: List<PlanetPosition>): LalKitabAnalysis {
        val byId = planets.associateBy { it.planet }
        val entries = planets.map { pos ->
            val house = pos.houseFromLagna
            val kalRashi = Rashi.fromIndex(house - 1) // Kal Purush natural sign in house
            val isBlind = house in (blindHousesByPlanet[pos.planet] ?: emptySet())
            val (remHi, remEn) = houseRemedyPair(pos.planet, house)
            val noteHi = if (isBlind) {
                "लाल किताब खाना नं. $house (अंधा/मंद ग्रह संकेत) • कालपुरुष राशि: ${kalRashi.number}"
            } else {
                "लाल किताब खाना नं. $house • कालपुरुष राशि: ${kalRashi.number}"
            }
            val noteEn = if (isBlind) {
                "Lal Kitab Khana #$house (Dormant/Sensitive placement) • Natural sign #${kalRashi.number}"
            } else {
                "Lal Kitab Khana #$house • Natural Kal Purush sign #${kalRashi.number}"
            }
            LalKitabPlanetEntry(
                planet = pos.planet,
                house = house,
                kalPurushRashi = kalRashi,
                isBlind = isBlind,
                natureNoteHi = noteHi,
                natureNoteEn = noteEn,
                remedyHi = remHi,
                remedyEn = remEn
            )
        }

        // Classical Lal Kitab Rina (Karmic Debt) combinations
        val jupH = byId.getValue(PlanetId.JUPITER).houseFromLagna
        val venH = byId.getValue(PlanetId.VENUS).houseFromLagna
        val merH = byId.getValue(PlanetId.MERCURY).houseFromLagna
        val rahH = byId.getValue(PlanetId.RAHU).houseFromLagna
        val ketH = byId.getValue(PlanetId.KETU).houseFromLagna
        val sunH = byId.getValue(PlanetId.SUN).houseFromLagna
        val satH = byId.getValue(PlanetId.SATURN).houseFromLagna
        val moonH = byId.getValue(PlanetId.MOON).houseFromLagna

        val pitraRina = venH == 9 || merH == 9 || rahH == 9 || ketH == 9
        val matriRina = ketH == 4 || satH == 4 || (moonH in listOf(6, 8, 12))
        val striRina = sunH == 2 || sunH == 7 || moonH == 2 || rahH == 7
        val selfRina = venH == 5 || satH == 5 || rahH == 5

        val rinas = listOf(
            KarmicRina(
                code = "PITRA",
                titleHi = "पितृ ऋण (Pitra Rina)",
                titleEn = "Pitra Rina (Ancestral Debt)",
                isPresent = pitraRina,
                reasonHi = if (pitraRina) "9वें भाव में बुध/शुक्र/राहु/केतु की स्थिति से पितृ ऋण योग बनता है।" else "9वें भाव में पितृ ऋण का प्रमुख योग नहीं है।",
                reasonEn = if (pitraRina) "Mercury, Venus, Rahu, or Ketu in House 9 indicates Pitra Rina." else "No primary Pitra Rina placement in House 9.",
                remedyHi = "सभी रक्त-संबंधियों से समान मात्रा में सिक्के एकत्र कर मंदिर में अर्पित करें।",
                remedyEn = "Collect small equal donations from family elders and offer at a sacred shrine."
            ),
            KarmicRina(
                code = "MATRI",
                titleHi = "मातृ ऋण (Matri Rina)",
                titleEn = "Matri Rina (Maternal Debt)",
                isPresent = matriRina,
                reasonHi = if (matriRina) "4थे भाव में शनि/केतु या त्रिक भाव में चंद्र से मातृ ऋण संकेत है।" else "4थे भाव और चंद्र की स्थिति संतुलित है।",
                reasonEn = if (matriRina) "Saturn/Ketu in House 4 or afflicted Moon signals Matri Rina." else "House 4 and Moon show balanced maternal harmony.",
                remedyHi = "परिवार से चांदी/चावल एकत्र कर बहते जल में प्रवाहित करें और माता की सेवा करें।",
                remedyEn = "Serve your mother attentively and offer silver or milk in flowing clean water."
            ),
            KarmicRina(
                code = "STRI",
                titleHi = "स्त्री ऋण (Stri Rina)",
                titleEn = "Stri Rina (Spousal Debt)",
                isPresent = striRina,
                reasonHi = if (striRina) "भाव 2 या 7 में सूर्य/चंद्र/राहु से स्त्री ऋण का योग दिखता है।" else "भाव 2 और 7 में स्त्री ऋण संकेत नहीं है।",
                reasonEn = if (striRina) "Sun, Moon, or Rahu in Houses 2 or 7 triggers Stri Rina." else "No major Stri Rina yoga in Houses 2 or 7.",
                remedyHi = "100 गायों के लिए चारा या कन्या भोजन का संकल्प लें, जीवनसाथी का सम्मान करें।",
                remedyEn = "Arrange fodder for cows or charity meals for women; honor your spouse."
            ),
            KarmicRina(
                code = "SELF",
                titleHi = "स्व ऋण (Self Rina)",
                titleEn = "Self Rina (Personal Karma)",
                isPresent = selfRina,
                reasonHi = if (selfRina) "5वें भाव में शुक्र/शनि/राहु होने से स्व-ऋण योग बनता है।" else "5वां भाव स्व-ऋण से मुक्त है।",
                reasonEn = if (selfRina) "Venus, Saturn, or Rahu in House 5 signifies Self Rina." else "House 5 is clear of Self Rina.",
                remedyHi = "मंच/जनसेवा या यज्ञ में नारियल व सूखा नारियल अर्पित करें।",
                remedyEn = "Offer dry coconuts in flowing water and contribute to community service."
            )
        )

        val generalHi = entries.filter { it.isBlind }.map { "${it.planet.name}: ${it.remedyHi}" }
            .ifEmpty { listOf("नियमित सूर्य अर्घ्य, गाय को प्रथम रोटी तथा सत्य आचरण सर्वोत्तम लाल किताब उपाय है।") }
        val generalEn = entries.filter { it.isBlind }.map { "${it.planet.name}: ${it.remedyEn}" }
            .ifEmpty { listOf("Daily morning Surya Arghya, first bread for cows, and ethical conduct serve as top remedies.") }

        return LalKitabAnalysis(entries, rinas, generalHi, generalEn)
    }

    val catalog: List<RemedialStoreItem> = listOf(
        // Gemstones (Navratna)
        RemedialStoreItem(
            id = "gem_ruby",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.SUN,
            nameHi = "माणिक्य (Burmese Ruby)",
            nameEn = "Natural Ruby (Manikya)",
            subtitleHi = "सूर्य रत्न • आत्मविश्वास एवं राजयोग",
            subtitleEn = "Sun Gemstone • Authority & Vitality",
            benefitHi = "नेतृत्व क्षमता, मान-सम्मान, पिता से संबंध और हृदय बल बढ़ाता है।",
            benefitEn = "Boosts leadership, vitality, confidence, and Sun-ruled career authority.",
            priceInr = 7500,
            mantra = "ॐ घृणि सूर्याय नमः",
            wearingMethodHi = "रविवार प्रातः तांबे या सोने की अंगूठी में अनामिका अंगुली में धारण करें।",
            wearingMethodEn = "Wear in gold/copper on ring finger Sunday morning after Surya mantra."
        ),
        RemedialStoreItem(
            id = "gem_pearl",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.MOON,
            nameHi = "मोती (Natural Basra/South Sea Pearl)",
            nameEn = "Natural Pearl (Moti)",
            subtitleHi = "चंद्र रत्न • मानसिक शांति एवं सुख",
            subtitleEn = "Moon Gemstone • Calm Mind & Peace",
            benefitHi = "मन की स्थिरता, भावनात्मक संतुलन, निद्रा सुख और माता का सहयोग बढ़ाता है।",
            benefitEn = "Promotes emotional stability, mental calm, restful sleep, and clarity.",
            priceInr = 4200,
            mantra = "ॐ सों सोमाय नमः",
            wearingMethodHi = "सोमवार प्रातः चांदी की अंगूठी में कनिष्ठा अंगुली में धारण करें।",
            wearingMethodEn = "Wear in silver on little finger Monday morning."
        ),
        RemedialStoreItem(
            id = "gem_coral",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.MARS,
            nameHi = "मूंगा (Italian Red Coral)",
            nameEn = "Red Coral (Moonga)",
            subtitleHi = "मंगल रत्न • साहस एवं भूमि लाभ",
            subtitleEn = "Mars Gemstone • Courage & Property",
            benefitHi = "पराक्रम, ऊर्जा, अचल संपत्ति और मांगलिक ऊर्जा के संतुलन हेतु।",
            benefitEn = "Enhances determination, courage, vitality, and real-estate progress.",
            priceInr = 5100,
            mantra = "ॐ अं अंगारकाय नमः",
            wearingMethodHi = "मंगलवार प्रातः तांबे या सोने में अनामिका अंगुली में धारण करें।",
            wearingMethodEn = "Wear in copper or gold on ring finger Tuesday morning."
        ),
        RemedialStoreItem(
            id = "gem_emerald",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.MERCURY,
            nameHi = "पन्ना (Zambian Emerald)",
            nameEn = "Natural Emerald (Panna)",
            subtitleHi = "बुध रत्न • बुद्धि, वाणी एवं व्यापार",
            subtitleEn = "Mercury Gemstone • Intellect & Business",
            benefitHi = "तीव्र स्मरण शक्ति, संवाद कौशल, गणित व व्यापार उन्नति के लिए।",
            benefitEn = "Sharpens analytical intellect, speech, education, and trade acumen.",
            priceInr = 8900,
            mantra = "ॐ बुं बुधाय नमः",
            wearingMethodHi = "बुधवार प्रातः सोने में कनिष्ठा अंगुली में धारण करें।",
            wearingMethodEn = "Wear in gold on little finger Wednesday morning."
        ),
        RemedialStoreItem(
            id = "gem_yellow_sapphire",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.JUPITER,
            nameHi = "पुखराज (Ceylon Yellow Sapphire)",
            nameEn = "Yellow Sapphire (Pukhraj)",
            subtitleHi = "गुरु रत्न • ज्ञान, विवाह एवं धन",
            subtitleEn = "Jupiter Gemstone • Wisdom & Fortune",
            benefitHi = "धर्म, उच्च शिक्षा, संतान सुख, वैवाहिक सौहार्द और समृद्धि प्रदान करता है।",
            benefitEn = "Attracts divine grace, prosperity, marital harmony, and higher wisdom.",
            priceInr = 12500,
            mantra = "ॐ बृं बृहस्पतये नमः",
            wearingMethodHi = "गुरुवार प्रातः सोने में तर्जनी अंगुली में धारण करें।",
            wearingMethodEn = "Wear in gold on index finger Thursday morning."
        ),
        RemedialStoreItem(
            id = "gem_opal",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.VENUS,
            nameHi = "हीरा / ओपल (Diamond / White Opal)",
            nameEn = "Australian Opal / Diamond",
            subtitleHi = "शुक्र रत्न • कला, ऐश्वर्य एवं प्रेम",
            subtitleEn = "Venus Gemstone • Luxury & Harmony",
            benefitHi = "कलात्मक प्रतिभा, वैवाहिक सुख, आकर्षण और भौतिक वैभव बढ़ाता है।",
            benefitEn = "Supports artistic flair, relationship warmth, comfort, and prosperity.",
            priceInr = 6800,
            mantra = "ॐ शुं शुक्राय नमः",
            wearingMethodHi = "शुक्रवार प्रातः चांदी या प्लेटिनम में मध्यमा/अनामिका में धारण करें।",
            wearingMethodEn = "Wear in silver/platinum on Friday morning."
        ),
        RemedialStoreItem(
            id = "gem_blue_sapphire",
            category = StoreCategory.GEMSTONE,
            relatedPlanet = PlanetId.SATURN,
            nameHi = "नीलम (Ceylon Blue Sapphire)",
            nameEn = "Blue Sapphire (Neelam)",
            subtitleHi = "शनि रत्न • अनुशासन एवं तीव्र उन्नति",
            subtitleEn = "Saturn Gemstone • Career Discipline",
            benefitHi = "योगकारक शनि होने पर करियर, न्याय और दीर्घायु लाभ (परीक्षण के बाद ही पहनें)।",
            benefitEn = "Accelerates career rise and protection when Saturn is Yogakaraka (trial first).",
            priceInr = 14000,
            mantra = "ॐ शं शनैश्चराय नमः",
            wearingMethodHi = "ज्योतिषी परीक्षण पश्चात शनिवार को पंचधातु में मध्यमा अंगुली में धारण करें।",
            wearingMethodEn = "Wear only after 3-day trial & consultation on middle finger Saturday evening."
        ),
        // Rudrakshas
        RemedialStoreItem(
            id = "rud_5mukhi",
            category = StoreCategory.RUDRAKSHA,
            relatedPlanet = PlanetId.JUPITER,
            nameHi = "पंचमुखी रुद्राक्ष (5-Mukhi Nepali)",
            nameEn = "5-Mukhi Nepali Rudraksha",
            subtitleHi = "कालाग्नि रुद्र रूप • सर्व-साधारण शांति",
            subtitleEn = "Universal Peace, Focus & Health",
            benefitHi = "बिना किसी दोष के हर व्यक्ति के लिए श्रेष्ठ; रक्तचाप, ध्यान और गुरु बल हेतु।",
            benefitEn = "Safe for everyone; aids meditation, heart peace, and Jupiter balance.",
            priceInr = 1100,
            mantra = "ॐ ह्रीं नमः",
            wearingMethodHi = "सोमवार या गुरुवार को लाल/पीले धागे में गंगाजल से धोकर धारण करें।",
            wearingMethodEn = "Wash with clean water/milk and wear on Monday or Thursday morning."
        ),
        RemedialStoreItem(
            id = "rud_7mukhi",
            category = StoreCategory.RUDRAKSHA,
            relatedPlanet = PlanetId.SATURN,
            nameHi = "सप्तमुखी रुद्राक्ष (7-Mukhi Mahalakshmi)",
            nameEn = "7-Mukhi Mahalakshmi Rudraksha",
            subtitleHi = "महालक्ष्मी एवं शनि शांति",
            subtitleEn = "Wealth Stability & Saturn Relief",
            benefitHi = "साढ़े साती व ढैया के कष्ट कम कर आर्थिक स्थिरता और लक्ष्मी कृपा दिलाता है।",
            benefitEn = "Relieves Sade Sati/Dhaiya hardship and invites steady financial flow.",
            priceInr = 2400,
            mantra = "ॐ हुं नमः",
            wearingMethodHi = "शुक्रवार या शनिवार को लाल धागे या चांदी कैप में धारण करें।",
            wearingMethodEn = "Wear on Friday or Saturday in silver cap or red silk thread."
        ),
        RemedialStoreItem(
            id = "rud_gaurishankar",
            category = StoreCategory.RUDRAKSHA,
            relatedPlanet = PlanetId.MOON,
            nameHi = "गौरी-शंकर रुद्राक्ष",
            nameEn = "Gauri-Shankar Rudraksha",
            subtitleHi = "शिव-शक्ति एकता • वैवाहिक सौहार्द",
            subtitleEn = "Marital Harmony & Family Unity",
            benefitHi = "दंपति के बीच प्रेम, सामंजस्य और मांगलिक बाधा शांति हेतु उत्तम।",
            benefitEn = "Harmonizes marriage, relationship misunderstandings, and home peace.",
            priceInr = 4500,
            mantra = "ॐ गौरीशंकराय नमः",
            wearingMethodHi = "सोमवार को पूजा स्थल में रखें या कंठ में धारण करें।",
            wearingMethodEn = "Place on home altar or wear after Monday Rudrabhishek."
        ),
        // Yantras
        RemedialStoreItem(
            id = "yan_shri",
            category = StoreCategory.YANTRA,
            relatedPlanet = PlanetId.VENUS,
            nameHi = "अष्टधातु श्री यंत्र (Meru Shri Yantra)",
            nameEn = "Energized Shri Yantra",
            subtitleHi = "महात्रिपुरसुंदरी • धन, वास्तु एवं वैभव",
            subtitleEn = "Supreme Prosperity & Vastu Harmony",
            benefitHi = "घर/व्यापार के वास्तु दोष शांत कर लक्ष्मी स्थिरीकरण व सकारात्मक ऊर्जा लाता है।",
            benefitEn = "Corrects Vastu imbalances and attracts abundance to home or workplace.",
            priceInr = 2100,
            mantra = "ॐ श्रीं ह्रीं श्रीं कमले कमलालये प्रसीद प्रसीद",
            wearingMethodHi = "शुक्रवार को पूर्व या ईशान कोण में लाल वस्त्र पर स्थापित करें।",
            wearingMethodEn = "Install in East/North-East puja corner on Friday morning."
        ),
        RemedialStoreItem(
            id = "yan_navagraha",
            category = StoreCategory.YANTRA,
            relatedPlanet = null,
            nameHi = "नवग्रह शांति ताम्र यंत्र",
            nameEn = "Navagraha Peace Copper Yantra",
            subtitleHi = "नौ ग्रहों का संतुलित आध्यात्मिक कवच",
            subtitleEn = "All 9 Planets Harmonic Shield",
            benefitHi = "कठिन महादशा, अंतरदशा और गोचर के प्रतिकूल प्रभाव कम करता है।",
            benefitEn = "Harmonizes challenging Mahadasha, Antardasha, and transit periods.",
            priceInr = 1500,
            mantra = "ॐ नवग्रहाय नमः",
            wearingMethodHi = "रविवार प्रातः पूजा स्थान पर धूप-दीप के साथ स्थापित करें।",
            wearingMethodEn = "Place on altar on Sunday morning with incense and lamp."
        )
    )

    /**
     * Never recommend a gemstone for a Marakesh or dysfunctional trik lord!
     * Suggests gemstones for Lagnesh/5th/9th lords or Yogakaraka, plus 5-Mukhi & Navagraha Yantra safely.
     */
    fun recommendedIdsForChart(summary: KundaliSummary): Set<String> {
        val auspiciousPlanets = summary.planetSummaries.filter { item ->
            !item.isMarakesh && item.planet == summary.lagnesh ||
                (item.functionalNature == com.example.model.FunctionalNature.YOGAKARAKA) ||
                (!item.isMarakesh && item.functionalNature == com.example.model.FunctionalNature.BENEFIC)
        }.map { it.planet }.toSet()

        return catalog.filter { item ->
            item.relatedPlanet == null || item.relatedPlanet in auspiciousPlanets || item.id == "rud_5mukhi"
        }.map { it.id }.toSet()
    }
}
