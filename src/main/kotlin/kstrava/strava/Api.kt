package com.grysz.kstrava.strava

import arrow.Kind
import arrow.core.extensions.list.traverse.traverse
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.typeclasses.Applicative
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import com.grysz.kstrava.IOE
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.activities.Distance
import com.grysz.kstrava.activities.Gear
import com.grysz.kstrava.token.AccessToken
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder

data class StravaClientError(val exception: Throwable)

data class ApiActivity(
    val id: Long,
    val distance: BigDecimal,
    val gear_id: String?,
    val name: String,
    val private: Boolean,
    val start_date_local: String,
    val type: String
)

data class UpdatableApiActivity(
    val name: String
)

data class ApiAthlete(
    val bikes: List<ApiGear>,
    val shoes: List<ApiGear>
)

data class ApiGear(val id: String, val name: String)

fun getAthleteActivities(
    token: AccessToken,
    baseUrl: String = "https://www.strava.com"
): IOE<StravaClientError, List<ApiActivity>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${token.token}")
        .responseObject<List<ApiActivity>>()
    result.fold({ it.right() }, { StravaClientError(it.exception).left() })
}

fun updateAthleteActivity(
    token: AccessToken,
    id: Long,
    activity: UpdatableApiActivity,
    baseUrl: String = "https://www.strava.com"
): IOE<StravaClientError, ApiActivity> = IO {
    val path = "$baseUrl/api/v3/activities/${id}"

    val (_, _, result) = Fuel.put(path)
        .header(Headers.AUTHORIZATION, "Bearer ${token.token}")
        .objectBody(activity)
        .responseObject<ApiActivity>()
    result.fold({ it.right() }, { StravaClientError(it.exception).left() })
}

fun getAthlete(
    token: AccessToken,
    baseUrl: String = "https://www.strava.com"
): IOE<StravaClientError, ApiAthlete> = IO {
    val path = "$baseUrl/api/v3/athlete"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${token.token}")
        .responseObject<ApiAthlete>()
    result.fold({ it.right() }, { StravaClientError(it.exception).left() })
}

fun <F> Applicative<F>.getActivities(
    getAthleteActivities: (AccessToken) -> Kind<F, List<ApiActivity>>,
    getAthlete: (AccessToken) -> Kind<F, ApiAthlete>,
    token: AccessToken
): Kind<F, List<Activity>> =
    getAthleteActivities(token).map2(getAthlete(token)) { (apiActivities, apiAthlete) ->
        apiActivities.map { toActivity(it, apiAthlete) }
    }

fun <F> Applicative<F>.updateActivities(
    updateAthleteActivity: (AccessToken, Long, UpdatableApiActivity) -> Kind<F, ApiActivity>,
    getAthlete: (AccessToken) -> Kind<F, ApiAthlete>,
    token: AccessToken,
    activityIds: List<ActivityId>,
    activityName: ActivityName
): Kind<F, List<Activity>> =
    updateAthleteActivities(updateAthleteActivity, activityIds.map(ActivityId::id), token, UpdatableApiActivity(name = activityName.name))
        .map2(getAthlete(token)) { (apiActivities, apiAthlete) ->
            apiActivities.map { toActivity(it, apiAthlete) }
        }

private fun <F> Applicative<F>.updateAthleteActivities(
    updateAthleteActivity: (AccessToken, Long, UpdatableApiActivity) -> Kind<F, ApiActivity>,
    activityIds: List<Long>,
    token: AccessToken,
    updatableApiActivity: UpdatableApiActivity
): Kind<F, List<ApiActivity>> = activityIds.traverse(this) { updateAthleteActivity(token, it, updatableApiActivity) }
    .map { it.fix() }

private fun toActivity(activity: ApiActivity, apiAthlete: ApiAthlete): Activity {
    fun toDistance(meters: BigDecimal) = meters.round(MathContext(0, RoundingMode.FLOOR)).toInt()

    return Activity(
        id = activity.id,
        distance = Distance(toDistance(activity.distance)),
        gear = activity.gear_id?.let { apiAthlete.findGear(activity.gear_id)?.let { Gear(it.id, it.name) } },
        name = activity.name,
        private = activity.private,
        startDate = LocalDateTime.parse(activity.start_date_local, localDateTimeFormatter),
        type = activity.type
    )
}

private fun ApiAthlete.findGear(gearId: String) = (bikes + shoes).find { it.id == gearId }

private val localDateTimeFormatter = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral('T')
    .append(ISO_LOCAL_TIME)
    .appendLiteral('Z')
    .toFormatter()

