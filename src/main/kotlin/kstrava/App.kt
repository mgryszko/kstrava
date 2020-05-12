package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import arrow.typeclasses.Monad
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.jackson.responseObject
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

typealias IOE<A, B> = IO<Either<A, B>>

fun readAccessToken(tokenFileName: String): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(File(tokenFileName).readText()) })
}

fun getActivities(accessToken: AccessToken, baseUrl: String = "https://www.strava.com"): IOE<ListActivitiesError, List<ApiActivity>> = IO {
    val path = "$baseUrl/api/v3/athlete/activities"

    val (_, _, result) = Fuel.get(path)
        .header(Headers.AUTHORIZATION, "Bearer ${accessToken.token}")
        .responseObject<List<ApiActivity>>()
    result.fold({ it.right() }, { StravaApiError(it.exception).left() })
}

data class AccessToken(val token: String) {
    init {
        require(token.isNotBlank()) { "token must not be blank" }
    }
}

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

data class Activity(
    val id: Long,
    val distance: Distance,
    val gear_id: String?,
    val name: String,
    val private: Boolean,
    val start_date: String,
    val start_date_local: String,
    val timezone: String,
    val type: String
)

data class Distance(val meters: Int) {
    init {
        require(meters > 0) { "distance in meters must be greater than 0" }
    }
}

sealed class ListActivitiesError

object TokenAccessError : ListActivitiesError()

data class StravaApiError(val exception: Throwable) : ListActivitiesError()

fun <F> listActitivies(
    M: Monad<F>,
    readAccessToken: (String) -> Kind<F, AccessToken>,
    getActivities: (AccessToken) -> Kind<F, List<ApiActivity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    M.fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        val apiActivities = !getActivities(accessToken)
        apiActivities.map {
            Activity(
                id = it.id,
                distance = Distance(meters = it.distance.round(MathContext(0, RoundingMode.FLOOR)).toInt()),
                gear_id = it.gear_id,
                name = it.name,
                private = it.private,
                start_date = it.start_date,
                start_date_local = it.start_date_local,
                timezone = it.timezone,
                type = it.type
            )
        }
    }

fun <E, F, A, B> lift(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun app(accessTokenFileName: String): IO<Unit> {
    val M = EitherT.monad<ListActivitiesError, ForIO>(IO.monad())
    val maybeActivities = listActitivies(
        M,
        lift(::readAccessToken),
        lift(::getActivities),
        accessTokenFileName
    ).fix()
    return maybeActivities.fold(
        IO.functor(),
        { e -> println("error: $e") },
        ::printActivitiesTable
    ).fix()
}

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    val accessTokenFileName: String by option(help = "File name containing access token").default(".access-token")

    override fun run() {
        app(accessTokenFileName).unsafeRunSync()
    }
}.main(args)
