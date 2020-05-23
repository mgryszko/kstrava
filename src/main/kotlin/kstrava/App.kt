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
import kstrava.strava.getAthlete
import kstrava.strava.getAthleteActivities
import kstrava.token.readAccessToken

typealias IOE<A, B> = IO<Either<A, B>>

fun <E, F, A, B> lift(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun app(accessTokenFileName: String): IO<Unit> {
    val M: Monad<EitherTPartialOf<ListActivitiesError, ForIO>> = EitherT.monad(IO.monad())
    val readAccessToken = lift(::readAccessToken)
    val getActivities: (AccessToken) -> Kind<EitherTPartialOf<ListActivitiesError, ForIO>, List<Activity>> =
        { accessToken: AccessToken -> getActivities(M, lift(::getAthleteActivities), lift(::getAthlete), accessToken) }

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
