package kstrava.api

import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import com.grysz.kstrava.AccessToken
import com.grysz.kstrava.IOE
import com.grysz.kstrava.ListActivitiesError
import com.grysz.kstrava.StravaApiError
import java.math.BigDecimal

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

fun getActivities(accessToken: AccessToken, baseUrl: String = "https://www.strava.com"): IOE<ListActivitiesError, List<ApiActivity>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${accessToken.token}")
        .responseObject<List<ApiActivity>>()
    result.fold({ it.right() }, { StravaApiError(it.exception).left() })
}

