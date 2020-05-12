package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import arrow.core.fix
import arrow.core.value
import arrow.fx.IO
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.creating.FeatureExpect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.mockk.every
import io.mockk.mockk
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
            expect(readAccessToken(accessTokenFile.canonicalPath)).runE.right.toBe(AccessToken("::token::"))
        }
    }

    @Nested
    @DisplayName("missing access token")
    inner class MissingAccessToken {
        @Test
        fun `read token`() {
            expect(readAccessToken("non-existing")).runE.left.toBe(TokenAccessError)
        }
    }
}

class GetActivitiesTest {
    val wm: WireMockServer = WireMockServer(options().dynamicPort().notifier(ConsoleNotifier(false)))

    val accessToken = AccessToken("::token::")

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
                .withHeader("Authorization", equalTo("Bearer ${accessToken.token}"))
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
            ApiActivity(
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

class ListActivitiesWorkflowTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<ForId, List<ApiActivity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val accessToken = AccessToken("::token::")
    val apiActivity = ApiActivity(0, 123.56.toBigDecimal(), "", "", false, "", "", "", "")
    val apiActivities = listOf(apiActivity)
    val activity = Activity(0, Distance(123), "", "", false, "", "", "", "")
    val activities = listOf(activity)

    @Test
    fun list() {
        every { readAccessToken(accessTokenFileName) } returns Id(accessToken)
        every { getActivities(accessToken) } returns Id(apiActivities)

        expect(listActitivies(Id.monad(), readAccessToken, getActivities, accessTokenFileName)).value.toBe(activities)
    }
}

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

