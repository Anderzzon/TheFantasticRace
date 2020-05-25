package com.erikwestervind.thefantasticrace.Adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.shapes.Shape
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.erikwestervind.thefantasticrace.*
import com.google.firebase.Timestamp

class StopsListRecyclerAdapter(private val stops: List<GameLocation>)
    : RecyclerView.Adapter<StopsListRecyclerAdapter.ViewHolder>() {
    //private lateinit var pagerAdapter: StopsListRecyclerAdapter

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater, parent)
    }

    override fun getItemCount() = stops.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stop = stops[position]

            holder.stopNumerTextView?.text = (stop.order!! + 1).toString()
            holder.stopNameTextView?.text = stop.name!!.capitalize()
            holder.stopID = stop.id!!
            holder.gamePosition = position
            holder.padlock.visibility = View.GONE

        if (!stops[position].visited) {
            holder.checkMark.visibility = View.GONE
        }
        if (stops[position].timestamp == null) {
            holder.stopNumerTextView.setBackgroundResource(R.drawable.rounded_list_grey) //Sets circle to grey
            holder.stopNameTextView.visibility = View.GONE
            holder.padlock.visibility = View.VISIBLE

        }

    }

    inner class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.stops_list_view, parent, false)) {

        val stopNumerTextView = itemView.findViewById<TextView>(R.id.listItemNumberView)
        val stopNameTextView = itemView.findViewById<TextView>(R.id.stopNameTextView)
        val checkMark = itemView.findViewById<ImageView>(R.id.checkMarkImageView)
        val padlock = itemView.findViewById<ImageView>(R.id.padlockImageView)
        var gamePosition = 0
        var stopID = ""


        init {

            itemView.setOnClickListener {
                if (stops[gamePosition].timestamp != null) {
                    //val intent = Intent(context, MapsActivity::class.java)
                    val intent = Intent(parent.context, AnswerQuestionActivity::class.java)
                    intent.putExtra("MARKER", stops[gamePosition].id)
                    parent.context.startActivity(intent)
                } else {
                    val message = Toast.makeText(parent.context, "You need to visit stop ${(gamePosition)}: ${stops[gamePosition-1].name!!.capitalize()} first", Toast.LENGTH_LONG)
                    message.setGravity(Gravity.BOTTOM, 0, 200)
                    message.show()
                }
            }
        }
    }
}