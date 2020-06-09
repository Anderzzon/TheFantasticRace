package com.erikwestervind.thefantasticrace

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GameListRecyclerAdapter (private val context: Context, private val games: List<GameInfo>)
    : RecyclerView.Adapter<GameListRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private val intent = Intent(context, ActiveGameActivity::class.java)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.game_list_view, parent, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        return ViewHolder(itemView)
    }

    override fun getItemCount() = games.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = games[position]
        holder.textName.text = game.name
        holder.textDescription?.text = game.description
        holder.itemID = game.parent_race
        holder.gamePosition = position

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.stopNameTextView)
        val textDescription = itemView.findViewById<TextView>(R.id.descriptionTextView)
        var gamePosition = 0
        var itemID: String? = null

        init {
            itemView.setOnClickListener {
                val timestamp = Timestamp.now()
                val date = timestamp.toDate()
                if (date > games[gamePosition].start_time) {
                    getLocations(games[gamePosition].id!!, games[gamePosition].parent_race!!)


                    intent.putExtra(GAME_ID_KEY, games[gamePosition].id)
                    intent.putExtra(PARENT_ID_KEY, games[gamePosition].parent_race)

                } else {
                    Toast.makeText(context, "Game hasn't started yet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getLocations(gameId: String, parentId: String) {
        val user = auth.currentUser
        db.collection("users").document(user!!.uid).collection("places")
            .whereEqualTo("race", parentId)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                DataManager.locations.clear()
                DataManager.markerOptions.clear()
                DataManager.markers.clear()
                DataManager.circlesOptions.clear()
                DataManager.circles.clear()
                for(document in documents) {
                    val newStop = document.toObject(GameLocation::class.java)

                    if(newStop != null) {
                        newStop.id = document.id

                        //gameLocations.add(newStop)
                        DataManager.locations.add(newStop)

                        val location = LatLng(newStop.latitude!!, newStop.longitude!!)
                        val markerOption = MarkerOptions()
                            .position(location)
                            .visible(false)
                            .snippet(newStop.id)

                        DataManager.markerOptions.add(markerOption)

                        val radius = DataManager.listOfGames[gameId]!!.radius
                        val circleOption = CircleOptions()
                            .center(location)
                            .radius(radius!!)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(0.0f)
                            .fillColor(0x220000FF)
                            .visible(false)

                        DataManager.circlesOptions.add(circleOption)
                        println("!!! ${newStop}")
                    }
                }
                context.startActivity(intent)

            }
            .addOnFailureListener { exception ->
                println("!!! Error ${exception}")
            }
    }

}