package com.erikwestervind.thefantasticrace

import java.util.*

class Player(var name: String? = null,
             var email: String? = null,
             var uid: String? = null,
             var finishedStops:Int? = null,
             var gameFinished:Boolean? = null,
             var finished_time: Date? = null,
             var updated: Date? = null,
             var photoUrl: String? = null,
             var latitude: Double? = null,
             var longitude: Double? = null) {
}