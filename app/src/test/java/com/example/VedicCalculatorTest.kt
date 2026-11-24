package com.example

import com.example.astro.KpCalculator
import com.example.astro.LalKitabAndStoreEngine
import com.example.astro.TimeUtils
import com.example.astro.VedicCalculator
import com.example.astro.WesternNumerologyVastuEngine
import com.example.core.AppLanguage
import com.example.data.PlacesRepository
import com.example.model.BirthInput
import com.example.model.ChartDivision
import com.example.model.DstRule
import com.example.model.PlanetId
import com.example.model.Rashi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VedicCalculatorTest {

    @Test
    fun `india independence chart matches classical lahiri positions`() {
        val delhi = PlacesRepository.defaultPlace
        val input = BirthInput(
            name = "India 1947",
            year = 1947,
            month = 8,
            day = 15,
            hour = 0,
            minute = 0,
            place = delhi
        )
        val result = VedicCalculator.calculateKundali(input)

        assertEquals(Rashi.VRISHABHA, result.lagnaRashi)

        val byId = result.planets.associateBy { it.planet }
        assertEquals(Rashi.KARKA, byId.getValue(PlanetId.SUN).rashi)
        assertEquals(Rashi.KARKA, byId.getValue(PlanetId.MOON).rashi)
        assertEquals(7, byId.getValue(PlanetId.MOON).nakshatraIndex)
        assertEquals(Rashi.MITHUNA, byId.getValue(PlanetId.MARS).rashi)
        assertEquals(Rashi.KARKA, byId.getValue(PlanetId.MERCURY).rashi)
        assertEquals(Rashi.TULA, byId.getValue(PlanetId.JUPITER).rashi)
        assertEquals(Rashi.KARKA, byId.getValue(PlanetId.VENUS).rashi)
        assertEquals(Rashi.KARKA, byId.getValue(PlanetId.SATURN).rashi)
        assertEquals(Rashi.VRISHABHA, byId.getValue(PlanetId.RAHU).rashi)
        assertEquals(Rashi.VRISCHIKA, byId.getValue(PlanetId.KETU).rashi)
    }

    @Test
    fun `known 2025 festival tithis match panchang calculations`() {
        val delhi = PlacesRepository.defaultPlace

        val ramNavami = VedicCalculator.calculateKundali(
            BirthInput("Ram Navami", 2025, 4, 6, 12, 0, delhi)
        )
        assertEquals(9, ramNavami.panchang.tithiNumber)

        val guruPurnima = VedicCalculator.calculateKundali(
            BirthInput("Guru Purnima", 2025, 7, 10, 18, 0, delhi)
        )
        assertEquals(15, guruPurnima.panchang.tithiNumber)

        val janmashtami = VedicCalculator.calculateKundali(
            BirthInput("Janmashtami", 2025, 8, 16, 12, 0, delhi)
        )
        assertEquals(23, janmashtami.panchang.tithiNumber)

        val diwali = VedicCalculator.calculateKundali(
            BirthInput("Diwali", 2025, 10, 20, 19, 0, delhi)
        )
        assertEquals(30, diwali.panchang.tithiNumber)
    }

    @Test
    fun `vimshottari dasha totals 120 years with 9x9x9 pratyantardashas`() {
        val delhi = PlacesRepository.defaultPlace
        val result = VedicCalculator.calculateKundali(
            BirthInput("Test", 1990, 5, 12, 10, 30, delhi)
        )
        assertEquals(9, result.mahadashas.size)
        result.mahadashas.forEach { maha ->
            assertEquals(9, maha.antardashas.size)
            maha.antardashas.forEach { antar ->
                assertEquals(9, antar.pratyantardashas.size)
            }
        }
        val totalDays = result.mahadashas.last().endJd - result.mahadashas.first().startJd
        val totalYears = totalDays / 365.24219878
        assertEquals(120.0, totalYears, 0.01)
    }

    @Test
    fun `shodashvarga computes all 16 charts and ashtakavarga totals 337 bindus`() {
        val delhi = PlacesRepository.defaultPlace
        val result = VedicCalculator.calculateKundali(
            BirthInput("Parashari Full", 1995, 8, 15, 6, 30, delhi)
        )
        assertEquals(16, result.shodashvarga.size)
        assertEquals(ChartDivision.shodashvargaOrder, result.shodashvarga.map { it.division })
        result.shodashvarga.forEach { row ->
            assertEquals(9, row.planetRashis.size)
        }

        val av = result.ashtakavarga
        assertNotNull(av)
        assertEquals(7, av!!.bhinnashtakavargas.size)
        assertEquals(12, av.sarvashtakavargaByRashi.size)
        assertEquals(337, av.totalSarvaBindus)
    }

    @Test
    fun `kp calculator provides 249 sub divisions, 12 cusps and horary support`() {
        assertEquals(249, KpCalculator.table249.size)
        assertEquals(1, KpCalculator.table249.first().number)
        assertEquals(249, KpCalculator.table249.last().number)

        val delhi = PlacesRepository.defaultPlace
        val result = VedicCalculator.calculateKundali(
            BirthInput("KP Prashna", 2026, 2, 10, 11, 15, delhi),
            prashnaNumber = 108
        )
        val kp = result.kpAnalysis
        assertNotNull(kp)
        assertEquals(108, kp!!.prashnaNumber)
        assertEquals(12, kp.cusps.size)
        assertEquals(9, kp.planets.size)
        assertTrue(kp.rulingPlanets.distinctRulers.isNotEmpty())
    }

    @Test
    fun `lal kitab, western, numerology, vastu and matching computations succeed`() {
        val delhi = PlacesRepository.defaultPlace
        val boy = VedicCalculator.calculateKundali(BirthInput("Boy", 1994, 4, 18, 8, 15, delhi))
        val girl = VedicCalculator.calculateKundali(BirthInput("Girl", 1996, 9, 24, 15, 45, delhi))

        assertNotNull(boy.lalKitab)
        assertEquals(9, boy.lalKitab!!.entries.size)
        assertEquals(4, boy.lalKitab!!.rinas.size)

        val safeStoreIds = LalKitabAndStoreEngine.recommendedIdsForChart(boy.summary)
        assertTrue("rud_5mukhi" in safeStoreIds)

        assertNotNull(boy.westernChart)
        assertNotNull(boy.numerology)
        assertNotNull(boy.vastuFengShui)
        assertTrue(boy.numerology!!.mulank in 1..9)
        assertTrue(boy.numerology!!.bhagyank in 1..9)

        val matching = WesternNumerologyVastuEngine.calculateMatching(boy, girl)
        assertEquals(8, matching.koots.size)
        assertEquals(36.0, matching.maxScore, 0.001)
        assertTrue(matching.totalScore in 0.0..36.0)
    }

    @Test
    fun `all ten app languages are configured`() {
        assertEquals(10, AppLanguage.entries.size)
    }

    @Test
    fun `modern dst rules apply properly for US and UK`() {
        val nycSummer = TimeUtils.effectiveUtcOffset(2024, 7, 15, 12, -5.0, DstRule.US_MODERN)
        val nycWinter = TimeUtils.effectiveUtcOffset(2024, 1, 15, 12, -5.0, DstRule.US_MODERN)
        assertEquals(-4.0, nycSummer, 0.001)
        assertEquals(-5.0, nycWinter, 0.001)

        val ukSummer = TimeUtils.effectiveUtcOffset(2024, 6, 20, 12, 0.0, DstRule.UK_MODERN)
        val ukWinter = TimeUtils.effectiveUtcOffset(2024, 12, 20, 12, 0.0, DstRule.UK_MODERN)
        assertEquals(1.0, ukSummer, 0.001)
        assertEquals(0.0, ukWinter, 0.001)
    }

    @Test
    fun `sunrise sunset and shadbala outputs are well formed`() {
        val delhi = PlacesRepository.defaultPlace
        val result = VedicCalculator.calculateKundali(
            BirthInput("Test", 2025, 3, 21, 9, 0, delhi)
        )
        assertTrue(result.panchang.sunriseStr.startsWith("06:"))
        assertTrue(result.panchang.sunsetStr.startsWith("18:"))
        assertEquals(7, result.shadbala.size)
        assertEquals(12, result.bhavaBala.size)
        assertEquals(3, result.threeDayHoroscope.size)
        assertTrue(result.upcomingTransits1Year.isNotEmpty())
    }

    @Test
    fun `places repository loads all Indian districts with unique ids and exact longitudes`() {
        val all = PlacesRepository.allPlaces
        assertTrue(all.size >= 738)
        assertEquals(all.size, all.distinctBy { it.id }.size)

        val adilabad = PlacesRepository.search("Adilabad").first()
        assertEquals(78.5320, adilabad.longitude, 0.0001)

        val varanasi = PlacesRepository.search("Varanasi").first()
        assertEquals(82.9739, varanasi.longitude, 0.0001)

        val westKameng = PlacesRepository.search("West Kameng").first()
        assertEquals(92.4159, westKameng.longitude, 0.0001)

        val custom = PlacesRepository.addCustomPlace("Vrindavan", 27.5806, 77.7006, 5.5)
        assertTrue(PlacesRepository.search("Vrindavan").any { it.id == custom.id })
    }

    @Test
    fun `comprehensive 21-module report bundle generates all engines cleanly`() {
        val delhi = PlacesRepository.defaultPlace
        val result = VedicCalculator.calculateKundali(
            BirthInput("Acharya Demo", 1992, 11, 14, 7, 45, delhi),
            prashnaNumber = 72
        )
        val bundle = com.example.astro.ComprehensiveReportBuilder.build(result)

        assertTrue(bundle.panchangExt.dinmaanStr.isNotBlank())
        assertTrue(bundle.panchangExt.yamagandaStr.isNotBlank())
        assertTrue(bundle.panchangExt.dashaLifeAreaInsights.isNotEmpty())

        assertTrue(bundle.phaladesh.size >= 10)
        assertEquals(24, bundle.yoginiDashas.size)
        assertEquals(8, bundle.yoginiDashas.first().antardashas.size)
        assertTrue(bundle.bcp.currentActiveHouse in 1..12)
        assertTrue(bundle.bcp.rows.size >= 25)

        assertEquals(8, bundle.jaimini.charaKarakas.size)
        assertEquals(12, bundle.jaimini.arudhaPadas.size)
        assertTrue(bundle.jaimini.charaDashas.isNotEmpty())

        assertEquals(9, bundle.specialLagnas.size)
        assertEquals(20, bundle.all20DivisionalCharts.size)
        val divCodes = bundle.all20DivisionalCharts.map { it.code }.toSet()
        assertTrue(listOf("D1", "D6", "D8", "D28", "D50", "D60").all { it in divCodes })

        assertTrue(bundle.vedicYogas.isNotEmpty())
        assertEquals(7, bundle.avShodhanas.size)
        assertEquals(7, bundle.shadbalaExt.size)
        assertTrue(bundle.sbcAndKota.stambhaZone.isNotBlank())

        assertEquals(9, bundle.nadi.rows.size)
        assertTrue(bundle.kpExt.scorecards.isNotEmpty())
        assertTrue(bundle.gemstones.primaryGem.isNotBlank())
        assertEquals(12, bundle.vedicNumerology.monthlyEnergy.size)
        assertTrue(bundle.loShuExt.activePlanes.isNotEmpty() || bundle.loShuExt.weaknessPlanes.isNotEmpty())
        assertEquals(9, bundle.lalKitabExt.varshphal2026Rows.size)
        assertTrue(bundle.bnn.progressionRows.isNotEmpty())
        assertTrue(bundle.tajika2026.muddaDashas.isNotEmpty())
        assertTrue(bundle.crystalAndRemedies.planetMantras.isNotEmpty())
        assertEquals(7, bundle.crystalAndRemedies.weeklyRemedySchedule.size)
    }
}
