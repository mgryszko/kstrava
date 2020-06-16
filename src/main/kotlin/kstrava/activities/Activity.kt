package com.grysz.kstrava.activities

import java.time.LocalDateTime

data class Activity(
    val id: Long,
    val distance: Distance,
    val gear: Gear?,
    val name: String,
    val private: Boolean,
    val startDate: LocalDateTime,
    val type: String
)

data class ActivityId(val id: Long)

data class ActivityName(val name: String) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }
}

data class Distance(val meters: Int) {
    init {
        require(meters > 0) { "distance in meters must be greater than 0" }
    }
}

data class Gear(val id: String, val name: String)