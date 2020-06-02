package com.grysz.kstrava

import arrow.Kind
import arrow.core.Option
import arrow.core.Option.Companion.empty
import arrow.typeclasses.Monad
import com.grysz.kstrava.token.AccessToken

fun <F> Monad<F>.updateActitivies(
    readAccessToken: (String) -> Kind<F, AccessToken>,
    accessTokenFileName: String,
    activityId: ActivityId,
    activityName: ActivityName
): Kind<F, Option<Activity>> =
    fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        empty()
    }

