package com.example.astro

import com.example.core.AppLanguage
import com.example.core.AstroStrings
import com.example.model.AshtakavargaShodhanaRow
import com.example.model.Dignity
import com.example.model.KundaliResult
import com.example.model.PlanetId
import com.example.model.Rashi
import com.example.model.SarvatobhadraAndKotaReport
import com.example.model.ShadbalaExtendedRow
import com.example.model.VedicYogaEntry
import kotlin.math.abs

object YogaAndChakraEngine {

    fun computeVedicYogas(result: KundaliResult): List<VedicYogaEntry> {
        val byId = result.planets.associateBy { it.planet }
        val kendras = setOf(1, 4, 7, 10)
        val trikonas = setOf(1, 5, 9)
        val upachayas = setOf(3, 6, 10, 11)
        val trik = setOf(6, 8, 12)
        val strongDignities = setOf(Dignity.EXALTED, Dignity.MOOLTRIKONA, Dignity.OWN_SIGN)

        val sun = byId.getValue(PlanetId.SUN)
        val moon = byId.getValue(PlanetId.MOON)
        val mars = byId.getValue(PlanetId.MARS)
        val mer = byId.getValue(PlanetId.MERCURY)
        val jup = byId.getValue(PlanetId.JUPITER)
        val ven = byId.getValue(PlanetId.VENUS)
        val sat = byId.getValue(PlanetId.SATURN)

        val list = mutableListOf<VedicYogaEntry>()

        // 1. Pancha Mahapurusha Yogas
        val mahapurusha = listOf(
            Triple(mars, "Ruchaka Mahapurusha Yoga", "Courage, executive command, property & decisive victory."),
            Triple(mer, "Bhadra Mahapurusha Yoga", "Sharp intellect, eloquence, commerce & analytical mastery."),
            Triple(jup, "Hamsa Mahapurusha Yoga", "Wisdom, spiritual grace, philanthropy & respected counsel."),
            Triple(ven, "Malavya Mahapurusha Yoga", "Artistic refinement, luxury, marital harmony & charm."),
            Triple(sat, "Shasha Mahapurusha Yoga", "Organizational endurance, authority over large teams & justice.")
        )
        mahapurusha.forEach { (pl, name, effect) ->
            if (pl.houseFromLagna in kendras && pl.dignity in strongDignities) {
                list.add(
                    VedicYogaEntry(
                        name = name,
                        category = "Pancha Mahapurusha",
                        strength = if (pl.dignity == Dignity.EXALTED) "Strong (Exalted)" else "Full (Own/Mooltrikona)",
                        planets = pl.planet.name,
                        condition = "${pl.planet.name} in Kendra House ${pl.houseFromLagna} (${pl.rashi.name}, ${pl.dignity.name})",
                        effect = effect
                    )
                )
            }
        }

        // 2. Gajakesari Yoga
        val jupFromMoon = ((jup.rashi.ordinal - moon.rashi.ordinal + 12) % 12) + 1
        if (jupFromMoon in kendras) {
            list.add(
                VedicYogaEntry(
                    name = "Gajakesari Yoga",
                    category = "Prosperity & Fame",
                    strength = if (jup.dignity in strongDignities || moon.dignity in strongDignities) "High" else "Moderate",
                    planets = "JUPITER + MOON",
                    condition = "Jupiter is in House $jupFromMoon (Kendra) from Moon",
                    effect = "Lasting reputation, dignified conduct, generous wisdom & financial resilience."
                )
            )
        }

        // 3. Budhaditya Yoga
        if (sun.rashi == mer.rashi) {
            list.add(
                VedicYogaEntry(
                    name = "Budhaditya Yoga",
                    category = "Intellect & Status",
                    strength = if (!mer.isCombust) "High" else "Mild (Mercury near Sun)",
                    planets = "SUN + MERCURY",
                    condition = "Sun and Mercury conjoin in ${sun.rashi.name} (House ${sun.houseFromLagna})",
                    effect = "Administrative brilliance, communication mastery & academic/professional recognition."
                )
            )
        }

        // 4. Sunapha, Anapha, Durudhara from Moon
        val moon2nd = result.planets.filter { it.planet !in listOf(PlanetId.SUN, PlanetId.RAHU, PlanetId.KETU) && it.houseFromMoon == 2 }
        val moon12th = result.planets.filter { it.planet !in listOf(PlanetId.SUN, PlanetId.RAHU, PlanetId.KETU) && it.houseFromMoon == 12 }
        if (moon2nd.isNotEmpty() && moon12th.isNotEmpty()) {
            list.add(
                VedicYogaEntry(
                    name = "Durudhara Yoga",
                    category = "Chandra Yoga",
                    strength = "Strong",
                    planets = (moon2nd + moon12th).joinToString(", ") { it.planet.name },
                    condition = "Planets flank both 2nd & 12th from Moon",
                    effect = "Balanced abundance, supportive allies, assets & comfortable lifestyle."
                )
            )
        } else if (moon2nd.isNotEmpty()) {
            list.add(
                VedicYogaEntry(
                    name = "Sunapha Yoga",
                    category = "Chandra Yoga",
                    strength = "Active",
                    planets = moon2nd.joinToString(", ") { it.planet.name },
                    condition = "Planets occupy 2nd house from Moon",
                    effect = "Self-earned wealth, steady acumen & resourceful entrepreneurship."
                )
            )
        } else if (moon12th.isNotEmpty()) {
            list.add(
                VedicYogaEntry(
                    name = "Anapha Yoga",
                    category = "Chandra Yoga",
                    strength = "Active",
                    planets = moon12th.joinToString(", ") { it.planet.name },
                    condition = "Planets occupy 12th house from Moon",
                    effect = "Polished personality, spiritual detachment & serene charisma."
                )
            )
        }

        // 5. Adhi Yoga (Benefics in 6,7,8 from Moon or Lagna)
        val benefics = listOf(mer, jup, ven)
        val adhiMoon = benefics.filter { it.houseFromMoon in listOf(6, 7, 8) }
        if (adhiMoon.size >= 2) {
            list.add(
                VedicYogaEntry(
                    name = "Chandra Adhi Yoga",
                    category = "Leadership",
                    strength = if (adhiMoon.size == 3) "Full" else "Moderate",
                    planets = adhiMoon.joinToString(", ") { it.planet.name },
                    condition = "${adhiMoon.size} natural benefics in 6/7/8 from Moon",
                    effect = "Leadership trust, triumph over opposition & ministerial poise."
                )
            )
        }

        // 6. Vesi, Vosi, Ubhayachari from Sun
        val sun2nd = result.planets.filter { it.planet !in listOf(PlanetId.MOON, PlanetId.RAHU, PlanetId.KETU) && ((it.rashi.ordinal - sun.rashi.ordinal + 12) % 12) + 1 == 2 }
        val sun12th = result.planets.filter { it.planet !in listOf(PlanetId.MOON, PlanetId.RAHU, PlanetId.KETU) && ((it.rashi.ordinal - sun.rashi.ordinal + 12) % 12) + 1 == 12 }
        if (sun2nd.isNotEmpty() && sun12th.isNotEmpty()) {
            list.add(VedicYogaEntry("Ubhayachari Yoga", "Surya Yoga", "Strong", (sun2nd + sun12th).joinToString(",") { it.planet.name }, "Planets in 2nd & 12th from Sun", "Eloquent diplomacy, versatility & broad social reach."))
        } else if (sun2nd.isNotEmpty()) {
            list.add(VedicYogaEntry("Vesi Yoga", "Surya Yoga", "Active", sun2nd.joinToString(",") { it.planet.name }, "Planets in 2nd from Sun", "Truthful character, balanced temperament & steady focus."))
        } else if (sun12th.isNotEmpty()) {
            list.add(VedicYogaEntry("Vosi Yoga", "Surya Yoga", "Active", sun12th.joinToString(",") { it.planet.name }, "Planets in 12th from Sun", "Skillful craftsmanship, generosity & intellectual depth."))
        }

        // 7. Vasumati Yoga (Benefics in Upachayas 3,6,10,11)
        val vasu = benefics.filter { it.houseFromLagna in upachayas || it.houseFromMoon in upachayas }
        if (vasu.size >= 2) {
            list.add(VedicYogaEntry("Vasumati Yoga", "Wealth", "Active", vasu.joinToString(", ") { it.planet.name }, "Natural benefics occupy Upachaya houses (3/6/10/11)", "Progressive wealth accumulation and material stability at home."))
        }

        // 8. Rajyoga (Kendra-Trikona Lord Association) & Dhana Yoga
        val houseLords = (1..12).associateWith { h -> Rashi.fromIndex(result.lagnaRashi.ordinal + h - 1).lord }
        val kendraLords = listOf(1, 4, 7, 10).map { houseLords.getValue(it) }.toSet()
        val trikonaLords = listOf(1, 5, 9).map { houseLords.getValue(it) }.toSet()
        for (kl in kendraLords) {
            for (tl in trikonaLords) {
                if (kl == tl || (kl != tl && byId.getValue(kl).rashi == byId.getValue(tl).rashi)) {
                    val name = if (kl == tl) "Yogakaraka Rajyoga ($kl)" else "Dharma-Karmadhipati / Rajyoga ($kl + $tl)"
                    if (list.none { it.name == name }) {
                        list.add(
                            VedicYogaEntry(
                                name = name,
                                category = "Rajyoga",
                                strength = "High",
                                planets = if (kl == tl) kl.name else "${kl.name} + ${tl.name}",
                                condition = "Kendra-Trikona lord unity/conjunction",
                                effect = "Elevates authority, career milestones & auspicious fortune during their Dashas."
                            )
                        )
                    }
                }
            }
        }

        val h2Lord = houseLords.getValue(2)
        val h11Lord = houseLords.getValue(11)
        val h5Lord = houseLords.getValue(5)
        val h9Lord = houseLords.getValue(9)
        if (byId.getValue(h2Lord).houseFromLagna in listOf(2, 5, 9, 11) || byId.getValue(h11Lord).rashi == byId.getValue(h2Lord).rashi) {
            list.add(
                VedicYogaEntry(
                    name = "Dhana Yoga (Wealth Alliance)",
                    category = "Wealth",
                    strength = "Active",
                    planets = "${h2Lord.name} / ${h11Lord.name}",
                    condition = "2nd/11th lords link wealth & trine houses",
                    effect = "Strong capacity for savings, asset building & multi-stream income."
                )
            )
        }

        // 9. Saraswati Yoga
        if (jup.houseFromLagna in (kendras + trikonas + 2) &&
            ven.houseFromLagna in (kendras + trikonas + 2) &&
            mer.houseFromLagna in (kendras + trikonas + 2) &&
            jup.dignity != Dignity.DEBILITATED
        ) {
            list.add(
                VedicYogaEntry(
                    name = "Saraswati Yoga",
                    category = "Learning & Arts",
                    strength = "Strong",
                    planets = "JUPITER, VENUS, MERCURY",
                    condition = "Jupiter, Venus & Mercury occupy Kendras/Trikonas/2nd",
                    effect = "Eloquent writing, scholarly fame, poetic intuition & mastery of disciplines."
                )
            )
        }

        // 10. Vipareeta Rajyoga
        listOf(6 to "Harsha Yoga", 8 to "Sarala Yoga", 12 to "Vimala Yoga").forEach { (h, yName) ->
            val lord = houseLords.getValue(h)
            val lPos = byId.getValue(lord)
            if (lPos.houseFromLagna in trik) {
                list.add(
                    VedicYogaEntry(
                        name = "$yName (Vipareeta Rajyoga)",
                        category = "Resilience",
                        strength = "Active",
                        planets = lord.name,
                        condition = "House $h lord ${lord.name} placed in House ${lPos.houseFromLagna}",
                        effect = "Turns adversity into unexpected victory and builds fearless stamina."
                    )
                )
            }
        }

        // 11. Neechabhanga Rajyoga
        result.planets.filter { it.dignity == Dignity.DEBILITATED }.forEach { deb ->
            val signLordPos = byId.getValue(deb.rashi.lord)
            if (signLordPos.houseFromLagna in kendras || signLordPos.houseFromMoon in kendras || signLordPos.dignity == Dignity.EXALTED) {
                list.add(
                    VedicYogaEntry(
                        name = "Neechabhanga Rajyoga (${deb.planet.name})",
                        category = "Cancellation & Rise",
                        strength = "Restorative",
                        planets = "${deb.planet.name} & ${deb.rashi.lord.name}",
                        condition = "Debilitation lord ${deb.rashi.lord.name} occupies Kendra/Exaltation",
                        effect = "Early hurdles transform into late-blooming strength and maturity."
                    )
                )
            }
        }

        // 12. Lakshmi Yoga
        if (ven.dignity in strongDignities && byId.getValue(h9Lord).houseFromLagna in kendras) {
            list.add(
                VedicYogaEntry(
                    name = "Lakshmi Yoga",
                    category = "Prosperity",
                    strength = "Strong",
                    planets = "VENUS & ${h9Lord.name}",
                    condition = "Venus in own/exalted sign and 9th lord strong in Kendra",
                    effect = "Refined prosperity, graceful leadership & generous fortune."
                )
            )
        }

        if (list.isEmpty()) {
            list.add(
                VedicYogaEntry(
                    name = "Sankhya / Balanced Graha Yoga",
                    category = "Structural",
                    strength = "Balanced",
                    planets = "All 9 Planets",
                    condition = "Spread planetary distribution across signs",
                    effect = "Versatile adaptability across multiple walks of life without extreme imbalances."
                )
            )
        }
        return list
    }

