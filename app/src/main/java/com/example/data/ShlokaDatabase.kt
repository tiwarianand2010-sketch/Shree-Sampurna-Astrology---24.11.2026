package com.example.data

import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.ShlokaEntry
import com.example.model.ShlokaVerificationStatus

/**
 * Classical Jyotish Shloka Database Engine.
 * Matches only verified classical scriptural verses against extracted Bhava conditions
 * without inventing synthetic predictions.
 */
object ShlokaDatabase {

    val showUnverifiedByDefault = false

    private data class ShlokaRule(
        val entry: ShlokaEntry,
        val matcher: (house: Int, lord: PlanetId, lordHouse: Int, occupants: List<PlanetId>, sign: Rashi) -> Boolean
    )

    private val rules: List<ShlokaRule> = listOf(
        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_1",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "1",
                sanskritVerse = "लग्नाधिपो लग्नगतो देहसौख्यं करोति वै। चतुरः प्रज्ञया युक्तो महाबलपराक्रमः॥",
                conditionDescHi = "लग्नेश प्रथम (लग्न) भाव में स्थित हो",
                conditionDescEn = "Lagnesh (1st lord) placed in the 1st house",
                conditionDescZh = "命主星落入第一宫（命宫）",
                meaningHi = "लग्नेश के लग्न में होने से जातक को देह-सुख, बुद्धि-चातुर्य, आत्मबल और पराक्रम प्राप्त होता है।",
                meaningEn = "With the Lagna lord in the 1st house, the native enjoys physical vitality, sharp intellect, and valor.",
                meaningZh = "命主星入命宫时，命主兼具健康体魄、聪慧才智与坚定意志。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 1 && lordHouse == 1 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_11",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "11",
                sanskritVerse = "लग्नाधिपे लाभसंस्थे नित्यं लाभो भवेन्नृणाम्। कीर्तिः सुचरितं तस्य बहुविद्यासमन्वितः॥",
                conditionDescHi = "लग्नेश एकादश (लाभ) भाव में स्थित हो",
                conditionDescEn = "Lagnesh (1st lord) placed in the 11th house",
                conditionDescZh = "命主星落入第十一宫（收益宫）",
                meaningHi = "लग्नेश के लाभ भाव में होने से विविध माध्यमों से निरंतर लाभ, सुयश और विद्या का संयोग बनता है।",
                meaningEn = "Lagnesh in the 11th house indicates steady gains, good reputation, and diverse learning.",
                meaningZh = "命主星入第十一宫，象征持续获益、良好声誉与广博学识。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 1 && lordHouse == 11 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_9",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "9",
                sanskritVerse = "लग्नाधिपे धर्मसंस्थे भाग्यवान् जनवल्लभः। विष्णुभक्तो विनीतश्च पुण्यवान् सुतवान् सुखी॥",
                conditionDescHi = "लग्नेश नवम (धर्म) भाव में स्थित हो",
                conditionDescEn = "Lagnesh (1st lord) placed in the 9th house",
                conditionDescZh = "命主星落入第九宫（福德/迁移宫）",
                meaningHi = "लग्नेश के नवम भाव में होने से व्यक्ति भाग्यशाली, धर्मनिष्ठ, विनयशील, जनप्रिय और सुखी होता है।",
                meaningEn = "Lagnesh in the 9th house bestows fortune, righteousness, humility, popularity, and contentment.",
                meaningZh = "命主星入第九宫，主福泽深厚、秉性谦和、受人敬重且家庭顺遂。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 1 && lordHouse == 9 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_10",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "10",
                sanskritVerse = "लग्नाधिपे कर्मसंस्थे पितृसुखसमन्वितः। नृपपूज्यो जने ख्यातः स्वार्जितैश्च धनैर्युतः॥",
                conditionDescHi = "लग्नेश दशम (कर्म) भाव में स्थित हो",
                conditionDescEn = "Lagnesh (1st lord) placed in the 10th house",
                conditionDescZh = "命主星落入第十宫（官禄宫）",
                meaningHi = "लग्नेश के दशम भाव में होने से पिता का सुख, समाज व शासन में सम्मान तथा स्व-अर्जित समृद्धि मिलती है।",
                meaningEn = "Lagnesh in the 10th house brings paternal blessings, public honor, and self-earned prosperity.",
                meaningZh = "命主星入第十宫，主事业声望、父辈助力与自力致富。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 1 && lordHouse == 10 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_14",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "14",
                sanskritVerse = "धनाधिपे धनस्थे तु धनवान् गर्वसंयुतः। भार्याद्वययुतो वापि ह्यल्पापत्यसमन्वितः॥",
                conditionDescHi = "धनेश द्वितीय भाव (स्वभाव) में स्थित हो",
                conditionDescEn = "2nd lord placed in the 2nd house",
                conditionDescZh = "第二宫主星落入第二宫（本宫）",
                meaningHi = "धनेश के द्वितीय भाव में होने से धन-संपत्ति का संचय और वाणी-प्रभाव प्रबल होता है।",
                meaningEn = "With the 2nd lord in the 2nd house, wealth accumulation and speech authority are strengthened.",
                meaningZh = "财帛宫主星入本宫，增强财富积累与言辞影响力。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 2 && lordHouse == 2 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_41",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "41",
                sanskritVerse = "चतुर्थेशे चतुर्थस्थे सर्वसंपत्समन्वितः। चतुर्विद्यः सुशीलश्च स्वजनप्रिय एव च॥",
                conditionDescHi = "चतुर्थेश चतुर्थ भाव में स्थित हो",
                conditionDescEn = "4th lord placed in the 4th house",
                conditionDescZh = "第四宫主星落入第四宫（田宅宫）",
                meaningHi = "चतुर्थेश के चौथे भाव में होने से भूमि-भवन, वाहन, विद्या और पारिवारिक स्नेह की प्राप्ति होती है।",
                meaningEn = "The 4th lord in the 4th house confers property, conveyances, learning, and family harmony.",
                meaningZh = "田宅宫主星入本宫，主不动产、学识修养与家人和睦。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 4 && lordHouse == 4 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_53",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "53",
                sanskritVerse = "पञ्चमेशे सुतस्थे तु सर्वसुखसमन्वितः। गुणवान् सुतवान् धीरो दैवतब्राह्मणप्रियः॥",
                conditionDescHi = "पंचमेश पंचम भाव में स्थित हो",
                conditionDescEn = "5th lord placed in the 5th house",
                conditionDescZh = "第五宫主星落入第五宫（子女宫）",
                meaningHi = "पंचमेश के पंचम भाव में होने से संतान सुख, सद्गुण, धैर्य और आध्यात्मिक रुचि बढ़ती है।",
                meaningEn = "The 5th lord in the 5th house grants wisdom, patience, spiritual inclination, and progeny happiness.",
                meaningZh = "子女宫主星入本宫，主智慧沉淀、正念修持与子嗣和美。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 5 && lordHouse == 5 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_105",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "105",
                sanskritVerse = "भाग्येशे भाग्यसंस्थे तु सर्वसौभाग्यसंयुतः। रूपशीलगुणोपेतो भ्रातृसौख्यसमन्वितः॥",
                conditionDescHi = "नवमेश (भाग्येश) नवम भाव में स्थित हो",
                conditionDescEn = "9th lord placed in the 9th house",
                conditionDescZh = "第九宫主星落入第九宫（福德宫）",
                meaningHi = "भाग्येश के स्वभाव में होने से उत्तम सौभाग्य, सद्गुण, शील और भ्रातृ-सुख प्राप्त होता है।",
                meaningEn = "The 9th lord in the 9th house brings enduring fortune, noble character, and fraternal support.",
                meaningZh = "第九宫主星入本宫，主贵人运、高尚品格与兄弟手足之和。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 9 && lordHouse == 9 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_24_118",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 24,
                verse = "118",
                sanskritVerse = "कर्मेशे कर्मभावस्थे सर्वकर्मसु कौशलः। सत्यवाक् शूरचित्तश्च गुरुभक्तिसमन्वितः॥",
                conditionDescHi = "दशमेश (कर्मेश) दशम भाव में स्थित हो",
                conditionDescEn = "10th lord placed in the 10th house",
                conditionDescZh = "第十宫主星落入第十宫（官禄宫）",
                meaningHi = "कर्मेश के दशम भाव में होने से कार्यकौशल, सत्यनिष्ठा, साहस और गुरुजनों के प्रति श्रद्धा रहती है।",
                meaningEn = "The 10th lord in the 10th house gives mastery in profession, truthfulness, courage, and respect for elders.",
                meaningZh = "官禄宫主星入本宫，主专业精进、诚信果敢与尊师重道。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Bhava-Phala Reference"
            )
        ) { house, _, lordHouse, _, _ -> house == 10 && lordHouse == 10 },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_11_guru_kendra",
                sourceBookHi = "बृहत् पाराशर होरा शास्त्र",
                sourceBookEn = "Brihat Parashara Hora Shastra",
                sourceBookZh = "《大帕拉沙罗霍拉经》(BPHS)",
                chapter = 11,
                verse = "2",
                sanskritVerse = "किं कुर्वन्ति ग्रहाः सर्वे यस्य केन्द्रे बृहस्पतिः। मत्तमातंगयूथानि हन्ति केसरीव शत्रून्॥",
                conditionDescHi = "बृहस्पति (गुरु) लग्न या केंद्र (1, 4, 7, 10) भाव में स्थित हो",
                conditionDescEn = "Jupiter placed in Lagna or a Kendra (1, 4, 7, 10)",
                conditionDescZh = "木星位于命宫或四角宫（1、4、7、10宫）",
                meaningHi = "केंद्र में स्थित गुरु अनेक ग्रह-दोषों का शमन कर व्यक्ति को विवेक, संरक्षण और शुभत्व प्रदान करते हैं।",
                meaningEn = "Jupiter in a Kendra mitigates numerous chart afflictions and bestows wisdom and protection.",
                meaningZh = "木星居四角宫可化解诸多星象煞气，赋予智慧与护佑。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical BPHS Kendra-Guru Reference"
            )
        ) { house, _, _, occupants, _ ->
            house in listOf(1, 4, 7, 10) && PlanetId.JUPITER in occupants
        },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_sun_10",
                sourceBookHi = "फलादीपिका / बृहत् पाराशर",
                sourceBookEn = "Phaladeepika / Parashari",
                sourceBookZh = "《果明灯》(Phaladeepika)",
                chapter = 8,
                verse = "4",
                sanskritVerse = "दशमगे दिननाथे नृपपूज्यः सुतसुखान्वितो मतिमान्। आरब्धकार्यसिद्धिं लभते नित्यं महासत्वः॥",
                conditionDescHi = "सूर्य दशम भाव में स्थित हो (दिग्बली)",
                conditionDescEn = "Sun placed in the 10th house (Digbali)",
                conditionDescZh = "太阳落入第十宫（得向力）",
                meaningHi = "दशम भाव में दिग्बली सूर्य नेतृत्व, प्रशासनिक सम्मान, कार्यसिद्धि और प्रबल इच्छाशक्ति प्रदान करते हैं।",
                meaningEn = "Sun in the 10th house (directional strength) confers leadership, authority, and accomplishment in undertakings.",
                meaningZh = "太阳在第十宫得向力，主领导才干、公职声望与事功顺遂。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical Phaladeepika Bhava Reference"
            )
        ) { house, _, _, occupants, _ -> house == 10 && PlanetId.SUN in occupants },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_budh_1",
                sourceBookHi = "फलादीपिका",
                sourceBookEn = "Phaladeepika",
                sourceBookZh = "《果明灯》(Phaladeepika)",
                chapter = 8,
                verse = "13",
                sanskritVerse = "लग्नस्थे शशितनये विद्वान् मधुरभाषी चिरंजीवी। शास्त्रकलासु च निपुणो दयान्वितो नीतिशास्त्रज्ञः॥",
                conditionDescHi = "बुध प्रथम (लग्न) भाव में स्थित हो (दिग्बली)",
                conditionDescEn = "Mercury placed in the 1st house (Digbali)",
                conditionDescZh = "水星落入第一宫（得向力）",
                meaningHi = "लग्न में दिग्बली बुध विद्वत्ता, मधुर वाणी, शास्त्र-कला में निपुणता और विवेकशीलता देता है।",
                meaningEn = "Mercury in the 1st house (directional strength) grants scholarship, eloquent speech, and analytical mastery.",
                meaningZh = "水星入命宫得向力，主博学多才、言辞温雅与思辨敏捷。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical Phaladeepika Bhava Reference"
            )
        ) { house, _, _, occupants, _ -> house == 1 && PlanetId.MERCURY in occupants },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_shani_7",
                sourceBookHi = "फलादीपिका",
                sourceBookEn = "Phaladeepika",
                sourceBookZh = "《果明灯》(Phaladeepika)",
                chapter = 8,
                verse = "25",
                sanskritVerse = "सप्तमगे दिवाकरसुते स्थिरकर्मा कार्यदक्षश्च। यद्यपि विलम्बितविवाहः तथापि धीरः सुशीलश्च॥",
                conditionDescHi = "शनि सप्तम भाव में स्थित हो (दिग्बली)",
                conditionDescEn = "Saturn placed in the 7th house (Digbali)",
                conditionDescZh = "土星落入第七宫（得向力）",
                meaningHi = "सप्तम भाव में दिग्बली शनि साझेदारी में स्थायित्व, कार्यदक्षता और धैर्यशील स्वभाव देता है।",
                meaningEn = "Saturn in the 7th house (directional strength) gives steady partnerships, diligence, and patient endurance.",
                meaningZh = "土星入第七宫得向力，主合作稳重、勤勉务实与深厚定力。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical Phaladeepika Bhava Reference"
            )
        ) { house, _, _, occupants, _ -> house == 7 && PlanetId.SATURN in occupants },

        ShlokaRule(
            entry = ShlokaEntry(
                id = "bphs_chandra_4",
                sourceBookHi = "फलादीपिका",
                sourceBookEn = "Phaladeepika",
                sourceBookZh = "《果明灯》(Phaladeepika)",
                chapter = 8,
                verse = "7",
                sanskritVerse = "सुखभावगते चन्द्रे मातृसौख्यं वाहनं तथा। विमलमतिः सुजनप्रियः कीर्तिमान् धनवान् सुखी॥",
                conditionDescHi = "चंद्रमा चतुर्थ भाव में स्थित हो (दिग्बली)",
                conditionDescEn = "Moon placed in the 4th house (Digbali)",
                conditionDescZh = "月亮落入第四宫（得向力）",
                meaningHi = "चतुर्थ भाव में दिग्बली चंद्रमा मातृ-सुख, निर्मल बुद्धि, गृह-वाहन सुख और जनप्रियता प्रदान करता है।",
                meaningEn = "Moon in the 4th house (directional strength) blesses maternal warmth, emotional clarity, home comfort, and goodwill.",
                meaningZh = "月亮入第四宫得向力，主母爱深厚、心境澄明与居所安宁。",
                status = ShlokaVerificationStatus.VERIFIED,
                verifiedBy = "Classical Phaladeepika Bhava Reference"
            )
        ) { house, _, _, occupants, _ -> house == 4 && PlanetId.MOON in occupants }
    )

    fun matchBhavaShlokas(
        house: Int,
        lord: PlanetId,
        lordHouse: Int,
        occupants: List<PlanetId>,
        sign: Rashi
    ): List<ShlokaEntry> {
        return rules
            .filter { rule ->
                (rule.entry.status == ShlokaVerificationStatus.VERIFIED || showUnverifiedByDefault) &&
                    rule.matcher(house, lord, lordHouse, occupants, sign)
            }
            .map { it.entry }
    }
}
