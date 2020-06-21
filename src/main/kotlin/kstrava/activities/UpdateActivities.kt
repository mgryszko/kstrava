package com.grysz.kstrava.kstrava.activities

import arrow.Kind
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.AccessTokenFileNameBlankError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.mapError
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName

fun <F> MonadError<F, ActivitiesError>.updateActitivies(
    readAccessToken: (AccessTokenFileName) -> Kind<F, AccessToken>,
    updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<F, List<Activity>>,
    accessTokenFileName: String,
    activityIds: List<ActivityId>,
    activityName: ActivityName
): Kind<F, List<Activity>> =
    fx.monad {
        val validated = !AccessTokenFileName.create(accessTokenFileName).mapError(this@updateActitivies) { AccessTokenFileNameBlankError }
        val token = !readAccessToken(validated)
        !updateActivities(token, activityIds, activityName)
    }