    fun computeAshtakavargaReductions(result: KundaliResult): Pair<List<Int>, List<AshtakavargaShodhanaRow>> {
        val av = result.ashtakavarga ?: return List(12) { 0 } to emptyList()
        val rashiMultipliers = intArrayOf(7, 10, 8, 4, 10, 5, 7, 8, 9, 5, 11, 12)
        val grahaMultipliers = mapOf(
            PlanetId.SUN to 5, PlanetId.MOON to 5, PlanetId.MARS to 8,
            PlanetId.MERCURY to 5, PlanetId.JUPITER to 10, PlanetId.VENUS to 7, PlanetId.SATURN to 5
        )
        val byId = result.planets.associateBy { it.planet }
        val ekadhipatyaPairs = listOf(0 to 7, 1 to 6, 2 to 5, 8 to 11, 9 to 10)

        val reducedSarva = IntArray(12)
        val rows = av.bhinnashtakavargas.map { bav ->
            val arr = bav.bindusByRashi.toIntArray()
            // 1. Trikona Shodhana on 4 trikona groups: (0,4,8), (1,5,9), (2,6,10), (3,7,11)
            for (g in 0 until 4) {
                val i1 = g; val i2 = g + 4; val i3 = g + 8
                val minVal = minOf(arr[i1], arr[i2], arr[i3])
                arr[i1] -= minVal
                arr[i2] -= minVal
                arr[i3] -= minVal
            }
            val trikonaTotal = arr.sum()

            // 2. Ekadhipatya Shodhana (pair signs Mars/Venus/Merc/Jup/Sat)
            for ((s1, s2) in ekadhipatyaPairs) {
                val occ1 = result.planets.any { it.planet.index < 7 && it.rashi.ordinal == s1 }
                val occ2 = result.planets.any { it.planet.index < 7 && it.rashi.ordinal == s2 }
                val a1 = arr[s1]; val a2 = arr[s2]
                if (!occ1 && !occ2) {
                    if (a1 == a2) { arr[s1] = 0; arr[s2] = 0 }
                    else { val m = minOf(a1, a2); arr[s1] = m; arr[s2] = m }
                } else if (!occ1 && occ2 && a1 < a2) {
                    arr[s1] = 0
                } else if (occ1 && !occ2 && a2 < a1) {
                    arr[s2] = 0
                }
            }
            val ekTotal = arr.sum()
            for (i in 0 until 12) reducedSarva[i] += arr[i]

            var rPinda = 0
            for (i in 0 until 12) rPinda += arr[i] * rashiMultipliers[i]

            var gPinda = 0
            for ((pl, mult) in grahaMultipliers) {
                val sIdx = byId.getValue(pl).rashi.ordinal
                gPinda += arr[sIdx] * mult
            }

            AshtakavargaShodhanaRow(
                planet = bav.planet,
                rawTotal = bav.totalBindus,
                trikonaReducedTotal = trikonaTotal,
                ekadhipatyaReducedTotal = ekTotal,
                rashiPinda = rPinda,
                grahaPinda = gPinda,
                shodhyaPinda = rPinda + gPinda
            )
        }
        return reducedSarva.toList() to rows
    }

