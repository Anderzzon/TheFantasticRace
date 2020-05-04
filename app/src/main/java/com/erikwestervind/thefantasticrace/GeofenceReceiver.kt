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
        }
            geofencingEvent.triggeringGeofences.forEach {
               val geofence = it.requestId
                println("!!! Geofence entered: " + geofence)
                // display notification
            }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences
//            val geofenceTransitionDetails = getGeofenceTransitionDetails(
//                this,
//                geofenceTransition,
//                triggeringGeofences
//            )

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.

            println("!!! You have entered a geofence")

        } else {
            println("!!! Error, not in a geofence")
        }

    }


}