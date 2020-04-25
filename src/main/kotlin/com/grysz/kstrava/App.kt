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
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import java.io.File
import java.math.BigDecimal

typealias IOE<A, B> = IO<Either<A, B>>

fun readToken(tokenFileName: String): IOE<TokenAccessError, String> = IO {
    Either.catch({ _ -> TokenAccessError }, { File(tokenFileName).readText() })
}

fun getActivities(accessToken: String, baseUrl: String = "https://www.strava.com"): IO<Either<FuelError, List<Activity>>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer $accessToken")
        .responseObject<List<Activity>>()
    result.fold({ it.right() }, { it.left() })
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

object TokenAccessError

fun app(accessTokenFileName: String): IO<Unit> =
    IO.fx {
        val tokenOrError = !readToken(accessTokenFileName)
        !tokenOrError.fold({ e -> IO { println("error: $e") } }, { token -> IO { println("token: $token") } })
    }

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    val accessTokenFileName: String by option(help="File name containing access token").default(".access-token")

    override fun run() {
        app(accessTokenFileName).unsafeRunSync()
    }
}.main(args)