    fun computeExtendedShadbala(result: KundaliResult): List<ShadbalaExtendedRow> {
        val reqRupas = mapOf(
            PlanetId.SUN to 6.5, PlanetId.MOON to 6.0, PlanetId.MARS to 5.0,
            PlanetId.MERCURY to 7.0, PlanetId.JUPITER to 6.5, PlanetId.VENUS to 5.5, PlanetId.SATURN to 5.0
        )
        val byId = result.planets.associateBy { it.planet }
        val isDayBirth = result.planets.first { it.planet == PlanetId.SUN }.houseFromLagna in 7..12
        val raw = result.shadbala.map { sb ->
            val p = sb.planet
            val pos = byId.getValue(p)
            val kaala = when {
                p == PlanetId.SUN || p == PlanetId.JUPITER || p == PlanetId.VENUS -> if (isDayBirth) 48.0 else 28.0
                p == PlanetId.MOON || p == PlanetId.MARS || p == PlanetId.SATURN -> if (!isDayBirth) 48.0 else 28.0
                else -> 42.0
            } + sb.pakshaBala * 0.45
            val cheshta = when {
                p == PlanetId.SUN || p == PlanetId.MOON -> (sb.sthanaBala * 0.4).coerceIn(15.0, 60.0)
                pos.isRetrograde -> 54.0
                else -> (abs(pos.speedDegPerDay) * 28.0).coerceIn(18.0, 52.0)
            }
            val naturalBenefics = setOf(PlanetId.JUPITER, PlanetId.VENUS, PlanetId.MOON)
            val drik = result.planets.filter { it.planet != p }.sumOf { other ->
                val diff = ((pos.rashi.ordinal - other.rashi.ordinal + 12) % 12) + 1
                if (diff == 7 || diff == 5 || diff == 9) {
                    if (other.planet in naturalBenefics) 10.0 else -5.0
                } else 0.0
            }.coerceIn(-20.0, 45.0) + 18.0

            val totalV = sb.sthanaBala + sb.digBala + kaala + cheshta + sb.naisargikaBala + drik
            val rupas = totalV / 60.0
            val req = reqRupas.getValue(p)
            ShadbalaExtendedRow(
                planet = p,
                sthana = sb.sthanaBala,
                dig = sb.digBala,
                kaala = kaala,
                cheshta = cheshta,
                naisargika = sb.naisargikaBala,
                drik = drik,
                totalVirupas = totalV,
                totalRupas = rupas,
                requiredRupas = req,
                ratio = rupas / req,
                rank = 1
            )
        }
        val sortedRank = raw.sortedByDescending { it.ratio }.mapIndexed { idx, row -> row.planet to (idx + 1) }.toMap()
        return raw.map { it.copy(rank = sortedRank.getValue(it.planet)) }
    }

