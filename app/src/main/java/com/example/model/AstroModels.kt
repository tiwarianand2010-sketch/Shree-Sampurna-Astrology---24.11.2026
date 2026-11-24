package com.example.model

enum class ChartStyle {
    NORTH_INDIAN,
    SOUTH_INDIAN
}

enum class ChartDivision(val code: String, val harmonic: Int) {
    LAGNA_D1("D1", 1),
    CHANDRA("Mo", 1),
    HORA_D2("D2", 2),
    DREKKANA_D3("D3", 3),
    CHATURTHAMSA_D4("D4", 4),
    SAPTAMSA_D7("D7", 7),
    NAVAMSA_D9("D9", 9),
    DASAMSA_D10("D10", 10),
    DWADASAMSA_D12("D12", 12),
    SHODASAMSA_D16("D16", 16),
    VIMSAMSA_D20("D20", 20),
    CHATURVIMSAMSA_D24("D24", 24),
    SAPTAVIMSAMSA_D27("D27", 27),
    TRIMSAMSA_D30("D30", 30),
    KHAVEDAMSA_D40("D40", 40),
    AKSHAVEDAMSA_D45("D45", 45),
    SHASHTIAMSA_D60("D60", 60);

    companion object {
        val shodashvargaOrder: List<ChartDivision> = listOf(
            LAGNA_D1, HORA_D2, DREKKANA_D3, CHATURTHAMSA_D4,
            SAPTAMSA_D7, NAVAMSA_D9, DASAMSA_D10, DWADASAMSA_D12,
            SHODASAMSA_D16, VIMSAMSA_D20, CHATURVIMSAMSA_D24, SAPTAVIMSAMSA_D27,
            TRIMSAMSA_D30, KHAVEDAMSA_D40, AKSHAVEDAMSA_D45, SHASHTIAMSA_D60
        )
    }
}

enum class DstRule {
    NONE,
    US_MODERN,
    UK_MODERN
}

data class Place(
    val id: String,
    val nameHi: String,
    val nameEn: String,
    val nameZh: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val baseUtcOffsetHours: Double,
    val dstRule: DstRule
)

data class BirthInput(
    val name: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val place: Place,
    val customUtcOffsetHours: Double? = null
)

enum class PlanetId(val index: Int) {
    SUN(0),
    MOON(1),
    MARS(2),
    MERCURY(3),
    JUPITER(4),
    VENUS(5),
    SATURN(6),
    RAHU(7),
    KETU(8);

    companion object {
        val dashaOrder = listOf(KETU, VENUS, SUN, MOON, MARS, RAHU, JUPITER, SATURN, MERCURY)
        val dashaYears = mapOf(
            KETU to 7.0,
            VENUS to 20.0,
            SUN to 6.0,
            MOON to 10.0,
            MARS to 7.0,
            RAHU to 18.0,
            JUPITER to 16.0,
            SATURN to 19.0,
            MERCURY to 17.0
        )
    }
}

enum class Rashi(val number: Int, val lord: PlanetId) {
    MESHA(1, PlanetId.MARS),
    VRISHABHA(2, PlanetId.VENUS),
    MITHUNA(3, PlanetId.MERCURY),
    KARKA(4, PlanetId.MOON),
    SIMHA(5, PlanetId.SUN),
    KANYA(6, PlanetId.MERCURY),
    TULA(7, PlanetId.VENUS),
    VRISCHIKA(8, PlanetId.MARS),
    DHANU(9, PlanetId.JUPITER),
    MAKARA(10, PlanetId.SATURN),
    KUMBHA(11, PlanetId.SATURN),
    MEENA(12, PlanetId.JUPITER);

    companion object {
        fun fromIndex(idx: Int): Rashi = entries[((idx % 12) + 12) % 12]
    }
}

enum class Dignity {
    EXALTED,
    MOOLTRIKONA,
    OWN_SIGN,
    FRIEND,
    NEUTRAL,
    ENEMY,
    DEBILITATED
}

data class PlanetPosition(
    val planet: PlanetId,
    val siderealLongitude: Double,
    val rashi: Rashi,
    val degreeInRashi: Double,
    val dmsFormatted: String,
    val nakshatraIndex: Int,
    val nakshatraPada: Int,
    val nakshatraLord: PlanetId,
    val houseFromLagna: Int,
    val houseFromMoon: Int,
    val navamsaRashi: Rashi,
    val isRetrograde: Boolean,
    val isCombust: Boolean,
    val dignity: Dignity,
    val speedDegPerDay: Double
)

