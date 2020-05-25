package com.erikwestervind.thefantasticrace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    val gameItems = mutableListOf<GameInfo>()

    //Navigation:
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var profileName: TextView
    lateinit var profileEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadGames()

        recyclerView = findViewById<RecyclerView>(R.id.gamesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GameListRecyclerAdapter(this, gameItems)

        //Navigation:
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        //profileName = findViewById(R.id.profileNameTextView)
        //profileEmail = findViewById(R.id.emailTextView)

        //profileName.text = ""
        //profileEmail.text = ""

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_friends -> {
                Toast.makeText(this, "Friends clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_update -> {
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                signOut()
                Toast.makeText(this, "You have signed out", Toast.LENGTH_LONG).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadGames() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("races_invited")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    gameItems.clear()
                    for(document in snapshot.documents) {
                        val game = document.toObject(GameInfo::class.java)
                        if(game != null) {
                            gameItems.add(game)
                        }
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                    println("!!! Recycler view updated")
                }
                if(e != null) {
                    println("!!! Listen failed ${e}")
                }
            }
    }

    private fun loadProfile() {

    }

    private fun signOut() {
        auth.signOut()
        val signOutIntent = Intent(this, LoginActivity::class.java)
        startActivity(signOutIntent)
    }
}
