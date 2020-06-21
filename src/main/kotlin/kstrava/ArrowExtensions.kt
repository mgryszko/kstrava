package com.grysz.kstrava

import arrow.Kind
import arrow.core.Validated
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.MonadError

fun <F, A, E, EE> MonadError<F, EE>.mapError(value: Validated<E, A>, fe: (E) -> (EE)): Kind<F, A> =
    value.fold({ e -> raiseError(fe(e))}, { it.just() })

fun <F, A, E, EE> Validated<E, A>.mapError(AE: ApplicativeError<F, EE>, fe: (E) -> (EE)): Kind<F, A> =
    fold({ e -> AE.raiseError(fe(e))}, { AE.just(it) })

