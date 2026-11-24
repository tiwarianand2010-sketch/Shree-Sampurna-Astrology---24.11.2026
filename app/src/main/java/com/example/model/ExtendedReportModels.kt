package com.example.model

// Comprehensive 21-Module Vedic / KP / Jaimini / Nadi / Lal Kitab / Tajika / Numerology Report Models

data class ExtendedPanchangDetails(
    val moonriseStr: String,
    val moonsetStr: String,
    val dinmaanStr: String,
    val yamagandaStr: String,
    val gulikaStr: String,
    val vedicMasa: String,
    val vikramSamvat: Int,
    val shakaSamvat: Int,
    val ayana: String,
    val ritu: String,
    val sunSign: Rashi,
    val moonSign: Rashi,
    val moonPada: Int,
    val moonStarLord: PlanetId,
    val pakshaLabel: String,
    val baladiAvasthas: Map<PlanetId, String>,
    val vargottamaPlanets: Set<PlanetId>,
    val chalitHouseShifts: List<String>,
    val bhavaChalitMap: Map<PlanetId, Int>,
    val dashaLifeAreaInsights: List<Pair<String, String>>
)

data class PhaladeshSection(
    val topic: String,
    val prediction: String
)

data class YoginiPratyantardasha(
    val lordName: String,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean
)

data class YoginiAntardasha(
    val lordName: String,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean,
    val pratyantars: List<YoginiPratyantardasha>
)

data class YoginiMahadasha(
    val name: String,
    val planetLord: PlanetId,
    val years: Int,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean,
    val antardashas: List<YoginiAntardasha>
)

data class BcpYearRow(
    val age: Int,
    val calendarYear: Int,
    val activeHouse: Int,
    val rashi: Rashi,
    val houseLord: PlanetId,
    val occupants: List<PlanetId>,
    val isCurrentYear: Boolean
)

data class BhriguChakraReport(
    val exactAgeYears: Double,
    val currentActiveAgeInt: Int,
    val currentActiveHouse: Int,
    val yearProgressPercent: Int,
    val currentHouseSummary: String,
    val rows: List<BcpYearRow>
)

data class JaiminiCharaKaraka(
    val karakaCode: String, // AK, AmK, BK, MK, PiK, PK, GK, DK
    val karakaTitle: String,
    val planet: PlanetId,
    val degInSignFormatted: String,
    val d1Rashi: Rashi,
    val d9Rashi: Rashi
)

data class JaiminiArudhaPada(
    val code: String, // AL, A2..A12, UL
    val houseNum: Int,
    val rashi: Rashi,
    val houseFromLagna: Int
)

data class JaiminiCharaDashaPeriod(
    val rashi: Rashi,
    val years: Int,
    val startDateStr: String,
    val endDateStr: String,
    val isCurrent: Boolean,
    val subSigns: List<Triple<Rashi, String, String>> // Rashi, start, end
)

data class JaiminiArgalaRow(
    val targetHouse: Int,
    val argalaSign: Rashi,
    val argalaSources: String,
    val virodhargalaObstructions: String,
    val netVerdict: String
)

data class JaiminiReport(
    val lagnaRashi: Rashi,
    val karakamsaRashi: Rashi,
    val upapadaRashi: Rashi,
    val lagnaRashiDrishti: List<Rashi>,
    val charaKarakas: List<JaiminiCharaKaraka>,
    val arudhaPadas: List<JaiminiArudhaPada>,
    val charaDashas: List<JaiminiCharaDashaPeriod>,
    val argalas: List<JaiminiArgalaRow>,
    val synthesisNotes: List<String>
)

data class NadiPlanetDetail(
    val planet: PlanetId,
    val starLord: PlanetId,
    val subLord: PlanetId,
    val subSubLord: PlanetId,
    val praanaLord: PlanetId,
    val hitHouses: List<Int>,
    val qualityLabel: String
)

