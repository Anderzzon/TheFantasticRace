package com.erikwestervind.thefantasticrace

import android.content.IntentSender
import java.util.*
import kotlin.collections.ArrayList

data class GameInfo(var name: String? = null,
               var description: String? = null,
               var radius: Double? = null,
               var id: String? = null,
               var owner: String? = null,
               var show_next_stop: Int? = null,
               var show_players_map: Boolean? = null,
               var unlock_with_question: Boolean? = null,
               var parent_race: String? = null,
               var start_time: Date? = null,
               var finished_time: Date? = null,
               var finishedStops: Int? = null,
               var gameFinished: Boolean? = null,
               var latestUpdated: Date? = null,
               var listOfPlayers: ArrayList<String>? = null,
               var invites: ArrayList<String>? = null,
               var accepted: ArrayList<String>? = null){
}