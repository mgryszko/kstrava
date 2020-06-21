package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.concurrent.dispatchers
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.mtl.concurrent
import arrow.fx.typeclasses.Concurrent
import arrow.mtl.EitherT
import arrow.mtl.EitherTPartialOf
import arrow.mtl.extensions.eithert.monadError.monadError
import arrow.mtl.fix
import arrow.typeclasses.Functor
import arrow.typeclasses.MonadError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.activities.StravaError
import com.grysz.kstrava.activities.TokenAccessError
import com.grysz.kstrava.kstrava.activities.listActitivies
import com.grysz.kstrava.kstrava.activities.updateActitivies
import com.grysz.kstrava.strava.getActivities
import com.grysz.kstrava.strava.getAthlete
import com.grysz.kstrava.strava.getAthleteActivities
import com.grysz.kstrava.strava.updateActivities
import com.grysz.kstrava.strava.updateAthleteActivity
import com.grysz.kstrava.table.printActivitiesTable
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.readAccessToken

typealias IOE<A, B> = IO<Either<A, B>>

fun <E, F, A, B> liftEitherT(f: (A) -> Kind<F, Either<E, B>>): (A) -> EitherT<E, F, B> =
    { a -> EitherT(f(a)) }

fun <E, F, A, B, C, D> liftEitherT(f: (A, B, C) -> Kind<F, Either<E, D>>): (A, B, C) -> EitherT<E, F, D> =
    { a, b, c -> EitherT(f(a, b, c)) }

fun <E, EE, F, A, B> ((A) -> EitherT<E, F, B>).mapError(FF: Functor<F>, ml: (E) -> EE): (A) -> EitherT<EE, F, B> =
    { a -> this(a).mapLeft(FF, ml) }

fun <E, EE, F, A, B, C, D> ((A, B, C) -> EitherT<E, F, D>).mapError(FF: Functor<F>, ml: (E) -> EE): (A, B, C) -> EitherT<EE, F, D> =
    { a, b, c -> this(a, b, c).mapLeft(FF, ml) }

fun listActivitiesApp(accessTokenFileName: String): IO<Unit> {
    val C: Concurrent<EitherTPartialOf<ActivitiesError, ForIO>> = EitherT.concurrent(IO.concurrent())
    val ME: MonadError<EitherTPartialOf<ActivitiesError, ForIO>, ActivitiesError> = EitherT.monadError(IO.monad())
    val getActivities = { token: AccessToken ->
        C.parApplicative(dispatchers().io()).run {
            getActivities(
                liftEitherT(::getAthleteActivities).mapError(IO.functor()) { StravaError(it.exception) },
                liftEitherT(::getAthlete).mapError(IO.functor()) { StravaError(it.exception) },
                token
            )
        }
    }

    val maybeActivities = ME.run {
        listActitivies(
            liftEitherT(::readAccessToken).mapError(IO.functor()) { TokenAccessError(it.exception) },
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

fun updateActivitiesApp(accessTokenFileName: String, activityIds: List<Long>, name: String): IO<Unit> {
    val C: Concurrent<EitherTPartialOf<ActivitiesError, ForIO>> = EitherT.concurrent(IO.concurrent())
    val ME: MonadError<EitherTPartialOf<ActivitiesError, ForIO>, ActivitiesError> = EitherT.monadError(IO.monad())
    val updateActivities = { token: AccessToken, activityIds: List<ActivityId>, activityName: ActivityName ->
        C.parApplicative(dispatchers().io()).run {
            updateActivities(
                liftEitherT(::updateAthleteActivity).mapError(IO.functor()) { StravaError(it.exception) },
                liftEitherT(::getAthlete).mapError(IO.functor()) { StravaError(it.exception) },
                token,
                activityIds,
                activityName
            )
        }
    }
    val maybeActivities = ME.run {
        updateActitivies(
            liftEitherT(::readAccessToken).mapError(IO.functor()) { TokenAccessError(it.exception) },
            updateActivities,
            accessTokenFileName,
            activityIds.map(::ActivityId),
            ActivityName(name)
        ).fix()
    }
    return maybeActivities.fold(
        IO.functor(),
        { e -> println("error: $e") },
        ::printActivitiesTable
    ).fix()
}