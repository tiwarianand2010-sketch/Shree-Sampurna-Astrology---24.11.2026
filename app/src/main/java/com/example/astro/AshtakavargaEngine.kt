package com.example.astro

import com.example.model.AshtakavargaResult
import com.example.model.Bhinnashtakavarga
import com.example.model.PlanetId
import com.example.model.PlanetPosition
import com.example.model.Rashi

/**
 * Classical Brihat Parashara Hora Shastra (BPHS) Bhinnashtakavarga & Sarvashtakavarga calculator.
 * Bindu tables give benefic houses (1-based offset from contributor) for each of the 7 planets.
 * Contributor index order: Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Lagna.
 */
object AshtakavargaEngine {

    private val sunRules = mapOf(
        PlanetId.SUN to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        PlanetId.MOON to listOf(3, 6, 10, 11),
        PlanetId.MARS to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        PlanetId.MERCURY to listOf(3, 5, 6, 9, 10, 11, 12),
        PlanetId.JUPITER to listOf(5, 6, 9, 11),
        PlanetId.VENUS to listOf(6, 7, 12),
        PlanetId.SATURN to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        null to listOf(3, 4, 6, 10, 11, 12) // Lagna
    )

    private val moonRules = mapOf(
        PlanetId.SUN to listOf(3, 6, 7, 8, 10, 11),
        PlanetId.MOON to listOf(1, 3, 6, 7, 10, 11),
        PlanetId.MARS to listOf(2, 3, 5, 6, 9, 10, 11),
        PlanetId.MERCURY to listOf(1, 3, 4, 5, 7, 8, 10, 11),
        PlanetId.JUPITER to listOf(1, 4, 7, 8, 10, 11, 12),
        PlanetId.VENUS to listOf(3, 4, 5, 7, 9, 10, 11),
        PlanetId.SATURN to listOf(3, 5, 6, 11),
        null to listOf(3, 6, 10, 11)
    )

    private val marsRules = mapOf(
        PlanetId.SUN to listOf(3, 5, 6, 10, 11),
        PlanetId.MOON to listOf(3, 6, 11),
        PlanetId.MARS to listOf(1, 2, 4, 7, 8, 10, 11),
        PlanetId.MERCURY to listOf(3, 5, 6, 11),
        PlanetId.JUPITER to listOf(6, 10, 11, 12),
        PlanetId.VENUS to listOf(6, 8, 11, 12),
        PlanetId.SATURN to listOf(1, 4, 7, 8, 9, 10, 11),
        null to listOf(1, 3, 6, 10, 11)
    )

    private val mercuryRules = mapOf(
        PlanetId.SUN to listOf(5, 6, 9, 11, 12),
        PlanetId.MOON to listOf(2, 4, 6, 8, 10, 11),
        PlanetId.MARS to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        PlanetId.MERCURY to listOf(1, 3, 5, 6, 9, 10, 11, 12),
        PlanetId.JUPITER to listOf(6, 8, 11, 12),
        PlanetId.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 11),
        PlanetId.SATURN to listOf(1, 2, 4, 7, 8, 9, 10, 11),
        null to listOf(1, 2, 4, 6, 8, 10, 11)
    )

    private val jupiterRules = mapOf(
        PlanetId.SUN to listOf(1, 2, 3, 4, 7, 8, 9, 10, 11),
        PlanetId.MOON to listOf(2, 5, 7, 9, 11),
        PlanetId.MARS to listOf(1, 2, 4, 7, 8, 10, 11),
        PlanetId.MERCURY to listOf(1, 2, 4, 5, 6, 9, 10, 11),
        PlanetId.JUPITER to listOf(1, 2, 3, 4, 7, 8, 10, 11),
        PlanetId.VENUS to listOf(2, 5, 6, 9, 10, 11),
        PlanetId.SATURN to listOf(3, 5, 6, 12),
        null to listOf(1, 2, 4, 5, 6, 7, 9, 10, 11)
    )

    private val venusRules = mapOf(
        PlanetId.SUN to listOf(8, 11, 12),
        PlanetId.MOON to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
        PlanetId.MARS to listOf(3, 5, 6, 9, 11, 12),
        PlanetId.MERCURY to listOf(3, 5, 6, 9, 11),
        PlanetId.JUPITER to listOf(5, 8, 9, 10, 11),
        PlanetId.VENUS to listOf(1, 2, 3, 4, 5, 8, 9, 10, 11),
        PlanetId.SATURN to listOf(3, 4, 5, 8, 9, 10, 11),
        null to listOf(1, 2, 3, 4, 5, 8, 9, 11)
    )

    private val saturnRules = mapOf(
        PlanetId.SUN to listOf(1, 2, 4, 7, 8, 10, 11),
        PlanetId.MOON to listOf(3, 6, 11),
        PlanetId.MARS to listOf(3, 5, 6, 10, 11, 12),
        PlanetId.MERCURY to listOf(6, 8, 9, 10, 11, 12),
        PlanetId.JUPITER to listOf(5, 6, 11, 12),
        PlanetId.VENUS to listOf(6, 11, 12),
        PlanetId.SATURN to listOf(3, 5, 6, 11),
        null to listOf(1, 3, 4, 6, 10, 11)
    )

    private val allRules = mapOf(
        PlanetId.SUN to sunRules,
        PlanetId.MOON to moonRules,
        PlanetId.MARS to marsRules,
        PlanetId.MERCURY to mercuryRules,
        PlanetId.JUPITER to jupiterRules,
        PlanetId.VENUS to venusRules,
        PlanetId.SATURN to saturnRules
    )

    fun calculate(lagnaRashi: Rashi, planets: List<PlanetPosition>): AshtakavargaResult {
        val seven = listOf(
            PlanetId.SUN, PlanetId.MOON, PlanetId.MARS,
            PlanetId.MERCURY, PlanetId.JUPITER, PlanetId.VENUS, PlanetId.SATURN
        )
        val rashiByPlanet = planets.associate { it.planet to it.rashi.ordinal }

        val bavs = seven.map { targetPlanet ->
            val rules = allRules.getValue(targetPlanet)
            val bindus = IntArray(12) { 0 }

            // Add bindus from 7 planets
            for (contributor in seven) {
                val baseSign = rashiByPlanet.getValue(contributor)
                for (houseOffset in rules.getValue(contributor)) {
                    val targetSign = (baseSign + houseOffset - 1) % 12
                    bindus[targetSign]++
                }
            }
            // Add bindus from Lagna
            for (houseOffset in rules.getValue(null)) {
                val targetSign = (lagnaRashi.ordinal + houseOffset - 1) % 12
                bindus[targetSign]++
            }
            val list = bindus.toList()
            Bhinnashtakavarga(
                planet = targetPlanet,
                bindusByRashi = list,
                totalBindus = list.sum()
            )
        }

        val sarva = IntArray(12) { r ->
            bavs.sumOf { it.bindusByRashi[r] }
        }.toList()

        return AshtakavargaResult(
            bhinnashtakavargas = bavs,
            sarvashtakavargaByRashi = sarva,
            totalSarvaBindus = sarva.sum()
        )
    }
}
