package com.erikwestervind.thefantasticrace

import java.util.*

data class GameLocation(var name: String? = null,
                        var hint: String? = null,
                        var question: String? = null,
                        var answer: String? = null,
                        var id: String? = null,
                        var latitude: Double? = null,
                        var longitude: Double? = null,
                        var race: String? = null,
                        var order: Int? = null,
                        var timestamp: Date? = null,
                        var visited: Boolean = false,
                        var entered: Boolean = false)
