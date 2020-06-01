package com.erikwestervind.thefantasticrace.Fragments

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
        loadPlayers()

        println("!!!! GameID from ActivityGame ${gameId}")

        //val view = inflater.inflate(R.layout.fragment_stops, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.playersList)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PlayersListRecyclerAdapter(players)

    }

    private fun loadPlayers() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("races_invited")
            .whereEqualTo("parent_race", gameId)
            .addSnapshotListener { snapshot, e ->
                if(e != null) {
                    println("!!!! Listen failed ${e}")
                }
                if (snapshot != null) {
                    players.clear()

                    for(document in snapshot.documents) {
                        //val game = document.toObject(GameInfo::class.java)
                        currentGame = document.toObject(GameInfo::class.java)
                        println("!!!! Game info collected")
                        if(currentGame != null) {

                            if(currentGame!!.listOfPlayers != null) {
                                //getPlayerInfo2()
                                for (i in 0.. currentGame!!.listOfPlayers!!.size-1) {
                                    getPlayerInfo(currentGame!!.listOfPlayers!![i], i)
                                    println("!!!! UID of player is ${currentGame!!.listOfPlayers!![i]}")

                                }
                            }
                            recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }

                }
            }
    }

    private fun getPlayerInfo2() {
        for (i in 0 until currentGame!!.listOfPlayers!!.size) {
            val uid = currentGame!!.listOfPlayers!![i]
            val gameRef = db.collection("users").document(uid)
            gameRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("!!!! Listen failed ${e}")
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(Player::class.java)
                    if (user != null) {
                        getGameInfo(uid, i)
                        players.add(user)
                        println("!!!! ${user.name}")
                        println("!!!! Player added")
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getPlayerInfo(uid: String, index: Int) {
        val you = auth.currentUser
        val doc = db.collection("users").document(uid)
        doc.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(Player::class.java)
                    if (user != null) {
                        if (you != null) {
                            if (you.uid == user.uid) {
                                user.name = "You"
                            }
                        }
                        players.add(user)
                        //getGameInfo(uid, index)
                        println("!!!! Player added: ${user.name}")

                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
    }

    private fun getGameInfo(uid: String, index: Int) {
        for (i in 0 until players.size)
        db.collection("users").document(uid).collection("races_invited")
            .whereEqualTo("parent_race", gameId)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    val playersGame = document.toObject(GameInfo::class.java)
                    println("!!!! Document id: ${document.id}")
                    if (playersGame != null) {
                        players[index].finishedStops = playersGame.finishedStops
                        println("!!!! UID is: ${uid}")
                        println("!!!! Player name: ${players[index].name}")
                        println("!!!! Finished stops ${players[index].name}: ${players[index].finishedStops}")
                    }
                    recyclerView.adapter?.notifyDataSetChanged()

                }
            }.addOnFailureListener { exception ->
                println("!!! get failed with  ${exception}")
            }
    }


}
