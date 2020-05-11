package com.erikwestervind.thefantasticrace

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameListRecyclerAdapter (private val context: Context, private val games: List<GameInfo>)
    : RecyclerView.Adapter<GameListRecyclerAdapter.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.game_list_view, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount() = games.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game = games[position]
        holder.textName.text = game.name
        holder.textDescription.text = game.description
        holder.itemID = game.parent_race!!
        holder.gamePosition = position

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.nameTextView)
        val textDescription = itemView.findViewById<TextView>(R.id.descriptionTextView)
        var gamePosition = 0
        var itemID = ""


        init {
            itemView.setOnClickListener {
                val intent = Intent(context, MapsActivity::class.java)
                intent.putExtra(GAME_ID_KEY, games[gamePosition].parent_race)
                context.startActivity(intent)
            }

        }
    }

}