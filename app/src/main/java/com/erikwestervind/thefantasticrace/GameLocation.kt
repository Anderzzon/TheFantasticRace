package com.erikwestervind.thefantasticrace

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import java.util.*

data class GameLocation(var name: String? = null,
                        var id: String? = null,
                        var latitude: Double? = null,
                        var longitude: Double? = null,
                        var race: String? = null,
                        var order: Int? = null,
                        var timestamp: Date? = null)
{

}