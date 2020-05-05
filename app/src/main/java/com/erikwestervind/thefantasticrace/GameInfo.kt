package com.erikwestervind.thefantasticrace

import java.util.*

data class GameInfo(var name: String? = null,
               var description: String? = null,
               var radius: Double? = null,
               var show_next_stop: Int? = null,
               var show_players_map: Boolean? = null,
               var unlock_with_question: Boolean? = null,
               var parent_race: String? = null,
               var start_time: Date? = null){
}