    fun computeSarvatobhadraAndKota(result: KundaliResult): SarvatobhadraAndKotaReport {
        val moon = result.planets.first { it.planet == PlanetId.MOON }
        val moonNak = moon.nakshatraIndex // 0..26
        val swaras = listOf("अ (A)", "आ (Aa)", "इ (I)", "ई (Ee)", "उ (U)", "ऊ (Oo)", "ए (E)", "ऐ (Ai)", "ओ (O)", "औ (Au)", "अं (Am)", "अः (Aha)")
        val birthSwara = swaras[moonNak % swaras.size]

        // Vedha targets from transit Moon/Saturn/Mars on Natal Moon Nakshatra (Sammukha +14, Vama/Dakshina)
        val gocharMoon = result.gocharPlanets.first { it.planet == PlanetId.MOON }
        val gocharSat = result.gocharPlanets.first { it.planet == PlanetId.SATURN }
        val gocharJup = result.gocharPlanets.first { it.planet == PlanetId.JUPITER }

        fun nakVedhaRelation(transitNak: Int): String {
            val front = (transitNak + 14) % 27
            val left = (transitNak + 7) % 27
            val right = (transitNak + 20) % 27
            return when (moonNak) {
                transitNak -> "Janma Nakshatra Overlap"
                front -> "Sammukha Vedha (+15)"
                left -> "Vama Vedha (+8)"
                right -> "Dakshina Vedha (+21)"
                else -> "Clear (No direct Vedha)"
            }
        }

        val hits = listOf(
            "Transit Moon (${AstroStrings.nakshatraName(gocharMoon.nakshatraIndex, AppLanguage.EN)}): ${nakVedhaRelation(gocharMoon.nakshatraIndex)}",
            "Transit Jupiter (${AstroStrings.nakshatraName(gocharJup.nakshatraIndex, AppLanguage.EN)}): ${nakVedhaRelation(gocharJup.nakshatraIndex)}",
            "Transit Saturn (${AstroStrings.nakshatraName(gocharSat.nakshatraIndex, AppLanguage.EN)}): ${nakVedhaRelation(gocharSat.nakshatraIndex)}"
        )

        // Latta kick (Sun +12 forward, Mars +3 forward, Jupiter +6 forward, Saturn +8 backward)
        val sun = result.planets.first { it.planet == PlanetId.SUN }
        val sunLattaNak = (sun.nakshatraIndex + 12) % 27
        val lattaNote = "Sun natal Latta falls on ${AstroStrings.nakshatraName(sunLattaNak, AppLanguage.EN)}; Saturn transit Latta falls on ${AstroStrings.nakshatraName((gocharSat.nakshatraIndex - 8 + 27) % 27, AppLanguage.EN)}."

        val sbcElements = listOf(
            "Swara (Vowel)" to birthSwara,
            "Vaar (Weekday)" to AstroStrings.weekdayName(result.panchang.weekdayIndex, AppLanguage.EN),
            "Tithi Group" to AstroStrings.tithiName(result.panchang.tithiNumber, AppLanguage.EN),
            "Nakshatra (27)" to AstroStrings.nakshatraName(moonNak, AppLanguage.EN),
            "Chandra Rashi" to moon.rashi.name
        )

        // Kota Chakra (28-nakshatra cycle starting from Janma Nakshatra; Stambha 0..3, Madhya 4..10, Prakaara 11..19, Bahya 20..27)
        val kotaSwami = moon.rashi.lord
        val kotaPal = moon.nakshatraLord
        fun zoneOfNak(nakIdx: Int): String {
            val offset = (nakIdx - moonNak + 27) % 27
            return when {
                offset in 0..3 -> "Stambha (Inner Pillar)"
                offset in 4..10 -> "Madhya (Fort Courtyard)"
                offset in 11..19 -> "Prakaara (Fort Wall)"
                else -> "Bahya (Outer Perimeter)"
            }
        }
        val stambhaPlanets = result.gocharPlanets.filter { ((it.nakshatraIndex - moonNak + 27) % 27) in 0..3 }.map { it.planet.name }
        val madhyaPlanets = result.gocharPlanets.filter { ((it.nakshatraIndex - moonNak + 27) % 27) in 4..10 }.map { it.planet.name }
        val prakaaraPlanets = result.gocharPlanets.filter { ((it.nakshatraIndex - moonNak + 27) % 27) in 11..19 }.map { it.planet.name }
        val bahyaPlanets = result.gocharPlanets.filter { ((it.nakshatraIndex - moonNak + 27) % 27) >= 20 }.map { it.planet.name }

        val maleficsInInner = result.gocharPlanets.count {
            it.planet in listOf(PlanetId.SATURN, PlanetId.MARS, PlanetId.RAHU, PlanetId.KETU) &&
                ((it.nakshatraIndex - moonNak + 27) % 27) in 0..10
        }
        val kotaVerdict = if (maleficsInInner <= 1) {
            "Fort Protected: Strong inner defense around Stambha & Madhya zones (${zoneOfNak(gocharJup.nakshatraIndex)} blessed by Jupiter)."
        } else {
            "Fort Alert: $maleficsInInner malefic transits inside inner zones — practice caution and Kota Swami (${kotaSwami.name}) remedies."
        }

        return SarvatobhadraAndKotaReport(
            birthSwara = birthSwara,
            sbcElements = sbcElements,
            vedhaHits = hits,
            lattaNote = lattaNote,
            kotaSwami = kotaSwami,
            kotaPal = kotaPal,
            stambhaZone = stambhaPlanets.joinToString(", ").ifEmpty { "Clear" },
            madhyaZone = madhyaPlanets.joinToString(", ").ifEmpty { "Clear" },
            prakaaraZone = prakaaraPlanets.joinToString(", ").ifEmpty { "Clear" },
            bahyaZone = bahyaPlanets.joinToString(", ").ifEmpty { "Clear" },
            kotaVerdict = kotaVerdict
        )
    }
}
