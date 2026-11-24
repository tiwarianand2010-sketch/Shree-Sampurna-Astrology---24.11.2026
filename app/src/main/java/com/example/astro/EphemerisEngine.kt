package com.example.astro

import com.example.model.PlanetId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Pure-Kotlin Vedic astronomical ephemeris engine using truncated VSOP87 / Standish
 * orbital elements and Meeus lunar & solar perturbation series with Lahiri Ayanamsa.
 */
object EphemerisEngine {

    private const val J2000 = 2451545.0

    fun normalize360(deg: Double): Double {
        val r = deg % 360.0
        return if (r < 0.0) r + 360.0 else r
    }

    private fun toRad(deg: Double): Double = deg * PI / 180.0
    private fun toDeg(rad: Double): Double = rad * 180.0 / PI

    /**
     * Lahiri Chitrapaksha Ayanamsa in degrees for UT Julian Day.
     * Calibrated to ~23°51'25" at J2000.0 (Spica at 180° sidereal).
     */
    fun lahiriAyanamsa(jd: Double): Double {
        val tYears = (jd - J2000) / 365.25
        val tCent = tYears / 100.0
        return 23.8530556 + (50.290966 / 3600.0) * tYears + 0.000111 * tCent * tCent
    }

    /**
     * True obliquity of the ecliptic in degrees.
     */
    fun obliquityDeg(jd: Double): Double {
        val t = (jd - J2000) / 36525.0
        return 23.4392911 - 0.013004167 * t - 1.64e-7 * t * t + 5.04e-7 * t * t * t
    }

    private data class SunGeo(val trueLonDeg: Double, val appLonDeg: Double, val radiusAu: Double)

    private fun computeSunGeo(jd: Double): SunGeo {
        val t = (jd - J2000) / 36525.0
        val l0 = normalize360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = normalize360(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val mRad = toRad(m)
        val e = 0.016708634 - 0.000042037 * t - 0.0000001267 * t * t
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2.0 * mRad) +
            0.000289 * sin(3.0 * mRad)
        val trueLon = normalize360(l0 + c)
        val trueAnomaly = toRad(m + c)
        val r = (1.000001018 * (1.0 - e * e)) / (1.0 + e * cos(trueAnomaly))
        val omega = toRad(125.04 - 1934.136 * t)
        val appLon = normalize360(trueLon - 0.00569 - 0.00478 * sin(omega))
        return SunGeo(trueLon, appLon, r)
    }

    fun sunTropicalLongitude(jd: Double): Double = computeSunGeo(jd).appLonDeg

    /**
     * Meeus Ch. 47 Lunar Longitude (truncated to top 28 periodic terms -> ~0.01 deg accuracy).
     */
    fun moonTropicalLongitude(jd: Double): Double {
        val t = (jd - J2000) / 36525.0
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        val lPrime = normalize360(
            218.3164477 + 481267.88123421 * t - 0.0015786 * t2 + t3 / 538841.0 - t4 / 65194000.0
        )
        val d = normalize360(
            297.8501921 + 445267.1114034 * t - 0.0018819 * t2 + t3 / 545868.0 - t4 / 113065000.0
        )
        val m = normalize360(
            357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0
        )
        val mPrime = normalize360(
            134.9633964 + 477198.8675055 * t + 0.0087414 * t2 + t3 / 69699.0 - t4 / 14712000.0
        )
        val f = normalize360(
            93.2720950 + 483202.0175233 * t - 0.0036539 * t2 - t3 / 3526000.0 + t4 / 863310000.0
        )

        val a1 = toRad(119.75 + 131.849 * t)
        val a2 = toRad(53.09 + 479264.290 * t)
        val eCorr = 1.0 - 0.002516 * t - 0.0000074 * t2

        val dR = toRad(d)
        val mR = toRad(m)
        val mpR = toRad(mPrime)
        val fR = toRad(f)
        val lR = toRad(lPrime)

        var sumL = 0.0
        sumL += 6288774.0 * sin(mpR)
        sumL += 1274027.0 * sin(2 * dR - mpR)
        sumL += 658314.0 * sin(2 * dR)
        sumL += 213618.0 * sin(2 * mpR)
        sumL -= 185116.0 * eCorr * sin(mR)
        sumL -= 114332.0 * sin(2 * fR)
        sumL += 58793.0 * sin(2 * dR - 2 * mpR)
        sumL += 57066.0 * eCorr * sin(2 * dR - mR - mpR)
        sumL += 53322.0 * sin(2 * dR + mpR)
        sumL += 45758.0 * eCorr * sin(2 * dR - mR)
        sumL -= 40923.0 * eCorr * sin(mR - mpR)
        sumL -= 34720.0 * sin(dR)
        sumL -= 30383.0 * eCorr * sin(mR + mpR)
        sumL += 15327.0 * sin(2 * dR - 2 * fR)
        sumL -= 12528.0 * sin(mpR + 2 * fR)
        sumL += 10980.0 * sin(mpR - 2 * fR)
        sumL += 10675.0 * sin(4 * dR - mpR)
        sumL += 10034.0 * sin(3 * mpR)
        sumL += 8548.0 * sin(4 * dR - 2 * mpR)
        sumL -= 7888.0 * eCorr * sin(2 * dR + mR - mpR)
        sumL -= 6766.0 * eCorr * sin(2 * dR + mR)
        sumL -= 5163.0 * sin(dR - mpR)
        sumL += 4987.0 * eCorr * sin(dR + mR)
        sumL += 4036.0 * eCorr * sin(2 * dR - mR + mpR)
        sumL += 3994.0 * sin(2 * dR + 2 * mpR)
        sumL += 3861.0 * sin(4 * dR)
        sumL += 3665.0 * sin(2 * dR - 3 * mpR)
        sumL -= 2689.0 * eCorr * sin(mR - 2 * mpR)
        sumL += 3958.0 * sin(a1)
        sumL += 1962.0 * sin(lR - fR)
        sumL += 318.0 * sin(a2)

        return normalize360(lPrime + sumL / 1_000_000.0)
    }

