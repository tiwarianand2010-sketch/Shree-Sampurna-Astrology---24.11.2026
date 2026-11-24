package com.example.core

import com.example.model.ChartDivision
import com.example.model.ChoghadiyaNature
import com.example.model.Dignity
import com.example.model.FunctionalNature
import com.example.model.PlanetId
import com.example.model.Place
import com.example.model.Rashi
import com.example.model.SadeSatiPhase

object AstroStrings {

    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> "श्री सम्पूर्णा एस्ट्रोलॉजी"
        AppLanguage.EN -> "Shri Sampurna Astrology"
        AppLanguage.BN -> "শ্রী সম্পূর্ণা অ্যাস্ট্রোলজি"
        AppLanguage.MR -> "श्री सम्पूर्णा अ‍ॅस्ट्रोलॉजी"
        AppLanguage.GU -> "શ્રી સંપૂર્ણા એસ્ટ્રોલોજી"
        AppLanguage.TA -> "ஸ்ரீ சம்பூர்ணா ஜோதிடம்"
        AppLanguage.TE -> "శ్రీ సంపూర్ణ ఆస్ట్రాలజీ"
        AppLanguage.ML -> "ശ്രീ സമ്പൂർണ്ണ ആസ്ട്രോളജി"
        AppLanguage.KN -> "ಶ್ರೀ ಸಂಪೂರ್ಣ ಆಸ್ಟ್ರಾಲಜಿ"
        AppLanguage.ZH -> "吉祥圆满吠陀占星"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.HI -> "वैदिक, केपी, लाल किताब एवं एआई ज्योतिष विज्ञान"
        AppLanguage.EN -> "Vedic, KP, Lal Kitab & AI Jyotish Science"
        AppLanguage.BN -> "বৈদিক, কেপি, লাল কিতাব ও এআই জ্যোতিষ"
        AppLanguage.MR -> "वैदिक, केपी, लाल किताब व एआय ज्योतिषशास्त्र"
        AppLanguage.GU -> "વૈદિક, કેપી, લાલ કિતાબ અને એઆઈ જ્યોતિષ"
        AppLanguage.TA -> "வேத, கேபி, லால் கிதாப் & AI ஜோதிடம்"
        AppLanguage.TE -> "వేద, కేపీ, లాల్ కితాబ్ & AI జ్యోతిష్యం"
        AppLanguage.ML -> "വേദ, കെപി, ലാൽ കിതാബ് & AI ജ്യോതിഷം"
        AppLanguage.KN -> "ವೇದ, ಕೆಪಿ, ಲಾಲ್ ಕಿತಾಬ್ ಮತ್ತು AI ಜ್ಯೋತಿಷ್ಯ"
        AppLanguage.ZH -> "传统吠陀、KP体系、红皮书与AI占星"
    }

    fun placeName(place: Place, lang: AppLanguage): String = when (lang) {
        AppLanguage.HI, AppLanguage.MR -> place.nameHi
        AppLanguage.ZH -> place.nameZh
        else -> place.nameEn
    }

    fun planetName(planet: PlanetId, lang: AppLanguage): String = AstroStringsExtended.planetName(planet, lang)
    fun planetShort(planet: PlanetId, lang: AppLanguage): String = AstroStringsExtended.planetShort(planet, lang)
    fun lagnaLabel(lang: AppLanguage): String = AstroStringsExtended.lagnaLabel(lang)
    fun rashiName(rashi: Rashi, lang: AppLanguage): String = AstroStringsExtended.rashiName(rashi, lang)
    fun nakshatraName(index: Int, lang: AppLanguage): String = AstroStringsExtended.nakshatraName(index, lang)
    fun dignityLabel(dignity: Dignity, lang: AppLanguage): String = AstroStringsExtended.dignityLabel(dignity, lang)
    fun weekdayName(index: Int, lang: AppLanguage): String = AstroStringsExtended.weekdayName(index, lang)
    fun tithiName(tithiNumber: Int, lang: AppLanguage): String = AstroStringsExtended.tithiName(tithiNumber, lang)
    fun yogaName(index: Int, lang: AppLanguage): String = AstroStringsExtended.yogaName(index, lang)
    fun karanaName(index: Int, lang: AppLanguage): String = AstroStringsExtended.karanaName(index, lang)
    fun choghadiyaName(nature: ChoghadiyaNature, lang: AppLanguage): String = AstroStringsExtended.choghadiyaName(nature, lang)
    fun bhavaTitle(house: Int, lang: AppLanguage): String = AstroStringsExtended.bhavaTitle(house, lang)
    fun functionalNatureLabel(nature: FunctionalNature, lang: AppLanguage): String = AstroStringsExtended.functionalNatureLabel(nature, lang)
    fun sadeSatiLabel(phase: SadeSatiPhase, lang: AppLanguage): String = AstroStringsExtended.sadeSatiLabel(phase, lang)
    fun taraName(taraNum: Int, lang: AppLanguage): String = AstroStringsExtended.taraName(taraNum, lang)
    fun divisionTitle(division: ChartDivision, lang: AppLanguage): String = AstroStringsExtended.divisionTitle(division, lang)
}