data class PratyantardashaPeriod(
    val lord: PlanetId,
    val antardashaLord: PlanetId,
    val mahadashaLord: PlanetId,
    val startJd: Double,
    val endJd: Double,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean
)

data class AntardashaPeriod(
    val lord: PlanetId,
    val mahadashaLord: PlanetId,
    val startJd: Double,
    val endJd: Double,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean,
    val pratyantardashas: List<PratyantardashaPeriod> = emptyList()
)

data class MahadashaPeriod(
    val lord: PlanetId,
    val startJd: Double,
    val endJd: Double,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean,
    val antardashas: List<AntardashaPeriod>
)

enum class ChoghadiyaNature {
    AMRIT,
    SHUBH,
    LABH,
    CHAR,
    UDVEG,
    ROG,
    KAAL
}

data class ChoghadiyaSlot(
    val nature: ChoghadiyaNature,
    val startTimeStr: String,
    val endTimeStr: String,
    val isGood: Boolean
)

data class PanchangInfo(
    val weekdayIndex: Int,
    val tithiNumber: Int,
    val tithiNameIndex: Int,
    val isShuklaPaksha: Boolean,
    val nakshatraIndex: Int,
    val yogaIndex: Int,
    val karanaIndex: Int,
    val sunriseStr: String,
    val sunsetStr: String,
    val rahukaalStr: String,
    val abhijitStr: String,
    val dayChoghadiya: List<ChoghadiyaSlot>,
    val nightChoghadiya: List<ChoghadiyaSlot>,
    val currentHoraLord: PlanetId
)

enum class ShlokaVerificationStatus {
    VERIFIED,
    PENDING_REVIEW
}

data class ShlokaEntry(
    val id: String,
    val sourceBookHi: String,
    val sourceBookEn: String,
    val sourceBookZh: String,
    val chapter: Int,
    val verse: String,
    val sanskritVerse: String,
    val conditionDescHi: String,
    val conditionDescEn: String,
    val conditionDescZh: String,
    val meaningHi: String,
    val meaningEn: String,
    val meaningZh: String,
    val status: ShlokaVerificationStatus,
    val verifiedBy: String
)

data class BhavaAnalysis(
    val houseNumber: Int,
    val signRashi: Rashi,
    val lord: PlanetId,
    val lordPlacedInHouse: Int,
    val occupants: List<PlanetId>,
    val aspectingPlanets: List<PlanetId>,
    val conjunctionDescriptionsHi: List<String>,
    val conjunctionDescriptionsEn: List<String>,
    val conjunctionDescriptionsZh: List<String>,
    val matchedShlokas: List<ShlokaEntry>
)

enum class FunctionalNature {
    YOGAKARAKA,
    BENEFIC,
    NEUTRAL,
    MALEFIC
}

data class PlanetSummaryItem(
    val planet: PlanetId,
    val functionalNature: FunctionalNature,
    val isMarakesh: Boolean,
    val housesOwned: List<Int>,
    val placedInHouse: Int
)

data class KundaliSummary(
    val lagnaRashi: Rashi,
    val lagnesh: PlanetId,
    val planetSummaries: List<PlanetSummaryItem>,
    val marakeshPlanets: List<PlanetId>,
    val marakaHouseOccupants: List<PlanetId>,
    val strongestPlanet: PlanetId,
    val weakestPlanet: PlanetId,
    val strongestHouse: Int,
    val weakestHouse: Int,
    val matchedConditionsCount: Int,
    val totalConditionsCount: Int
)

data class ShadbalaEntry(
    val planet: PlanetId,
    val sthanaBala: Double,
    val digBala: Double,
    val kendradiBala: Double,
    val naisargikaBala: Double,
    val pakshaBala: Double,
    val totalScore: Double,
    val normalizedStrength: Float
)

data class BhavaBalaEntry(
    val houseNumber: Int,
    val adhipatiBala: Double,
    val digBala: Double,
    val drishtiBala: Double,
    val totalScore: Double,
    val normalizedStrength: Float
)

