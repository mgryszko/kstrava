package com.grysz.kstrava.kstrava.activities

import arrow.Kind
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.AccessTokenFileNameBlankError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.mapError
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
