package com.erikwestervind.thefantasticrace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnswerQuestionActivity : AppCompatActivity() {

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var locationID: String
    val GAME_STRING = "GAMEID"
    val MARKER_STRING = "MARKER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_question)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val gameID = intent.getStringExtra(GAME_STRING)
        locationID = intent.getStringExtra(MARKER_STRING)
        getLocationInfo(locationID)

    }

    private fun getLocationInfo(uid: String) {
        val user = auth.currentUser
        var index: Int
        val locationRef =
            db.collection("users").document(user!!.uid).collection("places").document(uid)
        locationRef
            .get()
            .addOnSuccessListener { document ->

                val newStop = document.toObject(GameLocation::class.java)
                if (newStop != null) {
                    setTitle(newStop.name!!.capitalize())

                    index = newStop.order!!

                    locationRef
                        .update("visited", true)
                        .addOnSuccessListener {
                            println("!!! DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e -> println("!!! Error updating document ${e}") }

                }
            }
    }
}
