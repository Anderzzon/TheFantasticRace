package com.erikwestervind.thefantasticrace

import com.google.android.gms.maps.model.*

object DataManager {
    val locations = mutableListOf<GameLocation>()
    val markerOptions = mutableListOf<MarkerOptions>()
    val markers = mutableListOf<Marker>()
    val circlesOptions = mutableListOf<CircleOptions>()
    val circles = mutableListOf<Circle>()
    var gameInfo = GameInfo()
    var listOfGames = mutableMapOf<String, GameInfo>()

}