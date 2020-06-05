package com.erikwestervind.thefantasticrace.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erikwestervind.thefantasticrace.*
import com.erikwestervind.thefantasticrace.Adapter.StopsListRecyclerAdapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_stops.*

/**
 * A simple [Fragment] subclass.
 */
class StopsFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    private var gameId: String? = null
    val stopItems = mutableListOf<GameLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_stops, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        gameId = (activity as ActiveGameActivity).gameId
        loadLocations()

        recyclerView = view.findViewById<RecyclerView>(R.id.stopsList)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = StopsListRecyclerAdapter(stopItems)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadLocations()
    }

    override fun onResume() {
        super.onResume()
        loadLocations()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun loadLocations() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("places")
            .whereEqualTo("race", gameId)
            .orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                }
                if (snapshot != null) {
                    stopItems.clear()

                    for (document in snapshot.documents) {
                        val newStop = document.toObject(GameLocation::class.java)

                        if (newStop != null) {
                            newStop.id = document.id

                            stopItems.add(newStop)
                        }
                    }
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
    }
}
