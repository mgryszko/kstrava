package com.grysz.kstrava.kstrava.activities

import arrow.Kind
import arrow.core.Nel
import arrow.core.Tuple2
import arrow.core.Validated
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import arrow.core.extensions.validated.bifunctor.mapLeft
import arrow.core.extensions.validated.functor.widen
import arrow.core.invalidNel
import arrow.core.nel
import arrow.core.validNel
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.AccessTokenFileNameBlankError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.activities.EmptyActivityIdsError
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName

fun <F> MonadError<F, Nel<ActivitiesError>>.updateActitivies(
    readAccessToken: (AccessTokenFileName) -> Kind<F, AccessToken>,
    updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<F, List<Activity>>,
    accessTokenFileName: String,
    activityIds: List<ActivityId>,
    activityName: ActivityName
): Kind<F, List<Activity>> =
    validate(activityIds, accessTokenFileName)
        .fold(::raiseError) { (validActivityIds, validFileName) ->
            fx.monad {
                val token = !readAccessToken(validFileName)
                !updateActivities(token, validActivityIds, activityName)
            }
        }

private fun validate(
    activityIds: List<ActivityId>,
    accessTokenFileName: String
): Validated<Nel<ActivitiesError>, Tuple2<List<ActivityId>, AccessTokenFileName>> {
    val validActivityIds = if (activityIds.isEmpty()) EmptyActivityIdsError.invalidNel() else activityIds.validNel()
    val validFileName = AccessTokenFileName.create(accessTokenFileName)
        .mapLeft { AccessTokenFileNameBlankError.nel() }

    return with(Validated.applicative<Nel<ActivitiesError>>(Nel.semigroup())) {
        validActivityIds.product(validFileName)
    }.widen()
}

