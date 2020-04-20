package com.grysz.kstrava

import arrow.core.Either
import arrow.fx.IO
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.FeatureExpect
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.AfterTest
import kotlin.test.Test

class AppTest {
    @Nested
    @DisplayName("existing access token")
    inner class ExistingAccessToken {
        val accessTokenFile = createTempFile().apply {
            delete()
        }

        @AfterTest
        fun afterTest() {
            accessTokenFile.delete()
        }

        @Test
        fun `read token`() {
            accessTokenFile.writeText("::token::")
            expect(readToken(accessTokenFile.canonicalPath)).runE.right.toBe("::token::")
        }
    }

    @Nested
    @DisplayName("missing access token")
    inner class MissingAccessToken {
        @Test
        fun `read token`() {
            expect(readToken("non-existing")).runE.left.toBe(TokenAccessError)
        }
    }
}

val <A> Expect<IO<A>>.run: FeatureExpect<IO<A>, Either<Throwable, A>>
    get() = feature("unsafeRunSync") { attempt().unsafeRunSync() }

val <A, B> Expect<IO<Either<A, B>>>.runE: FeatureExpect<IO<Either<A, B>>, Either<A, B>>
    get() = feature("unsafeRunSync") { unsafeRunSync() }

val <A, B> Expect<Either<A, B>>.right: Expect<B>
    get() = isA<Either.Right<B>>().feature { f(it::b) }

val <A, B> Expect<Either<A, B>>.left: Expect<A>
    get() = isA<Either.Left<A>>().feature { f(it::a) }

