package com.example.astro

import com.example.model.ChartDivision
import com.example.model.PlanetId
import com.example.model.PlanetPosition
import com.example.model.Rashi
import com.example.model.ShodashvargaRow
import kotlin.math.floor

object ShodashvargaEngine {

    fun calculateVargaRashi(siderealLon: Double, division: ChartDivision, moonLon: Double = siderealLon): Rashi {
        val normLon = EphemerisEngine.normalize360(siderealLon)
        val signIdx = floor(normLon / 30.0).toInt() % 12
        val degInSign = normLon - signIdx * 30.0
        val isOdd = (signIdx % 2 == 0) // 0=Mesha (1st=odd), 1=Vrishabha (2nd=even)
        val modality = signIdx % 3     // 0=Movable, 1=Fixed, 2=Dual
        val element = signIdx % 4      // 0=Fire, 1=Earth, 2=Air, 3=Water

        val targetIdx = when (division) {
            ChartDivision.LAGNA_D1 -> signIdx
            ChartDivision.CHANDRA -> floor(EphemerisEngine.normalize360(moonLon) / 30.0).toInt() % 12
            ChartDivision.HORA_D2 -> {
                val firstHalf = degInSign < 15.0
                if (isOdd) {
                    if (firstHalf) Rashi.SIMHA.ordinal else Rashi.KARKA.ordinal
                } else {
                    if (firstHalf) Rashi.KARKA.ordinal else Rashi.SIMHA.ordinal
                }
            }
            ChartDivision.DREKKANA_D3 -> {
                val part = floor(degInSign / 10.0).toInt().coerceIn(0, 2)
                (signIdx + part * 4) % 12
            }
            ChartDivision.CHATURTHAMSA_D4 -> {
                val part = floor(degInSign / 7.5).toInt().coerceIn(0, 3)
                (signIdx + part * 3) % 12
            }
            ChartDivision.SAPTAMSA_D7 -> {
                val part = floor(degInSign / (30.0 / 7.0)).toInt().coerceIn(0, 6)
                val start = if (isOdd) signIdx else (signIdx + 6) % 12
                (start + part) % 12
            }
            ChartDivision.NAVAMSA_D9 -> {
                floor(normLon / (30.0 / 9.0)).toInt() % 12
            }
            ChartDivision.DASAMSA_D10 -> {
                val part = floor(degInSign / 3.0).toInt().coerceIn(0, 9)
                val start = if (isOdd) signIdx else (signIdx + 8) % 12
                (start + part) % 12
            }
            ChartDivision.DWADASAMSA_D12 -> {
                val part = floor(degInSign / 2.5).toInt().coerceIn(0, 11)
                (signIdx + part) % 12
            }
            ChartDivision.SHODASAMSA_D16 -> {
                val part = floor(degInSign / (30.0 / 16.0)).toInt().coerceIn(0, 15)
                val start = when (modality) {
                    0 -> Rashi.MESHA.ordinal
                    1 -> Rashi.SIMHA.ordinal
                    else -> Rashi.DHANU.ordinal
                }
                (start + part) % 12
            }
            ChartDivision.VIMSAMSA_D20 -> {
                val part = floor(degInSign / 1.5).toInt().coerceIn(0, 19)
                val start = when (modality) {
                    0 -> Rashi.MESHA.ordinal
                    1 -> Rashi.DHANU.ordinal
                    else -> Rashi.SIMHA.ordinal
                }
                (start + part) % 12
            }
            ChartDivision.CHATURVIMSAMSA_D24 -> {
                val part = floor(degInSign / 1.25).toInt().coerceIn(0, 23)
                val start = if (isOdd) Rashi.SIMHA.ordinal else Rashi.KARKA.ordinal
                (start + part) % 12
            }
            ChartDivision.SAPTAVIMSAMSA_D27 -> {
                val part = floor(degInSign / (30.0 / 27.0)).toInt().coerceIn(0, 26)
                val start = when (element) {
                    0 -> Rashi.MESHA.ordinal
                    1 -> Rashi.KARKA.ordinal
                    2 -> Rashi.TULA.ordinal
                    else -> Rashi.MAKARA.ordinal
                }
                (start + part) % 12
            }
            ChartDivision.TRIMSAMSA_D30 -> {
                if (isOdd) {
                    when {
                        degInSign < 5.0 -> Rashi.MESHA.ordinal
                        degInSign < 10.0 -> Rashi.KUMBHA.ordinal
                        degInSign < 18.0 -> Rashi.DHANU.ordinal
                        degInSign < 25.0 -> Rashi.MITHUNA.ordinal
                        else -> Rashi.TULA.ordinal
                    }
                } else {
                    when {
                        degInSign < 5.0 -> Rashi.VRISHABHA.ordinal
                        degInSign < 12.0 -> Rashi.KANYA.ordinal
                        degInSign < 20.0 -> Rashi.MEENA.ordinal
                        degInSign < 25.0 -> Rashi.MAKARA.ordinal
                        else -> Rashi.VRISCHIKA.ordinal
                    }
                }
            }
            ChartDivision.KHAVEDAMSA_D40 -> {
                val part = floor(degInSign / 0.75).toInt().coerceIn(0, 39)
                val start = if (isOdd) Rashi.MESHA.ordinal else Rashi.TULA.ordinal
                (start + part) % 12
            }
            ChartDivision.AKSHAVEDAMSA_D45 -> {
                val part = floor(degInSign / (30.0 / 45.0)).toInt().coerceIn(0, 44)
                val start = when (modality) {
                    0 -> Rashi.MESHA.ordinal
                    1 -> Rashi.SIMHA.ordinal
                    else -> Rashi.DHANU.ordinal
                }
                (start + part) % 12
            }
            ChartDivision.SHASHTIAMSA_D60 -> {
                val part = floor(degInSign / 0.5).toInt().coerceIn(0, 59)
                (signIdx + part) % 12
            }
        }
        return Rashi.fromIndex(targetIdx)
    }

    fun computeAllShodashvarga(
        lagnaLon: Double,
        planets: List<PlanetPosition>
    ): List<ShodashvargaRow> {
        return ChartDivision.shodashvargaOrder.map { div ->
            val lagnaR = calculateVargaRashi(lagnaLon, div)
            val map = planets.associate { p ->
                p.planet to calculateVargaRashi(p.siderealLongitude, div)
            }
            ShodashvargaRow(
                division = div,
                lagnaRashi = lagnaR,
                planetRashis = map
            )
        }
    }
}
