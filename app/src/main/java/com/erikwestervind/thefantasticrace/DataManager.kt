package com.erikwestervind.thefantasticrace

import com.google.android.gms.maps.model.LatLng

object DataManager {
    val locations = mutableListOf<GameLocation>()

    init {
        createMockData()
    }

    private fun createMockData() {
        var location = GameLocation("Sjöstan",59.304596, 18.094637)
        locations.add(location)
        location = GameLocation("Dagis",59.308147, 18.095720)
        locations.add(location)
        location = GameLocation("Lekpark",59.311010, 18.106046)
        locations.add(location)
    }
}