package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.concurrent.dispatchers
import arrow.fx.extensions.io.functor.functor
import arrow.fx.fix
import arrow.fx.mtl.concurrent
import arrow.fx.typeclasses.Concurrent
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.fix
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.grysz.kstrava.strava.getActivities
import com.grysz.kstrava.strava.getAthlete
import com.grysz.kstrava.strava.getAthleteActivities
import com.grysz.kstrava.table.printActivitiesTable
import com.grysz.kstrava.token.readAccessToken

typealias IOE<A, B> = IO<Either<A, B>>

fun <E, F, A, B> lift(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun app(accessTokenFileName: String): IO<Unit> {
    val C: Concurrent<EitherTPartialOf<ListActivitiesError, ForIO>> = EitherT.concurrent(IO.concurrent())
    val readAccessToken = lift(::readAccessToken)
    val getActivities: (AccessToken) -> Kind<EitherTPartialOf<ListActivitiesError, ForIO>, List<Activity>> =
        { accessToken: AccessToken ->
            getActivities(
                C.parApplicative(dispatchers().io()),
                lift(::getAthleteActivities),
                lift(::getAthlete),
                accessToken
            )
        }

    val maybeActivities = listActitivies(
        C,
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