    private data class OrbitalElements(
        val a: Double, val aDot: Double,
        val e: Double, val eDot: Double,
        val i: Double, val iDot: Double,
        val l: Double, val lDot: Double,
        val wBar: Double, val wBarDot: Double,
        val omega: Double, val omegaDot: Double,
        val b: Double = 0.0, val c: Double = 0.0, val s: Double = 0.0, val f: Double = 0.0
    )

    // JPL Standish Keplerian elements (1800 AD - 2050 AD)
    private val planetElements = mapOf(
        PlanetId.MERCURY to OrbitalElements(
            0.38709927, 0.00000037,
            0.20563593, 0.00001906,
            7.00497902, -0.00594749,
            252.25032350, 149472.67411175,
            77.45779628, 0.16047689,
            48.33076593, -0.12534081
        ),
        PlanetId.VENUS to OrbitalElements(
            0.72333566, 0.00000390,
            0.00677672, -0.00004107,
            3.39467605, -0.00078890,
            181.97909950, 58517.81538729,
            131.60246718, 0.00268329,
            76.67984255, -0.27769418
        ),
        PlanetId.MARS to OrbitalElements(
            1.52371034, 0.00001847,
            0.09339410, 0.00007882,
            1.84969142, -0.00813131,
            -4.55343205, 19140.30268499,
            -23.94362959, 0.44441088,
            49.55953891, -0.29257343
        ),
        PlanetId.JUPITER to OrbitalElements(
            5.20288700, -0.00011607,
            0.04838624, -0.00013253,
            1.30439695, -0.00183714,
            34.39644051, 3034.74612775,
            14.72847983, 0.21252668,
            100.47390909, 0.20469106,
            b = -0.00012452, c = 0.06064060, s = -0.35635438, f = 38.35125000
        ),
        PlanetId.SATURN to OrbitalElements(
            9.53667594, -0.00125060,
            0.05386179, -0.00050991,
            2.48599187, 0.00193609,
            49.95424423, 1222.49362201,
            92.59887831, -0.41897216,
            113.66242448, -0.28867794,
            b = 0.00025899, c = -0.13434469, s = 0.87320147, f = 38.35125000
        )
    )

    private fun heliocentricEclipticXYZ(planet: PlanetId, jd: Double): Triple<Double, Double, Double> {
        val el = planetElements.getValue(planet)
        val t = (jd - J2000) / 36525.0

        val a = el.a + el.aDot * t
        val e = el.e + el.eDot * t
        val iRad = toRad(el.i + el.iDot * t)
        val l = el.l + el.lDot * t
        val wBar = el.wBar + el.wBarDot * t
        val omegaRad = toRad(el.omega + el.omegaDot * t)
        val argPeriRad = toRad(wBar - el.omega - el.omegaDot * t)

        var mDeg = l - wBar + el.b * t * t +
            el.c * cos(toRad(el.f * t)) +
            el.s * sin(toRad(el.f * t))
        mDeg = ((mDeg % 360.0) + 360.0) % 360.0
        if (mDeg > 180.0) mDeg -= 360.0
        val mRad = toRad(mDeg)

        // Solve Kepler's Equation: E - e*sin(E) = M
        var eccAnom = mRad + e * sin(mRad)
        for (iter in 0 until 8) {
            val deltaM = mRad - (eccAnom - e * sin(eccAnom))
            val deltaE = deltaM / (1.0 - e * cos(eccAnom))
            eccAnom += deltaE
            if (abs(deltaE) < 1e-10) break
        }

        val xOrb = a * (cos(eccAnom) - e)
        val yOrb = a * sqrt(1.0 - e * e) * sin(eccAnom)

        val cosW = cos(argPeriRad)
        val sinW = sin(argPeriRad)
        val cosO = cos(omegaRad)
        val sinO = sin(omegaRad)
        val cosI = cos(iRad)

        val xEcl = (cosW * cosO - sinW * sinO * cosI) * xOrb +
            (-sinW * cosO - cosW * sinO * cosI) * yOrb
        val yEcl = (cosW * sinO + sinW * cosO * cosI) * xOrb +
            (-sinW * sinO + cosW * cosO * cosI) * yOrb
        val zEcl = (sinW * sin(iRad)) * xOrb + (cosW * sin(iRad)) * yOrb

        return Triple(xEcl, yEcl, zEcl)
    }

