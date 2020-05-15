package kstrava.strava

import arrow.Kind
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.typeclasses.Monad
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import com.grysz.kstrava.AccessToken
import com.grysz.kstrava.Activity
import com.grysz.kstrava.Distance
import com.grysz.kstrava.IOE
import com.grysz.kstrava.ListActivitiesError
import com.grysz.kstrava.StravaApiError
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class ApiActivity(
    val id: Long,
    val distance: BigDecimal,
    val gear_id: String?,
    val name: String,
    val private: Boolean,
    val start_date: String,
    val start_date_local: String,
    val timezone: String,
    val type: String
)

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

fun <F> getActivities(
    M: Monad<F>,
    getAthleteActivities: (AccessToken) -> Kind<F, List<ApiActivity>>,
    accessToken: AccessToken
): Kind<F, List<Activity>> =
    M.fx.monad {
        val apiActivities = !getAthleteActivities(accessToken)
        apiActivities.map(::toActivity)
    }

private fun toActivity(activity: ApiActivity): Activity {
    fun toDistance(meters: BigDecimal) = meters.round(MathContext(0, RoundingMode.FLOOR)).toInt()

    return Activity(
        id = activity.id,
        distance = Distance(toDistance(activity.distance)),
        gear_id = activity.gear_id,
        name = activity.name,
        private = activity.private,
        start_date = activity.start_date,
        start_date_local = activity.start_date_local,
        timezone = activity.timezone,
        type = activity.type
    )
}

