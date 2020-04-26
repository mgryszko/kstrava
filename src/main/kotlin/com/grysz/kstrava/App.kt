package com.grysz.kstrava

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import java.io.File
import java.math.BigDecimal

typealias IOE<A, B> = IO<Either<A, B>>

fun readAccessToken(tokenFileName: String): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(File(tokenFileName).readText()) })
}

fun getActivities(accessToken: AccessToken, baseUrl: String = "https://www.strava.com"): IOE<ListActivitiesError, List<Activity>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${accessToken.token}")
        .responseObject<List<Activity>>()
    result.fold({ it.right() }, { StravaApiError(it.exception).left() })
}

typealias ReadAccessToken = (String) -> IOE<ListActivitiesError, AccessToken>
typealias GetActivities = (AccessToken) -> IOE<ListActivitiesError, List<Activity>>

data class AccessToken(val token: String) {
    init {
        require(token.isNotBlank()) { "token must not be blank" }
    }
}

data class Activity(
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

sealed class ListActivitiesError

object TokenAccessError : ListActivitiesError()

data class StravaApiError(val exception: Throwable) : ListActivitiesError()

fun listActitivies(
    readAccessToken: ReadAccessToken,
    getActivities: GetActivities,
    accessTokenFileName: String
): IOE<ListActivitiesError, List<Activity>> =
    IO.fx {
        val maybeAccessToken = !readAccessToken(accessTokenFileName)
        !maybeAccessToken.fold(
            { l -> IO.just(l.left()) },
            { accessToken -> getActivities(accessToken) }
        )
    }


fun app(accessTokenFileName: String): IO<Unit> =
    IO.fx {
        val maybeActivities = !listActitivies(::readAccessToken, ::getActivities, accessTokenFileName)
        !maybeActivities.fold(
            { e -> IO { println("error: $e") } },
            { activities -> IO { println("activities: $activities") } }
        )
    }

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    val accessTokenFileName: String by option(help = "File name containing access token").default(".access-token")

    override fun run() {
        app(accessTokenFileName).unsafeRunSync()
    }
}.main(args)
