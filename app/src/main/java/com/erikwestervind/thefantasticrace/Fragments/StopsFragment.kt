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
    //private var gameId: String? = id

    //private var gameId = "q6ou5AIikGUM5tSOY1Bw"
    private var gameId: String? = null
    private var parentId: String? = null
    val stopItems = mutableListOf<GameLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Bundle doesn't work
//        if(arguments != null) {
//            gameId = arguments!!.getString("GAME_ID")
//            println("!!! GameId in onCreate: ${gameId}")
//        }

        return inflater.inflate(R.layout.fragment_stops, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        gameId = (activity as ActiveGameActivity).gameId
        parentId = (activity as ActiveGameActivity).parentId

        DataManager.locations

        loadLocations()


        println("!!! GameID from ActivityGame ${gameId}")

        //val view = inflater.inflate(R.layout.fragment_stops, container, false)

            recyclerView = view.findViewById<RecyclerView>(R.id.stopsList)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = StopsListRecyclerAdapter(DataManager.locations)
}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadLocations()
    }

    override fun onResume() {
        super.onResume()
        println("!!!! In StopsFragment resume")
        loadLocations()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun loadLocations() {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("places")
            .whereEqualTo("race", parentId)
            .orderBy("order")
            .addSnapshotListener { snapshot, e ->
                if(e != null) {
                    println("!!!! Listen failed ${e}")
                }
                if (snapshot != null) {
                    //stopItems.clear()

/*                    for(document in snapshot.documents) {
                        val stop = document.toObject(GameLocation::class.java)
                        if(stop != null) {
                            stop.id = document.id
                            val index = stop.order!!
                            DataManager.locations[index].id = stop.id
                            DataManager.locations[index].visited = stop.visited
                            DataManager.locations[index].timestamp = stop.timestamp
                            //Set start time of first stop:
                            if (stop.order == 0 && stop.timestamp == null) {
                                stop.timestamp = DataManager.gameInfo.start_time
                            }
                            //showElapsedTime(DataManager.gameInfo.start_time!!.time)
                            println("!!! Updated: ${stop}")
                            println("!!! ID Updated: ${stop.id}")
                        }
                    }*/
                    recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

}
