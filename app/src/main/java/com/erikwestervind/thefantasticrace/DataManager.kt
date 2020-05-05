package com.erikwestervind.thefantasticrace

import com.google.android.gms.maps.model.*

object DataManager {
    val locations = mutableListOf<GameLocation>()
    val markerOptions = mutableListOf<MarkerOptions>()
    val markers = mutableListOf<Marker>()
    val circlesOptions = mutableListOf<CircleOptions>()
    val circles = mutableListOf<Circle>()

    init {
//        createMockData()
    }

//    private fun createMockData() {
//        var location = GameLocation("Sjöstan",59.304596, 18.094637)
//        locations.add(location)
//        location = GameLocation("Dagis",59.308147, 18.095720)
//        locations.add(location)
//        location = GameLocation("Lekpark",59.311010, 18.106046)
//        locations.add(location)
//    }
}