data class NadiLifeAreaRow(
    val lifeArea: String,
    val direction: String,
    val confidencePct: Int,
    val activatedHouses: String,
    val insight: String
)

data class NadiReport(
    val rows: List<NadiPlanetDetail>,
    val dashaLifeAreas: List<NadiLifeAreaRow>,
    val activeChainSummary: String
)

data class KpFourStepRow(
    val planet: PlanetId,
    val step1SelfHouses: List<Int>,
    val step2StarLordHouses: List<Int>,
    val step3SubLordHouses: List<Int>,
    val step4SubStarHouses: List<Int>,
    val sixFoldSummary: String
)

data class KpScorecardItem(
    val area: String,
    val primaryCusp: Int,
    val requiredHouses: String,
    val cuspSubLord: PlanetId,
    val scorePct: Int,
    val status: String,
    val timingHint: String
)

data class KpExtendedReport(
    val fourStepRows: List<KpFourStepRow>,
    val partOfFortuneLon: Double,
    val partOfFortuneRashi: Rashi,
    val partOfFortuneHouse: Int,
    val currentRulingPlanets: KpRulingPlanets,
    val transitPlanetsBrief: List<String>,
    val transitCuspAlignments: List<String>,
    val predictionSummary: String,
    val scorecards: List<KpScorecardItem>
)

data class SpecialLagnaRow(
    val code: String,
    val name: String,
    val longitude: Double,
    val rashi: Rashi,
    val dmsFormatted: String,
    val houseFromLagna: Int,
    val nakshatraName: String
)

data class VedicYogaEntry(
    val name: String,
    val category: String,
    val strength: String,
    val planets: String,
    val condition: String,
    val effect: String
)

data class ExtendedVargaChart(
    val code: String,
    val title: String,
    val lagnaRashi: Rashi,
    val planetRashis: Map<PlanetId, Rashi>
)

data class AshtakavargaShodhanaRow(
    val planet: PlanetId,
    val rawTotal: Int,
    val trikonaReducedTotal: Int,
    val ekadhipatyaReducedTotal: Int,
    val rashiPinda: Int,
    val grahaPinda: Int,
    val shodhyaPinda: Int
)

data class ShadbalaExtendedRow(
    val planet: PlanetId,
    val sthana: Double,
    val dig: Double,
    val kaala: Double,
    val cheshta: Double,
    val naisargika: Double,
    val drik: Double,
    val totalVirupas: Double,
    val totalRupas: Double,
    val requiredRupas: Double,
    val ratio: Double,
    val rank: Int
)

data class GemstoneReport(
    val primaryGem: String,
    val primaryPlanet: PlanetId,
    val primaryScore: Int,
    val secondaryGem: String,
    val secondaryPlanet: PlanetId,
    val secondaryScore: Int,
    val additionalGem: String,
    val fingerMetalDayMantra: String,
    val avoidGems: List<String>,
    val extendedOptions: List<String>
)

data class VedicNumerologyReport(
    val mulank: Int,
    val bhagyank: Int,
    val activeDashaNum: Int,
    val gridCounts: Map<Int, Int>,
    val activePlanes: List<String>,
    val gridYogas: List<String>,
    val missingRemedies: List<String>,
    val nameVibrationNote: String,
    val supportNumbersByArea: List<Pair<String, String>>,
    val monthlyEnergy: List<Triple<String, Int, String>> // Month, EnergyScore, LuckyDates+Tip
)

data class LoShuExtendedReport(
    val driver: Int,
    val conductor: Int,
    val nameNumber: Int,
    val personalYear: Int,
    val personalMonth: Int,
    val personalDay: Int,
    val gridCounts: Map<Int, Int>,
    val friendlyNumbers: List<Int>,
    val missingDigits: List<Int>,
    val repeatedDigits: List<String>,
    val activePlanes: List<String>,
    val weaknessPlanes: List<String>,
    val mysticTriangles: List<String>,
    val nameCorrectionTip: String
)

