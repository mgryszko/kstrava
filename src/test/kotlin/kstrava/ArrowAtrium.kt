package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.ForId
import arrow.core.fix
import arrow.core.value
import arrow.fx.IO
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.FeatureExpect

val <A> Expect<IO<A>>.run: FeatureExpect<IO<A>, Either<Throwable, A>>
    get() = feature("unsafeRunSync") { attempt().unsafeRunSync() }

val <A, B> Expect<IOE<A, B>>.runE: FeatureExpect<IOE<A, B>, Either<A, B>>
    get() = feature("unsafeRunSync") { unsafeRunSync() }

val <A> Expect<Kind<ForId, A>>.value: FeatureExpect<Kind<ForId, A>, A>
    get() = feature("value") { fix().value() }

val <A, B> Expect<Either<A, B>>.right: Expect<B>
    get() = isA<Either.Right<B>>().feature { f(it::b) }

val <A, B> Expect<Either<A, B>>.left: Expect<A>
    get() = isA<Either.Left<A>>().feature { f(it::a) }

