package com.grysz.kstrava

import arrow.Kind
import arrow.typeclasses.Monad
import com.grysz.kstrava.token.AccessToken

fun <F> Monad<F>.updateActitivies(
    readAccessToken: (String) -> Kind<F, AccessToken>,
    updateActivity: (AccessToken, ActivityId, ActivityName) -> Kind<F, Activity>,
    accessTokenFileName: String,
    activityId: ActivityId,
    activityName: ActivityName
): Kind<F, Activity> =
    fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        !updateActivity(accessToken, activityId, activityName)
    }

