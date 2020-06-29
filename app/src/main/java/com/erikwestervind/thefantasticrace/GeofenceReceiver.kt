package com.erikwestervind.thefantasticrace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.Timestamp
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
            println("!!!! Error ${geofencingEvent.errorCode}")
        }
            geofencingEvent.triggeringGeofences.forEach {
               val geofence = it.requestId
                vibrate(context)
                updateLocation(geofence)


                println("!!!! Geofence entered: " + geofence)

                val geofenceTransition = geofencingEvent.geofenceTransition

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    println("!!!! Geofence entered: " + geofence)
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

            println("!!!! You have entered a geofence")

        } else {
            println("!!!! Error, not in a geofence")
        }
    }

    private fun updateLocation(id: String) {
        val user = auth.currentUser
        var lastStop = false
        loop@ for (stop in DataManager.locations) {
            if (stop.id == id && stop.order == DataManager.locations.size - 1) {
                lastStop = true
                break@loop
            }
            println("!!!! Not last stop")
        }
        val locationRef =
            db.collection("users").document(user!!.uid).collection("places").document(id)

        if (DataManager.gameInfo.unlock_with_question == true) {
//                        if (newStop.visited == false) {
//                            DataManager.circles[index].isVisible = true
//                        }
            locationRef
                .update("entered", true)

            if (lastStop == true) {
                locationRef
                    .update("visited", true)
                finishGame()
            }
        } else {
            locationRef
                .update("entered", true)
            locationRef
                .update("visited", true)
                .addOnSuccessListener {
                    println("!!!! DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e -> println("!!!! Error updating document ${e}") }
        }
    }

    private fun vibrate(context: Context?) {

        val vibrator: Vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(3000)
        }
    }

    private fun finishGame() {
        val user = auth.currentUser
        val gameRef =
            db.collection("races").document(DataManager.gameInfo.parent_race!!).collection("users").document(user!!.uid)
        gameRef
            .update("finished_time", Timestamp.now())
        gameRef
            .update("gameFinished", true)
    }
}