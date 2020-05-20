package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.mockk.every
import io.mockk.mockk
import kstrava.strava.ApiActivity
import kstrava.strava.getActivities
import kstrava.strava.getAthleteActivities
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import kotlin.test.Test

private val accessToken = AccessToken("::token::")

class GetAthleteActivitiesTest {
    val wm: WireMockServer = WireMockServer(options().dynamicPort().notifier(ConsoleNotifier(false)))

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
                .willReturn(
                    okJson(
                        """[{
    "id": 1,
    "distance": 101.22,
    "gear_id": "::gearId1::",
    "name": "::name1::",
    "private": true,
    "start_date_local": "2020-01-02T03:04:05Z",
    "type": "::type::"
}]"""
                    )
                )
        )

        expect(getAthleteActivities(accessToken, "http://localhost:${wm.port()}")).runE.right.toBe(
            listOf(
                ApiActivity(
                    id = 1,
                    distance = 101.22.toBigDecimal(),
                    gear_id = "::gearId1::",
                    name = "::name1::",
                    private = true,
                    start_date_local = "2020-01-02T03:04:05Z",
                    type = "::type::"
                )
            )
        )
    }

    @Test
    fun unauthorized() {
        wm.stubFor(
            get(urlMatching("/api/v3/athlete/activities"))
                .willReturn(status(401))
        )

        expect(getAthleteActivities(accessToken, "http://localhost:${wm.port()}")).runE.left.isA<StravaApiError>()
    }
}

class GetActivitiesTest {
    val getAthleteActivities: (AccessToken) -> Kind<ForId, List<ApiActivity>> = mockk()

    @Test
    fun `get activities`() {
        val apiActivities = listOf(
            ApiActivity(
                id = 1,
                distance = 123.99.toBigDecimal(),
                gear_id = "::gearId::",
                name = "::name::",
                private = true,
                start_date_local = "2020-01-02T03:04:05Z",
                type = "::type::"
            )
        )
        val activities = listOf(
            Activity(
                id = 1,
                distance = Distance(123),
                gear = Gear("::gearId::", ""),
                name = "::name::",
                private = true,
                startDate = LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                type = "::type::"
            )
        )

        every { getAthleteActivities(accessToken) } returns Id(apiActivities)

        expect(getActivities(Id.monad(), getAthleteActivities, accessToken)).value.toBe(activities)
    }
}