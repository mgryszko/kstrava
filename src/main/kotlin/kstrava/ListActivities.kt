package com.grysz.kstrava

import arrow.Kind
import arrow.typeclasses.Monad
import com.grysz.kstrava.token.AccessToken
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

sealed class ListActivitiesError

object TokenAccessError : ListActivitiesError()

data class StravaApiError(val exception: Throwable) : ListActivitiesError()

fun <F> Monad<F>.listActitivies(
    readAccessToken: (String) -> Kind<F, AccessToken>,
    getActivities: (AccessToken) -> Kind<F, List<Activity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        !getActivities(accessToken)
    }

