package com.grysz.kstrava

import arrow.core.Either
import arrow.fx.IO
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.FeatureExpect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.AfterTest
import kotlin.test.Test

class ReadTokenTest {
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

class GetActivitiesTest {
    val wm: WireMockServer = WireMockServer(options().dynamicPort())

    val accessToken = "::token::"

    @BeforeEach
    fun setUp() {
        wm.start()
    }

    @AfterEach
    fun tearDown() {
        wm.stop()
    }

    @Test
    fun ok() {
        wm.stubFor(
            get(urlMatching("/api/v3/athlete/activities"))
                .withHeader("Authorization", equalTo("Bearer $accessToken"))
                .willReturn(okJson("""[{
    "id": 1,
    "distance": 101.22,
    "gear_id": "::gearId1::",
    "name": "::name1::",
    "private": true,
    "start_date": "2020-01-02T03:04:05",
    "start_date_local": "2020-01-02T03:04:05Z",
    "timezone": "(GMT+01:00) Europe/Madrid",
    "type": "::type::"
}]""")))

        expect(getActivities(accessToken, "http://localhost:${wm.port()}")).runE.right.toBe(listOf(
            Activity(
                id = 1,
                distance = 101.22.toBigDecimal(),
                gear_id = "::gearId1::",
                name = "::name1::",
                private = true,
                start_date = "2020-01-02T03:04:05",
                start_date_local = "2020-01-02T03:04:05Z",
                timezone = "(GMT+01:00) Europe/Madrid",
                type = "::type::"
            )
        ))
    }
}

val <A> Expect<IO<A>>.run: FeatureExpect<IO<A>, Either<Throwable, A>>
    get() = feature("unsafeRunSync") { attempt().unsafeRunSync() }

val <A, B> Expect<IOE<A, B>>.runE: FeatureExpect<IOE<A, B>, Either<A, B>>
    get() = feature("unsafeRunSync") { unsafeRunSync() }

val <A, B> Expect<Either<A, B>>.right: Expect<B>
    get() = isA<Either.Right<B>>().feature { f(it::b) }

val <A, B> Expect<Either<A, B>>.left: Expect<A>
    get() = isA<Either.Left<A>>().feature { f(it::a) }

