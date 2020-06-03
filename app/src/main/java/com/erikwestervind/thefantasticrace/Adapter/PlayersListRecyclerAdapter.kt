package com.erikwestervind.thefantasticrace.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erikwestervind.thefantasticrace.*

class PlayersListRecyclerAdapter(private val players: List<Player>)
    : RecyclerView.Adapter<PlayersListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {

        println("!!!! Number of players: ${players.count()}")
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun getItemCount() = players.count()


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]

            holder.playerNameTextView?.text = player.name!!.capitalize()
            holder.scoreTextView?.text = "${player.finishedStops.toString()}/${DataManager.locations.size} stops finished"
            holder.playerPosition = position


    }

    inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.players_list_view, parent, false)) {

        val playerNameTextView = itemView.findViewById<TextView>(R.id.playerNameTextView)
        val scoreTextView = itemView.findViewById<TextView>(R.id.scoreTextView)
        var playerPosition = 0

    }
}