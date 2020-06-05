package com.erikwestervind.thefantasticrace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signedOutIntent = Intent(this, LoginActivity::class.java)
        val signedInIntent = Intent(this, GameListActivity::class.java)

        if (FirebaseAuth.getInstance().currentUser == null)
            startActivity(signedOutIntent)
        else
            startActivity(signedInIntent)
        finish()
    }
}
