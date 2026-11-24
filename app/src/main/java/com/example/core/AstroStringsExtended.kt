package com.example.core

import com.example.model.ChartDivision
import com.example.model.ChoghadiyaNature
import com.example.model.Dignity
import com.example.model.FunctionalNature
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.SadeSatiPhase

object AstroStringsExtended {

    fun planetName(p: PlanetId, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> listOf("सूर्य", "चंद्र", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "राहु", "केतु")[p.index]
        AppLanguage.EN -> listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")[p.index]
        AppLanguage.BN -> listOf("সূর্য", "চন্দ্র", "মঙ্গল", "বুধ", "বৃহস্পতি", "শুক্র", "শনি", "রাহু", "কেতু")[p.index]
        AppLanguage.GU -> listOf("સૂર્ય", "ચંદ્ર", "મંગળ", "બુધ", "ગુરુ", "શુક્ર", "શનિ", "રાહુ", "કેતુ")[p.index]
        AppLanguage.TA -> listOf("சூரியன்", "சந்திரன்", "செவ்வாய்", "புதன்", "குரு", "சுக்கிரன்", "சனி", "ராகு", "கேது")[p.index]
        AppLanguage.TE -> listOf("సూర్యుడు", "చంద్రుడు", "కుజుడు", "బుధుడు", "గురుడు", "శుక్రుడు", "శని", "రాహువు", "కేతువు")[p.index]
        AppLanguage.ML -> listOf("സൂര്യൻ", "ചന്ദ്രൻ", "ചൊവ്വ", "ബുധൻ", "വ്യാഴം", "ശുക്രൻ", "ശനി", "രാഹു", "കേതു")[p.index]
        AppLanguage.KN -> listOf("ಸೂರ್ಯ", "ಚಂದ್ರ", "ಮಂಗಳ", "ಬುಧ", "ಗುರು", "ಶುಕ್ರ", "ಶನಿ", "ರಾಹು", "ಕೇತು")[p.index]
        AppLanguage.ZH -> listOf("太阳", "月亮", "火星", "水星", "木星", "金星", "土星", "罗睺", "计都")[p.index]
    }

    fun planetShort(p: PlanetId, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> listOf("सू", "चं", "मं", "बु", "गु", "शु", "श", "रा", "के")[p.index]
        AppLanguage.EN -> listOf("Su", "Mo", "Ma", "Me", "Ju", "Ve", "Sa", "Ra", "Ke")[p.index]
        AppLanguage.BN -> listOf("সূ", "চ", "ম", "বু", "বৃ", "শু", "শ", "রা", "কে")[p.index]
        AppLanguage.GU -> listOf("સૂ", "ચં", "મં", "બુ", "ગુ", "શુ", "શ", "રા", "કે")[p.index]
        AppLanguage.TA -> listOf("சூ", "ச", "செ", "பு", "கு", "சு", "சனி", "ரா", "கே")[p.index]
        AppLanguage.TE -> listOf("సూ", "చం", "కు", "బు", "గు", "శు", "శ", "రా", "కే")[p.index]
        AppLanguage.ML -> listOf("സൂ", "ച", "ചൊ", "ബു", "വ്യാ", "ശു", "ശ", "രാ", "കേ")[p.index]
        AppLanguage.KN -> listOf("ಸೂ", "ಚಂ", "ಮಂ", "ಬು", "ಗು", "ಶು", "ಶ", "ರಾ", "ಕೇ")[p.index]
        AppLanguage.ZH -> listOf("日", "月", "火", "水", "木", "金", "土", "罗", "计")[p.index]
    }

