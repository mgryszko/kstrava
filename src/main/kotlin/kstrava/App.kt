package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.right
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
import com.grysz.kstrava.strava.getActivities
import com.grysz.kstrava.strava.getAthlete
import com.grysz.kstrava.strava.getAthleteActivities
import com.grysz.kstrava.table.printActivitiesTable
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.readAccessToken
import java.time.LocalDateTime

typealias IOE<A, B> = IO<Either<A, B>>

fun <E, F, A, B> liftEitherT(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun listActivitiesApp(accessTokenFileName: String): IO<Unit> {
    val C: Concurrent<EitherTPartialOf<ListActivitiesError, ForIO>> = EitherT.concurrent(IO.concurrent())
    val readAccessToken = liftEitherT(::readAccessToken)
    val getActivities = { accessToken: AccessToken ->
            C.parApplicative(dispatchers().io()).run {
                getActivities(
                    liftEitherT(::getAthleteActivities),
                    liftEitherT(::getAthlete),
                    accessToken
                )
            }
        }

    val maybeActivities = C.run {
        listActitivies(
            readAccessToken,
            getActivities,
            accessTokenFileName
        ).fix()
    }
    return maybeActivities.fold(
        IO.functor(),
        { e -> println("error: $e") },
        ::printActivitiesTable
    ).fix()
}

fun updateActivitiesApp(accessTokenFileName: String, activityId: Long, name: String): IO<Unit> {
    val C: Concurrent<EitherTPartialOf<ListActivitiesError, ForIO>> = EitherT.concurrent(IO.concurrent())
    val readAccessToken = liftEitherT(::readAccessToken)
    val updateActivity = { accessToken: AccessToken, activityId: ActivityId, activityName: ActivityName ->
            EitherT(IO {
                Activity(
                    id = 0,
                    distance = Distance(meters = 1),
                    gear = null,
                    name = "",
                    private = false,
                    startDate = LocalDateTime.MIN,
                    type = ""
                ).right()
            })
        }
    val maybeActivities = C.run {
        updateActitivies(
            readAccessToken,
            updateActivity,
            accessTokenFileName,
            ActivityId(activityId),
            ActivityName(name)
        ).fix()
    }
    return maybeActivities.fold(
        IO.functor(),
        { e -> println("error: $e") },
        { activity -> printActivitiesTable(listOf(activity)) }
    ).fix()
}