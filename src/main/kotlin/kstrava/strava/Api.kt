package com.grysz.kstrava.strava

import arrow.Kind
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.typeclasses.Concurrent
import arrow.typeclasses.Monad
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import com.grysz.kstrava.AccessToken
import com.grysz.kstrava.Activity
import com.grysz.kstrava.Distance
import com.grysz.kstrava.Gear
import com.grysz.kstrava.IOE
import com.grysz.kstrava.ListActivitiesError
import com.grysz.kstrava.StravaApiError
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
import java.time.format.DateTimeFormatterBuilder

data class ApiActivity(
    val id: Long,
    val distance: BigDecimal,
    val gear_id: String?,
    val name: String,
    val private: Boolean,
    val start_date_local: String,
    val type: String
)

data class ApiAthlete(
    val bikes: List<ApiGear>,
    val shoes: List<ApiGear>
)

data class ApiGear(val id: String, val name: String)

fun getAthleteActivities(
    accessToken: AccessToken,
    baseUrl: String = "https://www.strava.com"
): IOE<ListActivitiesError, List<ApiActivity>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${accessToken.token}")
        .responseObject<List<ApiActivity>>()
    result.fold({ it.right() }, { StravaApiError(it.exception).left() })
}

fun getAthlete(
    accessToken: AccessToken,
    baseUrl: String = "https://www.strava.com"
): IOE<ListActivitiesError, ApiAthlete> = IO {
    val path = "$baseUrl/api/v3/athlete"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${accessToken.token}")
        .responseObject<ApiAthlete>()
    result.fold({ it.right() }, { StravaApiError(it.exception).left() })
}

fun <F> getActivities(
    M: Monad<F>,
    getAthleteActivities: (AccessToken) -> Kind<F, List<ApiActivity>>,
    getAthlete: (AccessToken) -> Kind<F, ApiAthlete>,
    accessToken: AccessToken
): Kind<F, List<Activity>> =
    M.fx.monad {
        val apiActivities = !getAthleteActivities(accessToken)
        val apiAthlete = !getAthlete(accessToken)
        apiActivities.map { toActivity(it, apiAthlete) }
    }

fun <F> getActivities(
    C: Concurrent<F>,
    getAthleteActivities: (AccessToken) -> Kind<F, List<ApiActivity>>,
    getAthlete: (AccessToken) -> Kind<F, ApiAthlete>,
    accessToken: AccessToken
): Kind<F, List<Activity>> = C.run {
    parMapN(dispatchers().io(), getAthleteActivities(accessToken), getAthlete(accessToken)) { (apiActivities, apiAthlete) ->
        apiActivities.map { toActivity(it, apiAthlete) }
    }
}

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