enum class SadeSatiPhase {
    NONE,
    RISING_12TH,
    PEAK_1ST,
    SETTING_2ND,
    DHAIYA_4TH,
    DHAIYA_8TH
}

data class DayHoroscopeEntry(
    val dayOffset: Int,
    val dateStr: String,
    val moonRashi: Rashi,
    val moonNakshatraIndex: Int,
    val chandraHouseFromBirthMoon: Int,
    val isChandraBalaGood: Boolean,
    val taraBalaNumber: Int,
    val isTaraBalaGood: Boolean
)

data class UpcomingTransitEntry(
    val planet: PlanetId,
    val fromRashi: Rashi,
    val toRashi: Rashi,
    val changeDateStr: String,
    val daysFromNow: Int
)

// --- Parashari Shodashvarga & Ashtakavarga ---
data class ShodashvargaRow(
    val division: ChartDivision,
    val lagnaRashi: Rashi,
    val planetRashis: Map<PlanetId, Rashi>
)

data class Bhinnashtakavarga(
    val planet: PlanetId,
    val bindusByRashi: List<Int>, // 12 entries (Mesha..Meena)
    val totalBindus: Int
)

data class AshtakavargaResult(
    val bhinnashtakavargas: List<Bhinnashtakavarga>,
    val sarvashtakavargaByRashi: List<Int>, // 12 entries (Mesha..Meena)
    val totalSarvaBindus: Int
)

// --- KP (Krishnamurti Paddhati) ---
data class KpSignification(
    val signLord: PlanetId,
    val starLord: PlanetId,
    val subLord: PlanetId,
    val subSubLord: PlanetId,
    val subNumber249: Int
)

data class KpHouseCusp(
    val houseNumber: Int,
    val kpLongitude: Double,
    val rashi: Rashi,
    val dmsFormatted: String,
    val kp: KpSignification,
    val significatorPlanets: List<PlanetId>
)

data class KpPlanetInfo(
    val planet: PlanetId,
    val kpLongitude: Double,
    val rashi: Rashi,
    val dmsFormatted: String,
    val kpHouse: Int,
    val kp: KpSignification,
    val signifiesHouses: List<Int>
)

data class KpRulingPlanets(
    val dayLord: PlanetId,
    val moonStarLord: PlanetId,
    val moonSignLord: PlanetId,
    val lagnaStarLord: PlanetId,
    val lagnaSignLord: PlanetId,
    val distinctRulers: List<PlanetId>
)

data class KpAnalysis(
    val kpAyanamsaDeg: Double,
    val cusps: List<KpHouseCusp>,
    val planets: List<KpPlanetInfo>,
    val rulingPlanets: KpRulingPlanets,
    val prashnaNumber: Int? = null
)

// --- Lal Kitab ---
data class LalKitabPlanetEntry(
    val planet: PlanetId,
    val house: Int,
    val kalPurushRashi: Rashi,
    val isBlind: Boolean,
    val natureNoteHi: String,
    val natureNoteEn: String,
    val remedyHi: String,
    val remedyEn: String
)

data class KarmicRina(
    val code: String,
    val titleHi: String,
    val titleEn: String,
    val isPresent: Boolean,
    val reasonHi: String,
    val reasonEn: String,
    val remedyHi: String,
    val remedyEn: String
)

data class LalKitabAnalysis(
    val entries: List<LalKitabPlanetEntry>,
    val rinas: List<KarmicRina>,
    val remediesHi: List<String>,
    val remediesEn: List<String>
)

// --- Western, Numerology & Vastu / Feng Shui ---
data class WesternPlanet(
    val planet: PlanetId,
    val tropicalLongitude: Double,
    val rashi: Rashi,
    val degreeInSign: Double,
    val dmsFormatted: String,
    val element: String,
    val modality: String
)

data class WesternAspect(
    val planet1: PlanetId,
    val planet2: PlanetId,
    val aspectName: String,
    val angleDeg: Double,
    val orbDeg: Double,
    val isHarmonious: Boolean
)

data class WesternChartResult(
    val ascendantLon: Double,
    val ascendantSign: Rashi,
    val midheavenLon: Double,
    val midheavenSign: Rashi,
    val sunSign: Rashi,
    val moonSign: Rashi,
    val planets: List<WesternPlanet>,
    val aspects: List<WesternAspect>,
    val fireCount: Int,
    val earthCount: Int,
    val airCount: Int,
    val waterCount: Int
)

