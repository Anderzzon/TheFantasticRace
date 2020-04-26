package com.erikwestervind.thefantasticrace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            //val errorMessage = GeofenceErrorMessages.getErrorString(this,
                //geofencingEvent.errorCode)
            // display error
            println("!!! Error")
        } //else {
//            geofencingEvent.triggeringGeofences.forEach {
//                val geofence = it.requestId
//                println("!!!" + geofence)
//                // display notification
//            }
//        }
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            Log.i("!!!", "You have entered a geofence")

            println("!!! You have entered a geofence")

        } else {
            println("!!! Error, not in a geofence")
        }

    }

}