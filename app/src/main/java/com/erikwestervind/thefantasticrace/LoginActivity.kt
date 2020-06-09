package com.erikwestervind.thefantasticrace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var textEmail: EditText
    lateinit var textPassword: EditText
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        textEmail = findViewById(R.id.editTextEmail)
        textPassword = findViewById(R.id.editTextPassword)
        auth = FirebaseAuth.getInstance()

        val createButton = findViewById<Button>(R.id.buttonCreate)
        createButton.setOnClickListener {
            createUser()
        }

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        loginButton.setOnClickListener {
            logInUser()
        }
    }

    fun goToAddActivity() {
        val intent = Intent(this, GameListActivity::class.java)
        startActivity(intent)
    }

    fun logInUser() {
        if (textEmail.text.toString().isEmpty() || textPassword.text.toString().isEmpty())
            return

        auth.signInWithEmailAndPassword(textEmail.text.toString(), textPassword.text.toString())
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    goToAddActivity()
                    println("!!! user Loged in")
                } else {
                    Snackbar.make(textEmail, "User not loged in",Snackbar.LENGTH_LONG)
                    println("!!! Fail to Login")
                }
            }
    }

    fun createUser() {
        if (textEmail.text.toString().isEmpty() || textPassword.text.toString().isEmpty())
            return

        auth.createUserWithEmailAndPassword(textEmail.text.toString(), textPassword.text.toString())
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    goToAddActivity()
                    println("!!! user created")
                } else {
                    Snackbar.make(textEmail, "User not created",Snackbar.LENGTH_LONG)
                    println("!!! Fail")
                }
            }


    }
}