data class SarvatobhadraAndKotaReport(
    val birthSwara: String,
    val sbcElements: List<Pair<String, String>>,
    val vedhaHits: List<String>,
    val lattaNote: String,
    val kotaSwami: PlanetId,
    val kotaPal: PlanetId,
    val stambhaZone: String,
    val madhyaZone: String,
    val prakaaraZone: String,
    val bahyaZone: String,
    val kotaVerdict: String
)

data class LalKitabVarshphalRow(
    val planet: PlanetId,
    val natalHouse: Int,
    val varshphal2026House: Int,
    val drishtiTargetHouse: Int,
    val note: String
)

data class LalKitabExtendedReport(
    val activeAgeCycleNote: String,
    val varshphal2026Rows: List<LalKitabVarshphalRow>,
    val drishtiHighlights: List<String>
)

data class BnnDirectionRow(
    val direction: String,
    val trineElement: String,
    val planetsNormal: List<PlanetId>,
    val planetsWithRetro: List<PlanetId>,
    val themeNote: String
)

data class BnnReport(
    val bhriguBinduLon: Double,
    val bhriguBinduRashi: Rashi,
    val bhriguBinduHouse: Int,
    val directionRows: List<BnnDirectionRow>,
    val jupiterTrineLink: String,
    val degreeOrderSequence: String,
    val predictionNotes: List<String>,
    val progressionRows: List<Triple<String, String, String>> // Year, ProgressingPlanet, ActivatedHouse/Note
)

data class TajikaVarshaphalReport(
    val targetYear: Int,
    val solarReturnDateTimeStr: String,
    val varshaLagna: Rashi,
    val varshaLagnaDeg: String,
    val munthaRashi: Rashi,
    val munthaHouse: Int,
    val munthaLord: PlanetId,
    val varshaSwami: PlanetId,
    val panchadhikari: List<Pair<String, String>>,
    val varshaPlanets: List<Triple<PlanetId, Rashi, Int>>,
    val sahams: List<Triple<String, Rashi, Int>>,
    val muddaDashas: List<Triple<PlanetId, String, String>>,
    val lifeAreaSummaries: List<Pair<String, String>>,
    val monthlyGuidance: List<String>,
    val remedies: List<String>
)

data class CrystalAndRemedyReport(
    val recommendedCrystals: List<Triple<String, String, String>>, // Name, VastuCorner/Usage, MeditationNote
    val planetMantras: List<Triple<PlanetId, String, String>>, // Planet, Mantra, Count/Day
    val lalKitabAndSevaUpays: List<String>,
    val weeklyRemedySchedule: List<Pair<String, String>> // Day -> 40-day / weekly practice
)

data class ComprehensiveReportBundle(
    val panchangExt: ExtendedPanchangDetails,
    val phaladesh: List<PhaladeshSection>,
    val yoginiDashas: List<YoginiMahadasha>,
    val bcp: BhriguChakraReport,
    val jaimini: JaiminiReport,
    val nadi: NadiReport,
    val kpExt: KpExtendedReport,
    val specialLagnas: List<SpecialLagnaRow>,
    val vedicYogas: List<VedicYogaEntry>,
    val all20DivisionalCharts: List<ExtendedVargaChart>,
    val avReducedSarva: List<Int>,
    val avShodhanas: List<AshtakavargaShodhanaRow>,
    val shadbalaExt: List<ShadbalaExtendedRow>,
    val gemstones: GemstoneReport,
    val vedicNumerology: VedicNumerologyReport,
    val loShuExt: LoShuExtendedReport,
    val sbcAndKota: SarvatobhadraAndKotaReport,
    val lalKitabExt: LalKitabExtendedReport,
    val bnn: BnnReport,
    val tajika2026: TajikaVarshaphalReport,
    val crystalAndRemedies: CrystalAndRemedyReport
)
