package com.erikwestervind.thefantasticrace

import com.google.android.gms.maps.model.LatLng

object DataManager {
    val locations = mutableListOf<Location>()

    init {
        createMockData()
    }

    private fun createMockData() {
        var location = Location("Sjöstan", LatLng(59.304596, 18.094637))
        locations.add(location)
        //location = Location("Dagis", LatLng(59.308147, 18.095720))
        //locations.add(location)
        //location = Location("Lekpark", LatLng(59.311010, 18.106046))
        //locations.add(location)
    }
}