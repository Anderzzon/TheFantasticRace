package com.erikwestervind.thefantasticrace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeofenceReceiver: BroadcastReceiver() {
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        if (geofencingEvent.hasError()) {
            //val errorMessage = GeofenceErrorMessages.getErrorString(this,
                //geofencingEvent.errorCode)
            // display error
            println("!!! Error ${geofencingEvent.errorCode}")
        }
            geofencingEvent.triggeringGeofences.forEach {
               val geofence = it.requestId
                updateLocation(geofence)
                println("!!! Geofence entered: " + geofence)

                val geofenceTransition = geofencingEvent.geofenceTransition

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    println("!!! Geofence entered: " + geofence)
                    }

                // display notification
            }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            //val triggeringGeofences = geofencingEvent.triggeringGeofences
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

    private fun updateLocation(uid:String) {
        val user = auth.currentUser
        var index:Int
        val locationRef = db.collection("users").document(user!!.uid).collection("places").document(uid)
        locationRef
            .get()
            .addOnSuccessListener { document ->

                val newStop = document.toObject(GameLocation::class.java)
                if(newStop != null) {

                    index = newStop.order!!
                    if (DataManager.gameInfo.unlock_with_question == true) {
                        if (newStop.visited == false) {
                            DataManager.circles[index].isVisible = true
                        }
                        locationRef
                            .update("entered", true)
                        //DataManager.markers[index].isVisible = true
                    } else {
                        locationRef
                            .update("entered", true)
                        locationRef
                            .update("visited", true)
                            .addOnSuccessListener {
                                println("!!! DocumentSnapshot successfully updated!")}
                            .addOnFailureListener { e -> println("!!! Error updating document ${e}") }
                    }
                }
            }
    }

}