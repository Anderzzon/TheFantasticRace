package com.erikwestervind.thefantasticrace.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erikwestervind.thefantasticrace.*
import com.erikwestervind.thefantasticrace.Adapter.PlayersListRecyclerAdapter
import com.erikwestervind.thefantasticrace.Adapter.StopsListRecyclerAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * A simple [Fragment] subclass.
 */
class PlayersFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    private var gameId: String? = null
    val players = mutableListOf<Player>()
    var currentGame: GameInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_players, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        gameId = (activity as ActiveGameActivity).gameId

        loadPlayersInGame()

        recyclerView = view.findViewById<RecyclerView>(R.id.playersList)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PlayersListRecyclerAdapter(players)
    }

    override fun onResume() {
        super.onResume()
        loadPlayersInGame()
    }

    private fun loadPlayersInGame() {
        val user = auth.currentUser
        db.collection("races").document(gameId!!).collection("users")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    players.clear()
                    for (document in snapshot.documents) {

                        val newPlayer = document.toObject(Player::class.java)
                        if (newPlayer != null) {
                            if (newPlayer.uid == user!!.uid) {
                                newPlayer.name = "You"
                            }
                            players.add(newPlayer)
                        }
                    }
                    recyclerView.adapter?.notifyDataSetChanged()

                }
                if (e != null) {
                }
            }
    }
}
