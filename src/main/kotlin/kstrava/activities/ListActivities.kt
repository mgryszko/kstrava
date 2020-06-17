package com.grysz.kstrava.kstrava.activities

import arrow.Kind
import arrow.core.Validated
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.AccessTokenFileNameBlankError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName

fun <F> MonadError<F, ActivitiesError>.listActitivies(
    readAccessToken: (AccessTokenFileName) -> Kind<F, AccessToken>,
    getActivities: (AccessToken) -> Kind<F, List<Activity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    fx.monad {
        val validated = !AccessTokenFileName.create(accessTokenFileName).mapError(this@listActitivies) { AccessTokenFileNameBlankError }
        val token = !readAccessToken(validated)
        !getActivities(token)
    }

private fun <F, A, E, EE> MonadError<F, EE>.mapError(value: Validated<E, A>, fe: (E) -> (EE)): Kind<F, A> =
    value.fold({ e -> raiseError(fe(e))}, { it.just() })

private fun <F, A, E, EE> Validated<E, A>.mapError(AE: ApplicativeError<F, EE>, fe: (E) -> (EE)): Kind<F, A> =
    fold({ e -> AE.raiseError(fe(e))}, { AE.just(it) })