    fun lagnaLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> "लग्न"
        AppLanguage.EN -> "Asc"
        AppLanguage.BN -> "লগ্ন"
        AppLanguage.GU -> "લગ્ન"
        AppLanguage.TA -> "லக்னம்"
        AppLanguage.TE -> "లగ్నం"
        AppLanguage.ML -> "ലഗ്നം"
        AppLanguage.KN -> "ಲಗ್ನ"
        AppLanguage.ZH -> "命"
    }

    fun rashiName(r: Rashi, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> listOf("मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या", "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन")[r.ordinal]
        AppLanguage.EN -> listOf("Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces")[r.ordinal]
        AppLanguage.BN -> listOf("মেষ", "বৃষ", "মিথুন", "কর্কট", "সিংহ", "কন্যা", "তুলা", "বৃশ্চিক", "ধনু", "মকর", "কুম্ভ", "মীন")[r.ordinal]
        AppLanguage.GU -> listOf("મેષ", "વૃષભ", "મિથુન", "કર્ક", "સિંહ", "કન્યા", "તુલા", "વૃશ્ચિક", "ધન", "મકર", "કુંભ", "મીન")[r.ordinal]
        AppLanguage.TA -> listOf("மேஷம்", "ரிஷபம்", "மிதுனம்", "கடகம்", "சிம்மம்", "கன்னி", "துலாம்", "விருச்சிகம்", "தனுசு", "மகரம்", "கும்பம்", "மீனம்")[r.ordinal]
        AppLanguage.TE -> listOf("మేషం", "వృషభం", "మిథునం", "కర్కాటకం", "సింహం", "కన్య", "తుల", "వృశ్చికం", "ధనుస్సు", "మకరం", "కుంభం", "మీనం")[r.ordinal]
        AppLanguage.ML -> listOf("മേടം", "ഇടവം", "മിഥുനം", "കർക്കടകം", "ചിങ്ങം", "കന്നി", "തുലാം", "വൃശ്ചികം", "ധനു", "മകരം", "കുംഭം", "മീനം")[r.ordinal]
        AppLanguage.KN -> listOf("ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕ", "ಸಿಂಹ", "ಕನ್ಯಾ", "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ")[r.ordinal]
        AppLanguage.ZH -> listOf("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座")[r.ordinal]
    }

    private val nakshatrasHi = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा",
        "पुनर्वसु", "पुष्य", "आश्लेषा", "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी",
        "हस्त", "चित्रा", "स्वाति", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूल", "पूर्वाषाढ़ा", "उत्तराषाढ़ा", "श्रवण", "धनिष्ठा", "शतभिषा",
        "पूर्वाभाद्रपद", "उत्तराभाद्रपद", "रेवती"
    )
    private val nakshatrasEn = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )
    private val nakshatrasZh = listOf(
        "娄宿(Ashwini)", "胃宿(Bharani)", "昴宿(Krittika)", "毕宿(Rohini)", "觜宿(Mrigashira)", "参宿(Ardra)",
        "井宿(Punarvasu)", "鬼宿(Pushya)", "柳宿(Ashlesha)", "星宿(Magha)", "张宿(P.Phalguni)", "翼宿(U.Phalguni)",
        "轸宿(Hasta)", "角宿(Chitra)", "亢宿(Swati)", "氐宿(Vishakha)", "房宿(Anuradha)", "心宿(Jyeshtha)",
        "尾宿(Mula)", "箕宿(P.Ashadha)", "斗宿(U.Ashadha)", "牛宿(Shravana)", "女宿(Dhanishta)", "虚宿(Shatabhisha)",
        "危宿(P.Bhadra)", "室宿(U.Bhadra)", "壁宿(Revati)"
    )

    fun nakshatraName(index: Int, lang: AppLanguage): String {
        val i = ((index % 27) + 27) % 27
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> nakshatrasHi[i]
            AppLanguage.ZH -> nakshatrasZh[i]
            else -> nakshatrasEn[i]
        }
    }

    fun dignityLabel(dignity: Dignity, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> when (dignity) {
            Dignity.EXALTED -> "उच्च"
            Dignity.MOOLTRIKONA -> "मूलत्रिकोण"
            Dignity.OWN_SIGN -> "स्वराशि"
            Dignity.FRIEND -> "मित्र"
            Dignity.NEUTRAL -> "सम"
            Dignity.ENEMY -> "शत्रु"
            Dignity.DEBILITATED -> "नीच"
        }
        AppLanguage.ZH -> when (dignity) {
            Dignity.EXALTED -> "庙旺"
            Dignity.MOOLTRIKONA -> "本源"
            Dignity.OWN_SIGN -> "入庙"
            Dignity.FRIEND -> "友宫"
            Dignity.NEUTRAL -> "平宫"
            Dignity.ENEMY -> "敌宫"
            Dignity.DEBILITATED -> "落陷"
        }
        else -> when (dignity) {
            Dignity.EXALTED -> "Exalted"
            Dignity.MOOLTRIKONA -> "Mooltrikona"
            Dignity.OWN_SIGN -> "Own Sign"
            Dignity.FRIEND -> "Friend"
            Dignity.NEUTRAL -> "Neutral"
            Dignity.ENEMY -> "Enemy"
            Dignity.DEBILITATED -> "Debilitated"
        }
    }

    fun weekdayName(index: Int, lang: AppLanguage): String {
        val i = ((index % 7) + 7) % 7
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> listOf("रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")[i]
            AppLanguage.BN -> listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")[i]
            AppLanguage.GU -> listOf("રવિવાર", "સોમવાર", "મંગળવાર", "બુધવાર", "ગુરુવાર", "શુક્રવાર", "શનિવાર")[i]
            AppLanguage.TA -> listOf("ஞாயிறு", "திங்கள்", "செவ்வாய்", "புதன்", "வியாழன்", "வெள்ளி", "சனி")[i]
            AppLanguage.TE -> listOf("ఆదివారం", "సోమవారం", "మంగళవారం", "బుధవారం", "గురువారం", "శుక్రవారం", "శనివారం")[i]
            AppLanguage.ML -> listOf("ഞായർ", "തിങ്കൾ", "ചൊവ്വ", "ബുധൻ", "വ്യാഴം", "വെള്ളി", "ശനി")[i]
            AppLanguage.KN -> listOf("ಭಾನುವಾರ", "ಸೋಮವಾರ", "ಮಂಗಳವಾರ", "ಬುಧವಾರ", "ಗುರುವಾರ", "ಶುಕ್ರವಾರ", "ಶನಿವಾರ")[i]
            AppLanguage.ZH -> listOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")[i]
            AppLanguage.EN -> listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")[i]
        }
    }

    fun tithiName(tithiNumber: Int, lang: AppLanguage): String {
        val pakshaIdx = (tithiNumber - 1) % 15
        val isShukla = tithiNumber <= 15
        val namesHi = listOf(
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पंचमी", "षष्ठी", "सप्तमी",
            "अष्टमी", "नवमी", "दशमी", "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी",
            if (isShukla) "पूर्णिमा" else "अमावस्या"
        )
        val namesEn = listOf(
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami", "Shashthi", "Saptami",
            "Ashtami", "Navami", "Dashami", "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi",
            if (isShukla) "Purnima" else "Amavasya"
        )
        val namesZh = listOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七",
            "初八", "初九", "初十", "十一", "十二", "十三", "十四",
            if (isShukla) "满月(Purnima)" else "新月(Amavasya)"
        )
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> "${if (isShukla) "शुक्ल" else "कृष्ण"} ${namesHi[pakshaIdx]}"
            AppLanguage.ZH -> "${if (isShukla) "白半月" else "黑半月"} ${namesZh[pakshaIdx]}"
            else -> "${if (isShukla) "Shukla" else "Krishna"} ${namesEn[pakshaIdx]}"
        }
    }

    fun yogaName(index: Int, lang: AppLanguage): String {
        val i = ((index % 27) + 27) % 27
        val hi = listOf(
            "विष्कंभ", "प्रीति", "आयुष्मान", "सौभाग्य", "शोभन", "अतिगंड", "सुकर्मा",
            "धृति", "शूल", "गंड", "वृद्धि", "ध्रुव", "व्याघात", "हर्षण", "वज्र",
            "सिद्धि", "व्यतीपात", "वरीयान", "परिघ", "शिव", "सिद्ध", "साध्य",
            "शुभ", "शुक्ल", "ब्रह्म", "इंद्र", "वैधृति"
        )
        val en = listOf(
            "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma",
            "Dhriti", "Shula", "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
            "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva", "Siddha", "Sadhya",
            "Shubha", "Shukla", "Brahma", "Indra", "Vaidhriti"
        )
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> hi[i]
            AppLanguage.ZH -> "第${i + 1}瑜伽(${en[i]})"
            else -> en[i]
        }
    }

    fun karanaName(index: Int, lang: AppLanguage): String {
        val i = ((index % 11) + 11) % 11
        val hi = listOf("बव", "बालव", "कौलव", "तैतिल", "गर", "वणिज", "विष्टि (भद्रा)", "शकुनि", "चतुष्पाद", "नाग", "किंस्तुघ्न")
        val en = listOf("Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti (Bhadra)", "Shakuni", "Chatushpada", "Naga", "Kimstughna")
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> hi[i]
            else -> en[i]
        }
    }

    fun choghadiyaName(nature: ChoghadiyaNature, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> when (nature) {
            ChoghadiyaNature.AMRIT -> "अमृत"
            ChoghadiyaNature.SHUBH -> "शुभ"
            ChoghadiyaNature.LABH -> "लाभ"
            ChoghadiyaNature.CHAR -> "चर"
            ChoghadiyaNature.UDVEG -> "उद्वेग"
            ChoghadiyaNature.ROG -> "रोग"
            ChoghadiyaNature.KAAL -> "काल"
        }
        AppLanguage.ZH -> when (nature) {
            ChoghadiyaNature.AMRIT -> "甘露(吉)"
            ChoghadiyaNature.SHUBH -> "吉祥(吉)"
            ChoghadiyaNature.LABH -> "增益(吉)"
            ChoghadiyaNature.CHAR -> "流动(中)"
            ChoghadiyaNature.UDVEG -> "焦虑(凶)"
            ChoghadiyaNature.ROG -> "疾厄(凶)"
            ChoghadiyaNature.KAAL -> "暗曜(凶)"
        }
        else -> when (nature) {
            ChoghadiyaNature.AMRIT -> "Amrit"
            ChoghadiyaNature.SHUBH -> "Shubh"
            ChoghadiyaNature.LABH -> "Labh"
            ChoghadiyaNature.CHAR -> "Char"
            ChoghadiyaNature.UDVEG -> "Udveg"
            ChoghadiyaNature.ROG -> "Rog"
            ChoghadiyaNature.KAAL -> "Kaal"
        }
    }

    fun bhavaTitle(house: Int, lang: AppLanguage): String {
        val hi = listOf("तनु (लग्न)", "धन", "सहज (पराक्रम)", "सुख (माता)", "पुत्र (विद्या)", "अरि (रोग/शत्रु)", "युवति (विवाह)", "रंध्र (आयु)", "धर्म (भाग्य)", "कर्म (कार्यक्षेत्र)", "लाभ (आय)", "व्यय (मोक्ष)")
        val en = listOf("Tanu (Self)", "Dhana (Wealth)", "Sahaja (Siblings)", "Sukha (Home)", "Putra (Children)", "Ari (Health/Debts)", "Yuvati (Marriage)", "Randhra (Longevity)", "Dharma (Fortune)", "Karma (Career)", "Labha (Gains)", "Vyaya (Expenses)")
        val zh = listOf("第一宫(命宫)", "第二宫(财帛)", "第三宫(兄弟)", "第四宫(田宅)", "第五宫(子女)", "第六宫(疾厄)", "第七宫(夫妻)", "第八宫(寿元)", "第九宫(福德)", "第十宫(官禄)", "第十一宫(收益)", "第十二宫(玄秘)")
        val idx = (house - 1).coerceIn(0, 11)
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> "${house}म भाव • ${hi[idx]}"
            AppLanguage.ZH -> "第${house}宫 • ${zh[idx]}"
            else -> "House $house • ${en[idx]}"
        }
    }

    fun functionalNatureLabel(nature: FunctionalNature, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> when (nature) {
            FunctionalNature.YOGAKARAKA -> "योगकारक"
            FunctionalNature.BENEFIC -> "शुभ"
            FunctionalNature.NEUTRAL -> "सम"
            FunctionalNature.MALEFIC -> "अशुभ"
        }
        AppLanguage.ZH -> when (nature) {
            FunctionalNature.YOGAKARAKA -> "瑜伽吉星"
            FunctionalNature.BENEFIC -> "吉星"
            FunctionalNature.NEUTRAL -> "中性"
            FunctionalNature.MALEFIC -> "凶星"
        }
        else -> when (nature) {
            FunctionalNature.YOGAKARAKA -> "Yogakaraka"
            FunctionalNature.BENEFIC -> "Benefic"
            FunctionalNature.NEUTRAL -> "Neutral"
            FunctionalNature.MALEFIC -> "Malefic"
        }
    }

    fun sadeSatiLabel(phase: SadeSatiPhase, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> when (phase) {
            SadeSatiPhase.NONE -> "वर्तमान में साढ़े साती या ढैया नहीं है"
            SadeSatiPhase.RISING_12TH -> "साढ़े साती — प्रथम चरण (चंद्र से १२वां शनि)"
            SadeSatiPhase.PEAK_1ST -> "साढ़े साती — शिखर चरण (जन्म चंद्र पर शनि)"
            SadeSatiPhase.SETTING_2ND -> "साढ़े साती — अंतिम चरण (चंद्र से २रा शनि)"
            SadeSatiPhase.DHAIYA_4TH -> "कंटक ढैया (चंद्र से ४था शनि)"
            SadeSatiPhase.DHAIYA_8TH -> "अष्टम ढैया (चंद्र से ८वां शनि)"
        }
        AppLanguage.ZH -> when (phase) {
            SadeSatiPhase.NONE -> "当前无土星七年半(Sade Sati)或两年半(Dhaiya)"
            SadeSatiPhase.RISING_12TH -> "土星七年半 — 初段 (土星在本命月第12宫)"
            SadeSatiPhase.PEAK_1ST -> "土星七年半 — 顶峰段 (土星合本命月亮)"
            SadeSatiPhase.SETTING_2ND -> "土星七年半 — 末段 (土星在本命月第2宫)"
            SadeSatiPhase.DHAIYA_4TH -> "土星两年半 — 第4宫行运"
            SadeSatiPhase.DHAIYA_8TH -> "土星两年半 — 第8宫行运"
        }
        else -> when (phase) {
            SadeSatiPhase.NONE -> "No Sade Sati or Dhaiya active currently"
            SadeSatiPhase.RISING_12TH -> "Sade Sati — Rising Phase (Saturn 12th from Moon)"
            SadeSatiPhase.PEAK_1ST -> "Sade Sati — Peak Phase (Saturn over Natal Moon)"
            SadeSatiPhase.SETTING_2ND -> "Sade Sati — Setting Phase (Saturn 2nd from Moon)"
            SadeSatiPhase.DHAIYA_4TH -> "Kantak Dhaiya (Saturn 4th from Moon)"
            SadeSatiPhase.DHAIYA_8TH -> "Ashtama Dhaiya (Saturn 8th from Moon)"
        }
    }

    fun taraName(taraNum: Int, lang: AppLanguage): String {
        val i = (taraNum - 1).coerceIn(0, 8)
        val hi = listOf("जन्म", "संपत", "विपत", "क्षेम", "प्रत्यरि", "साधक", "वध", "मित्र", "अतिमित्र")
        val en = listOf("Janma", "Sampat", "Vipat", "Kshema", "Pratyari", "Sadhaka", "Vadha", "Mitra", "Ati-Mitra")
        val zh = listOf("本命星", "财星(吉)", "厄星(凶)", "安康(吉)", "阻星(凶)", "成就(吉)", "克星(凶)", "友星(吉)", "挚友(吉)")
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> hi[i]
            AppLanguage.ZH -> zh[i]
            else -> en[i]
        }
    }

    fun divisionTitle(division: ChartDivision, lang: AppLanguage): String {
        val hi = when (division) {
            ChartDivision.LAGNA_D1 -> "लग्न कुंडली (D1)"
            ChartDivision.CHANDRA -> "चंद्र कुंडली"
            ChartDivision.HORA_D2 -> "होरा - धन (D2)"
            ChartDivision.DREKKANA_D3 -> "द्रेष्काण - पराक्रम (D3)"
            ChartDivision.CHATURTHAMSA_D4 -> "चतुर्थांश - भाग्य/भूमि (D4)"
            ChartDivision.SAPTAMSA_D7 -> "सप्तांश - संतान (D7)"
            ChartDivision.NAVAMSA_D9 -> "नवमांश - विवाह/धर्म (D9)"
            ChartDivision.DASAMSA_D10 -> "दशमांश - कर्म (D10)"
            ChartDivision.DWADASAMSA_D12 -> "द्वादशांश - माता-पिता (D12)"
            ChartDivision.SHODASAMSA_D16 -> "षोडशांश - वाहन/सुख (D16)"
            ChartDivision.VIMSAMSA_D20 -> "विंशांश - उपासना (D20)"
            ChartDivision.CHATURVIMSAMSA_D24 -> "चतुर्विंशांश - विद्या (D24)"
            ChartDivision.SAPTAVIMSAMSA_D27 -> "सप्तविंशांश - बल (D27)"
            ChartDivision.TRIMSAMSA_D30 -> "त्रिशांश - अरिष्ट/स्वभाव (D30)"
            ChartDivision.KHAVEDAMSA_D40 -> "खवेदांश - मातृकुल (D40)"
            ChartDivision.AKSHAVEDAMSA_D45 -> "अक्षवेदांश - चरित्र (D45)"
            ChartDivision.SHASHTIAMSA_D60 -> "षष्ट्यांश - सूक्ष्म कर्म (D60)"
        }
        val en = when (division) {
            ChartDivision.LAGNA_D1 -> "Lagna Chart (D1)"
            ChartDivision.CHANDRA -> "Moon Chart"
            ChartDivision.HORA_D2 -> "Hora - Wealth (D2)"
            ChartDivision.DREKKANA_D3 -> "Drekkana - Siblings (D3)"
            ChartDivision.CHATURTHAMSA_D4 -> "Chaturthamsa - Property (D4)"
            ChartDivision.SAPTAMSA_D7 -> "Saptamsa - Progeny (D7)"
            ChartDivision.NAVAMSA_D9 -> "Navamsa - Spouse/Dharma (D9)"
            ChartDivision.DASAMSA_D10 -> "Dasamsa - Career (D10)"
            ChartDivision.DWADASAMSA_D12 -> "Dwadasamsa - Parents (D12)"
            ChartDivision.SHODASAMSA_D16 -> "Shodasamsa - Vehicles (D16)"
            ChartDivision.VIMSAMSA_D20 -> "Vimsamsa - Spiritual (D20)"
            ChartDivision.CHATURVIMSAMSA_D24 -> "Chaturvimsamsa - Education (D24)"
            ChartDivision.SAPTAVIMSAMSA_D27 -> "Saptavimsamsa - Strength (D27)"
            ChartDivision.TRIMSAMSA_D30 -> "Trimsamsa - Troubles (D30)"
            ChartDivision.KHAVEDAMSA_D40 -> "Khavedamsa - Auspicious (D40)"
            ChartDivision.AKSHAVEDAMSA_D45 -> "Akshavedamsa - Ethics (D45)"
            ChartDivision.SHASHTIAMSA_D60 -> "Shashtiamsa - Past Karma (D60)"
        }
        val zh = when (division) {
            ChartDivision.LAGNA_D1 -> "命盘 (D1)"
            ChartDivision.CHANDRA -> "月亮盘"
            ChartDivision.HORA_D2 -> "二分财富盘 (D2)"
            ChartDivision.DREKKANA_D3 -> "三分手足盘 (D3)"
            ChartDivision.CHATURTHAMSA_D4 -> "四分田宅盘 (D4)"
            ChartDivision.SAPTAMSA_D7 -> "七分子女盘 (D7)"
            ChartDivision.NAVAMSA_D9 -> "九分婚姻盘 (D9)"
            ChartDivision.DASAMSA_D10 -> "十分事业盘 (D10)"
            ChartDivision.DWADASAMSA_D12 -> "十二分父母盘 (D12)"
            ChartDivision.SHODASAMSA_D16 -> "十六分座驾盘 (D16)"
            ChartDivision.VIMSAMSA_D20 -> "二十分灵修盘 (D20)"
            ChartDivision.CHATURVIMSAMSA_D24 -> "二十四分学业盘 (D24)"
            ChartDivision.SAPTAVIMSAMSA_D27 -> "二十七分体能盘 (D27)"
            ChartDivision.TRIMSAMSA_D30 -> "三十分灾厄盘 (D30)"
            ChartDivision.KHAVEDAMSA_D40 -> "四十分吉凶盘 (D40)"
            ChartDivision.AKSHAVEDAMSA_D45 -> "四十五分德行盘 (D45)"
            ChartDivision.SHASHTIAMSA_D60 -> "六十分因果盘 (D60)"
        }
        return when (lang) {
            AppLanguage.HI, AppLanguage.MR -> hi
            AppLanguage.ZH -> zh
            else -> en
        }
    }
}
