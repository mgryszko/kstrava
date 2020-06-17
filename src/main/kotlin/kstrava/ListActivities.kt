package com.grysz.kstrava

import arrow.Kind
import arrow.core.Validated
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName

sealed class ListActivitiesError

object AccessTokenFileNameBlankError : ListActivitiesError()

data class TokenAccessError(val exception: Throwable) : ListActivitiesError()

data class StravaError(val exception: Throwable) : ListActivitiesError()

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

private fun <F, A, E, EE> MonadError<F, EE>.mapError(value: Validated<E, A>, fe: (E) -> (EE)): Kind<F, A> =
    value.fold({ e -> raiseError(fe(e))}, { it.just() })

private fun <F, A, E, EE> Validated<E, A>.mapError(AE: ApplicativeError<F, EE>, fe: (E) -> (EE)): Kind<F, A> =
    fold({ e -> AE.raiseError(fe(e))}, { AE.just(it) })