data class NumerologyResult(
    val name: String,
    val mulank: Int,
    val bhagyank: Int,
    val kuaNumber: Int,
    val chaldeanNameNumber: Int,
    val pythagoreanDestiny: Int,
    val loShuCounts: Map<Int, Int>,
    val missingNumbers: List<Int>,
    val luckyColorsHi: String,
    val luckyColorsEn: String,
    val luckyDaysHi: String,
    val luckyDaysEn: String,
    val luckyNumbers: List<Int>
)

data class VastuFengShuiResult(
    val kuaNumber: Int,
    val kuaGroupEn: String,
    val kuaGroupHi: String,
    val wealthDirectionEn: String,
    val wealthDirectionHi: String,
    val entranceAdviceEn: String,
    val entranceAdviceHi: String,
    val kitchenDirectionEn: String,
    val kitchenDirectionHi: String,
    val bedroomDirectionEn: String,
    val bedroomDirectionHi: String,
    val pujaDirectionEn: String,
    val pujaDirectionHi: String,
    val elementTipsEn: List<String>,
    val elementTipsHi: List<String>
)

// --- Kundali Matching (Ashtakoot + Manglik) ---
data class KootScore(
    val nameEn: String,
    val nameHi: String,
    val maxPoints: Double,
    val obtainedPoints: Double,
    val boyValue: String,
    val girlValue: String,
    val noteEn: String,
    val noteHi: String
)

data class ManglikCheck(
    val isManglik: Boolean,
    val isCancelled: Boolean,
    val housesLagna: List<Int>,
    val housesMoon: List<Int>,
    val summaryEn: String,
    val summaryHi: String
)

data class MatchingResult(
    val boyChart: KundaliResult,
    val girlChart: KundaliResult,
    val koots: List<KootScore>,
    val totalScore: Double,
    val maxScore: Double,
    val boyManglik: ManglikCheck,
    val girlManglik: ManglikCheck,
    val verdictEn: String,
    val verdictHi: String
)

// --- Store Catalog ---
enum class StoreCategory {
    GEMSTONE,
    RUDRAKSHA,
    YANTRA
}

data class RemedialStoreItem(
    val id: String,
    val category: StoreCategory,
    val relatedPlanet: PlanetId?,
    val nameHi: String,
    val nameEn: String,
    val subtitleHi: String,
    val subtitleEn: String,
    val benefitHi: String,
    val benefitEn: String,
    val priceInr: Int,
    val mantra: String,
    val wearingMethodHi: String,
    val wearingMethodEn: String
)

data class KundaliResult(
    val input: BirthInput,
    val effectiveUtcOffsetHours: Double,
    val julianDay: Double,
    val ayanamsaDeg: Double,
    val lagnaLongitude: Double,
    val lagnaRashi: Rashi,
    val lagnaDmsFormatted: String,
    val lagnaNakshatraIndex: Int,
    val lagnaPada: Int,
    val planets: List<PlanetPosition>,
    val mahadashas: List<MahadashaPeriod>,
    val currentMahadasha: MahadashaPeriod?,
    val currentAntardasha: AntardashaPeriod?,
    val currentPratyantardasha: PratyantardashaPeriod? = null,
    val panchang: PanchangInfo,
    val bhavaAnalyses: List<BhavaAnalysis>,
    val summary: KundaliSummary,
    val shadbala: List<ShadbalaEntry>,
    val bhavaBala: List<BhavaBalaEntry>,
    val gocharDateStr: String,
    val gocharPlanets: List<PlanetPosition>,
    val sadeSatiPhase: SadeSatiPhase,
    val saturnHouseFromMoon: Int,
    val threeDayHoroscope: List<DayHoroscopeEntry>,
    val upcomingTransits30Days: List<UpcomingTransitEntry>,
    val upcomingTransits1Year: List<UpcomingTransitEntry>,
    val shodashvarga: List<ShodashvargaRow> = emptyList(),
    val ashtakavarga: AshtakavargaResult? = null,
    val kpAnalysis: KpAnalysis? = null,
    val lalKitab: LalKitabAnalysis? = null,
    val westernChart: WesternChartResult? = null,
    val numerology: NumerologyResult? = null,
    val vastuFengShui: VastuFengShuiResult? = null
)
