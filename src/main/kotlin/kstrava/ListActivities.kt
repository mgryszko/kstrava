package com.grysz.kstrava

import arrow.Kind
import arrow.typeclasses.Monad
import java.time.LocalDateTime

data class AccessToken(val token: String) {
    init {
        require(token.isNotBlank()) { "token must not be blank" }
    }
}

data class Activity(
    val id: Long,
    val distance: Distance,
    val gear: Gear?,
    val name: String,
    val private: Boolean,
    val startDate: LocalDateTime,
    val type: String
)

data class Distance(val meters: Int) {
    init {
        require(meters > 0) { "distance in meters must be greater than 0" }
    }
}

data class Gear(val id: String, val name: String)

sealed class ListActivitiesError

object TokenAccessError : ListActivitiesError()

data class StravaApiError(val exception: Throwable) : ListActivitiesError()

fun <F> listActitivies(
    M: Monad<F>,
    readAccessToken: (String) -> Kind<F, AccessToken>,
    getActivities: (AccessToken) -> Kind<F, List<Activity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    M.fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        !getActivities(accessToken)
    }