    fun planetTropicalLongitude(planet: PlanetId, jd: Double): Double {
        return when (planet) {
            PlanetId.SUN -> sunTropicalLongitude(jd)
            PlanetId.MOON -> moonTropicalLongitude(jd)
            PlanetId.RAHU -> {
                val t = (jd - J2000) / 36525.0
                normalize360(125.04452 - 1934.136261 * t + 0.0020708 * t * t + t * t * t / 450000.0)
            }
            PlanetId.KETU -> normalize360(planetTropicalLongitude(PlanetId.RAHU, jd) + 180.0)
            else -> {
                val sun = computeSunGeo(jd)
                val earthLonRad = toRad(normalize360(sun.trueLonDeg + 180.0))
                val xEarth = sun.radiusAu * cos(earthLonRad)
                val yEarth = sun.radiusAu * sin(earthLonRad)
                val (xp, yp, _) = heliocentricEclipticXYZ(planet, jd)
                val xGeo = xp - xEarth
                val yGeo = yp - yEarth
                normalize360(toDeg(atan2(yGeo, xGeo)))
            }
        }
    }

    fun planetSiderealLongitude(planet: PlanetId, jd: Double): Double {
        val ayanamsa = lahiriAyanamsa(jd)
        return normalize360(planetTropicalLongitude(planet, jd) - ayanamsa)
    }

    /**
     * Computes Ascendant (Lagna) sidereal longitude in degrees.
     */
    fun lagnaSiderealLongitude(jd: Double, latitudeDeg: Double, longitudeDeg: Double): Double {
        val t = (jd - J2000) / 36525.0
        val gmstDeg = normalize360(
            280.46061837 +
                360.98564736629 * (jd - J2000) +
                0.000387933 * t * t -
                t * t * t / 38710000.0
        )
        val lstRad = toRad(normalize360(gmstDeg + longitudeDeg))
        val epsRad = toRad(obliquityDeg(jd))
        val latRad = toRad(latitudeDeg.coerceIn(-66.0, 66.0))

        val y = cos(lstRad)
        val x = -(sin(lstRad) * cos(epsRad) + tan(latRad) * sin(epsRad))
        val ascTropical = normalize360(toDeg(atan2(y, x)))
        return normalize360(ascTropical - lahiriAyanamsa(jd))
    }

    /**
     * Computes local Sunrise and Sunset in hours [0..24] for a local calendar date.
     */
    fun sunriseAndSunsetHours(
        year: Int,
        month: Int,
        day: Int,
        latitudeDeg: Double,
        longitudeDeg: Double,
        utcOffsetHours: Double
    ): Pair<Double, Double> {
        // Approximate solar noon in UT
        val jdNoon = TimeUtils.toJulianDay(year, month, day, 12, 0, 0.0, utcOffsetHours)
        val sunLon = toRad(sunTropicalLongitude(jdNoon))
        val eps = toRad(obliquityDeg(jdNoon))
        val sinDec = sin(eps) * sin(sunLon)
        val dec = kotlin.math.asin(sinDec.coerceIn(-1.0, 1.0))

        // Equation of time (minutes) via Meeus approximation
        val t = (jdNoon - J2000) / 36525.0
        val l0 = toRad(normalize360(280.46646 + 36000.76983 * t))
        val y = tan(eps / 2.0) * tan(eps / 2.0)
        val m = toRad(normalize360(357.52911 + 35999.05029 * t))
        val e = 0.016708634 - 0.000042037 * t
        val eotRad = y * sin(2.0 * l0) - 2.0 * e * sin(m) +
            4.0 * e * y * sin(m) * cos(2.0 * l0) -
            0.5 * y * y * sin(4.0 * l0) -
            1.25 * e * e * sin(2.0 * m)
        val eotMinutes = toDeg(eotRad) * 4.0

        val latRad = toRad(latitudeDeg.coerceIn(-65.0, 65.0))
        val cosZenith = cos(toRad(90.833))
        val cosH0 = (cosZenith - sin(latRad) * sin(dec)) / (cos(latRad) * cos(dec))
        val h0Deg = toDeg(acos(cosH0.coerceIn(-0.999, 0.999)))

        val solarNoonMin = 720.0 - 4.0 * longitudeDeg - eotMinutes + utcOffsetHours * 60.0
        val sunriseHours = (solarNoonMin - h0Deg * 4.0) / 60.0
        val sunsetHours = (solarNoonMin + h0Deg * 4.0) / 60.0
        return sunriseHours to sunsetHours
    }
}
