package com.erikwestervind.thefantasticrace.Adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erikwestervind.thefantasticrace.*

class PlayersListRecyclerAdapter(private val players: List<Player>) :
    RecyclerView.Adapter<PlayersListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun getItemCount() = players.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]

        holder.playerNameTextView?.text = player.name!!.capitalize()
        holder.scoreTextView?.text =
            "${player.finishedStops.toString()}/${DataManager.locations.size} stops finished"
        holder.playerPosition = position
        if (player.finished_time != null) {
            if (DataManager.gameInfo.start_time != null) {
                val startTime = DataManager.gameInfo.start_time!!.time
                val endTime = player.finished_time!!.time
                val totalTime = (endTime - startTime)
                val totalTimeSec = totalTime / 1000
                val hours = totalTimeSec / 3600
                val totalMin = totalTimeSec % 3600
                val min = totalMin / 60
                val sec = totalMin % 60
                holder.scoreTextView?.text = "Finished in ${hours} hours, ${min} min, ${sec} sec"
            }
        }
    }

    inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.players_list_view, parent, false)) {

        val playerNameTextView = itemView.findViewById<TextView>(R.id.playerNameTextView)
        val scoreTextView = itemView.findViewById<TextView>(R.id.scoreTextView)
        var playerPosition = 0

    }
}