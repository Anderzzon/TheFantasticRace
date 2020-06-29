package com.erikwestervind.thefantasticrace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AnswerQuestionActivity : AppCompatActivity() {

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var locationID: String
    lateinit var hintTextView: TextView
    lateinit var questionTextView: TextView
    lateinit var answerView: EditText
    lateinit var answerButton: Button
    lateinit var answerTextView: TextView
    lateinit var question: GameLocation
    lateinit var gameID: String
    lateinit var parentID: String
    var answerInput = ""
    val GAME_STRING = "GAMEID"
    val PARENT_STRING = "PARENTID"
    val MARKER_STRING = "MARKER"
    var markerIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_question)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        hintTextView = findViewById(R.id.textViewHint)
        questionTextView = findViewById(R.id.textViewQuestion)
        answerView = findViewById(R.id.answerText)
        answerButton = findViewById(R.id.answerButton)
        answerTextView = findViewById(R.id.answerTextView)

        questionTextView.visibility = View.GONE
        answerView.visibility = View.GONE
        answerButton.visibility = View.GONE
        answerTextView.visibility = View.GONE

        gameID = intent.getStringExtra(GAME_STRING)
        parentID = intent.getStringExtra(PARENT_STRING)
        locationID = intent.getStringExtra(MARKER_STRING)

        DataManager.locations

        showQuestion(locationID)
        //getLocationInfo(locationID)

            answerButton.setOnClickListener {
                if(question.answer != null) {
                    if (question.entered == true) {
                    val answer = answerQuestion(question.answer!!)
                    if (answer == true) {
                        updateVisit()
                        showHideViews()
                        finish()
                    }
                }
                println("!!! Answer is ${question.answer}")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showQuestion(uid: String) {
        for (stop in DataManager.locations) {
            if (stop.id == uid) {
                question = DataManager.locations[stop.order!!]
                title = question.name!!.capitalize()
                if (question.hint != null) {
                    hintTextView.text = question.hint
                }
                if (question.question != null) {
                    if (question.entered == true) {
                        questionTextView.text = question.question
                    } else {
                        val notEnteredMessage = "You need to get closer"
                        Snackbar.make(findViewById(R.id.answerButton), notEnteredMessage, Snackbar.LENGTH_INDEFINITE).show()
                    }
                }
                showHideViews()

            }
        }
    }

    private fun getLocationInfo(uid: String) {
        val user = auth.currentUser
        var index: Int
        val locationRef =
            db.collection("users").document(user!!.uid).collection("places").document(uid)
        locationRef
            .get()
            .addOnSuccessListener { document ->

                val questionFB = document.toObject(GameLocation::class.java)
                if (questionFB != null) {
                    question = questionFB
                    showHideViews()
                    title = questionFB.name!!.capitalize()
                    markerIndex = questionFB.order!!

                    if (question.hint != null) {
                        hintTextView.text = questionFB.hint
                    }
                    if (questionFB.question != null) {
                        if (question.entered == true) {
                            questionTextView.text = questionFB.question
                        } else {
                            val notEnteredMessage = "You need to get closer"
                            Snackbar.make(findViewById(R.id.answerButton), notEnteredMessage, Snackbar.LENGTH_INDEFINITE).show()
                        }
                    }
                    index = question.order!!
                }
            }
    }

    private fun answerQuestion(question: String):Boolean {

        answerInput = answerView.text.toString()
        println("!!! answer input: ${answerInput}")
            if (question!! == answerInput) {
                println("!!! Correct answeer")
                val correct = "Correct answer"
                Toast.makeText(this, correct, Toast.LENGTH_LONG).show()
                return true
            }
        println("!!! Wrong answer")
        val wrongAnswer = "Wrong answer"
        Snackbar.make(findViewById(R.id.answerButton), wrongAnswer, Snackbar.LENGTH_INDEFINITE).show()
        return false
    }

    private fun updateVisit() {
        val user = auth.currentUser
        val locationRef =
            db.collection("users").document(user!!.uid).collection("places").document(locationID)
        locationRef
            .update("visited", true)
            .addOnSuccessListener {
                println("!!! DocumentSnapshot successfully updated!")
                //updateScore(game)
                updatePlayerScore()
                //updateFinish()

            }
            .addOnFailureListener { e -> println("!!! Error updating document ${e}") }
    }

    private fun updatePlayerScore() {
        val user = auth.currentUser
        val playerRef =
            db.collection("races").document(parentID).collection("users").document(user!!.uid)
        playerRef
            .update("finishedStops", markerIndex+1)
            .addOnSuccessListener {
                println("!!!! Player score successfully updated!")
            }
            .addOnFailureListener { e -> println("!!!! Error updating players finished stops ${e}") }
        if (markerIndex == DataManager.locations.size-1) {
            playerRef
                .update("finished_time", Timestamp.now())
                .addOnSuccessListener {
                    println("!!!! Player finished updated!")
                }
                .addOnFailureListener { e -> println("!!!! Error updating player finish time ${e}") }
        }
    }

    private fun showHideViews() {

        if (question != null) {
            if (question.entered == false) {
                questionTextView.visibility = View.GONE
                answerView.visibility = View.GONE
                answerButton.visibility = View.GONE
            } else {
                questionTextView.visibility = View.VISIBLE
                answerView.visibility = View.VISIBLE
                answerButton.visibility = View.VISIBLE
            }
            if (question.visited) {
                answerView.visibility = View.GONE
                answerButton.visibility = View.GONE
                if (question.answer != null) {
                    answerTextView.visibility = View.VISIBLE
                    answerTextView.text = "You answered correct: ${question.answer!!.capitalize()}"
                }

            }
        }
    }
}