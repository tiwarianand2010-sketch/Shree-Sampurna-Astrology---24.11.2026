package com.example.data

import com.example.model.DstRule
import com.example.model.Place

object PlacesRepository {

    private val internationalPlaces: List<Place> = listOf(
        Place("nyc", "न्यूयॉर्क, अमेरिका", "New York, USA", "纽约，美国", "US", 40.7128, -74.0060, -5.0, DstRule.US_MODERN),
        Place("la", "लॉस एंजेलिस, अमेरिका", "Los Angeles, USA", "洛杉矶，美国", "US", 34.0522, -118.2437, -8.0, DstRule.US_MODERN),
        Place("chicago", "शिकागो, अमेरिका", "Chicago, USA", "芝加哥，美国", "US", 41.8781, -87.6298, -6.0, DstRule.US_MODERN),
        Place("london", "लंदन, इंग्लैंड", "London, UK", "伦敦，英国", "GB", 51.5072, -0.1276, 0.0, DstRule.UK_MODERN),
        Place("beijing", "बीजिंग, चीन", "Beijing, China", "北京，中国", "CN", 39.9042, 116.4074, 8.0, DstRule.NONE),
        Place("shanghai", "शंघाई, चीन", "Shanghai, China", "上海，中国", "CN", 31.2304, 121.4737, 8.0, DstRule.NONE)
    )

    private val basePlaces: List<Place> by lazy {
        IndianDistrictsData.loadIndianDistricts() + internationalPlaces
    }

    private val customPlaces = mutableListOf<Place>()

    val allPlaces: List<Place>
        get() = customPlaces + basePlaces

    val defaultPlace: Place by lazy {
        basePlaces.firstOrNull { it.nameEn == "New Delhi" } ?: basePlaces.first()
    }

    fun addCustomPlace(name: String, latitude: Double, longitude: Double, utcOffsetHours: Double): Place {
        val cleanName = name.trim()
        val slug = cleanName.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifEmpty { "custom" }
        val place = Place(
            id = "custom_${slug}_${System.currentTimeMillis()}",
            nameHi = cleanName,
            nameEn = cleanName,
            nameZh = cleanName,
            countryCode = "CUSTOM",
            latitude = latitude,
            longitude = longitude,
            baseUtcOffsetHours = utcOffsetHours,
            dstRule = DstRule.NONE
        )
        customPlaces.add(0, place)
        return place
    }

    fun search(query: String): List<Place> {
        val q = query.trim().lowercase()
        val list = allPlaces
        if (q.isEmpty()) return list
        return list.filter {
            it.nameEn.lowercase().contains(q) ||
                it.nameHi.lowercase().contains(q) ||
                it.nameZh.lowercase().contains(q) ||
                it.countryCode.lowercase().contains(q)
        }
    }
}
