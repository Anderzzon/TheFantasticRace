package com.erikwestervind.thefantasticrace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GameListActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    val gameItems = mutableListOf<GameInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_list)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadGames()

        recyclerView = findViewById<RecyclerView>(R.id.gamesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = GameListRecyclerAdapter(this, gameItems)

    }

    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
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
}
