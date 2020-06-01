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
    var answerInput = ""
    val GAME_STRING = "GAMEID"
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

        val gameID = intent.getStringExtra(GAME_STRING)
        locationID = intent.getStringExtra(MARKER_STRING)

        DataManager.locations

        getLocationInfo(locationID)

            answerButton.setOnClickListener {
                if(question.answer != null) {
                    if (question.entered == true) {
                    val answer = answerQuestion(question.answer!!)
                    if (answer == true) {
                        updateVisit(gameID)
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
                            val notEnteredMessage = "You need to get get closer"
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

    private fun updateVisit(game: String) {
        val user = auth.currentUser
        val locationRef =
            db.collection("users").document(user!!.uid).collection("places").document(locationID)
        locationRef
            .update("visited", true)
            .addOnSuccessListener {
                println("!!! DocumentSnapshot successfully updated!")
                updateScore(game)

            }
            .addOnFailureListener { e -> println("!!! Error updating document ${e}") }

    }

    private fun updateScore(game: String) {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("races_invited")
            .whereEqualTo("parent_race", game)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    val game = document.toObject(GameInfo::class.java)
                    if (game != null) {
                        game.id = document.id
                        val gameRef =
                            db.collection("users").document(user!!.uid).collection("races_invited").document(game.id!!)
                        gameRef
                            .update("finishedStops", markerIndex + 1)
                            .addOnSuccessListener {
                                println("!!!! Marker Index DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e -> println("!!! Error updating document ${e}") }
                        if (markerIndex == DataManager.locations.size-1) {
                            gameRef
                                .update("finished_time", Timestamp.now())
                        }

                    }

                }
            }.addOnFailureListener { exception ->
                println("!!! get failed with  ${exception}")
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