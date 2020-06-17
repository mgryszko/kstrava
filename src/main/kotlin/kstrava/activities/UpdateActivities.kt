package com.grysz.kstrava.kstrava.activities

import arrow.Kind
import arrow.typeclasses.Monad
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.token.AccessToken

fun <F> Monad<F>.updateActitivies(
    readAccessToken: (String) -> Kind<F, AccessToken>,
    updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<F, List<Activity>>,
    accessTokenFileName: String,
    activityIds: List<ActivityId>,
    activityName: ActivityName
): Kind<F, List<Activity>> =
    fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        !updateActivities(accessToken, activityIds, activityName)
    }

