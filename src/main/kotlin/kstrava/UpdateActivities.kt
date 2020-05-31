package com.grysz.kstrava

import arrow.Kind
import arrow.typeclasses.Monad
import com.grysz.kstrava.token.AccessToken

fun <F> Monad<F>.updateActitivies(
    readAccessToken: (String) -> Kind<F, AccessToken>,
    accessTokenFileName: String,
    activityIds: List<ActivityId>,
    activityName: ActivityName
): Kind<F, List<Activity>> =
    fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        emptyList()
    }

