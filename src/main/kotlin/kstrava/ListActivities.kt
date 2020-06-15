package com.grysz.kstrava

import arrow.Kind
import arrow.core.Validated
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.MonadError
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName
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

object AccessTokenFileNameBlankError : ListActivitiesError()

object TokenAccessError : ListActivitiesError()

data class StravaApiError(val exception: Throwable) : ListActivitiesError()

fun <F> MonadError<F, ListActivitiesError>.listActitivies(
    readAccessToken: (AccessTokenFileName) -> Kind<F, AccessToken>,
    getActivities: (AccessToken) -> Kind<F, List<Activity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    fx.monad {
        val validated = !AccessTokenFileName.create(accessTokenFileName).mapError(this@listActitivies) { AccessTokenFileNameBlankError }
        val accessToken = !readAccessToken(validated)
        !getActivities(accessToken)
    }

fun <F, A, E, EE> MonadError<F, EE>.mapError(value: Validated<E, A>, fe: (E) -> (EE)): Kind<F, A> =
    value.fold({ e -> raiseError(fe(e))}, { it.just() })

fun <F, A, E, EE> Validated<E, A>.mapError(AE: ApplicativeError<F, EE>, fe: (E) -> (EE)): Kind<F, A> =
    fold({ e -> AE.raiseError(fe(e))}, { AE.just(it) })


