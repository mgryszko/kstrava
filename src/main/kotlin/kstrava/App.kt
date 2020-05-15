package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.fix
import arrow.typeclasses.Monad
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kstrava.strava.getActivities
import kstrava.strava.getAthleteActivities
import java.io.File

typealias IOE<A, B> = IO<Either<A, B>>

fun readAccessToken(tokenFileName: String): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(File(tokenFileName).readText()) })
}

data class AccessToken(val token: String) {
    init {
        require(token.isNotBlank()) { "token must not be blank" }
    }
}

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
    getActivities: (AccessToken) -> Kind<F, List<Activity>>,
    accessTokenFileName: String
): Kind<F, List<Activity>> =
    M.fx.monad {
        val accessToken = !readAccessToken(accessTokenFileName)
        !getActivities(accessToken)
    }

fun <E, F, A, B> lift(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun app(accessTokenFileName: String): IO<Unit> {
    val M: Monad<EitherTPartialOf<ListActivitiesError, ForIO>> = EitherT.monad(IO.monad())
    val readAccessToken = lift(::readAccessToken)
    val getActivities: (AccessToken) -> Kind<EitherTPartialOf<ListActivitiesError, ForIO>, List<Activity>> =
        { accessToken: AccessToken -> getActivities(M, lift(::getAthleteActivities), accessToken) }

    val maybeActivities = listActitivies(
        M,
        readAccessToken,
        getActivities,
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
