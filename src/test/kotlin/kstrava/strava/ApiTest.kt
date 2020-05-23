package com.grysz.kstrava.strava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import ch.tutteli.atrium.api.fluent.en_GB.all
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.grysz.kstrava.AccessToken
import com.grysz.kstrava.Activity
import com.grysz.kstrava.Distance
import com.grysz.kstrava.Gear
import com.grysz.kstrava.StravaApiError
import com.grysz.kstrava.left
import com.grysz.kstrava.right
import com.grysz.kstrava.runE
import com.grysz.kstrava.value
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.time.LocalDateTime
import kotlin.test.Test

private val accessToken = AccessToken("::token::")

class ApiTest {
    val wm: WireMockServer = WireMockServer(options().dynamicPort().notifier(ConsoleNotifier(false)))

    @BeforeEach
    fun setUp() {
        wm.start()
    }

    @AfterEach
    fun tearDown() {
        wm.stop()
    }

    @Nested
    @DisplayName("get activities")
    inner class GetActivities {
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
            wm.stubFor(get(urlMatching("/api/v3/athlete/activities")).willReturn(status(401)))

            expect(getAthleteActivities(accessToken, "http://localhost:${wm.port()}")).runE.left.isA<StravaApiError>()
        }
    }

    @Nested
    @DisplayName("get athlete")
    inner class GetAthlete {
        @Test
        fun ok() {
            wm.stubFor(
                get(urlMatching("/api/v3/athlete"))
                    .withHeader("Authorization", equalTo("Bearer ${accessToken.token}"))
                    .willReturn(
                        okJson(
                            """{
    "bikes": [
        {
            "id": "b0000001",
            "name": "::bike1::"
        },
        {
            "id": "b0000002",
            "name": "::bike2::"
        }
    ],
    "shoes": [
        {
            "id": "g0000001",
            "name": "::shoe1::"
        }
    ]
}"""
                        )
                    )
            )

            expect(getAthlete(accessToken, "http://localhost:${wm.port()}")).runE.right.toBe(
                ApiAthlete(
                    bikes = listOf(
                        ApiGear("b0000001", "::bike1::"),
                        ApiGear("b0000002", "::bike2::")
                    ),
                    shoes = listOf(
                        ApiGear("g0000001", "::shoe1::")
                    )
                )
            )
        }
    }
}

class GetActivitiesTest {
    val getAthlete: (AccessToken) -> Kind<ForId, ApiAthlete> = mockk()
    val getAthleteActivities: (AccessToken) -> Kind<ForId, List<ApiActivity>> = mockk()

    @Test
    fun `get activities`() {
        val apiAthlete = ApiAthlete(
            bikes = listOf(
                ApiGear(id = "::bikeId1::", name = "::bikeName1::"),
                ApiGear(id = "::bikeId2::", name = "::bikeName2::")
            ),
            shoes = listOf(
                ApiGear(id = "::shoeId::", name = "::shoeName::")
            )
        )
        val apiActivities = listOf(
            ApiActivity(
                id = 1,
                distance = 123.99.toBigDecimal(),
                gear_id = "::bikeId2::",
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
                gear = Gear("::bikeId2::", "::bikeName2::"),
                name = "::name::",
                private = true,
                startDate = LocalDateTime.of(2020, 1, 2, 3, 4, 5),
                type = "::type::"
            )
        )

        every { getAthlete(accessToken) } returns Id(apiAthlete)
        every { getAthleteActivities(accessToken) } returns Id(apiActivities)

        expect(getActivities(Id.monad(), getAthleteActivities, getAthlete, accessToken)).value.toBe(activities)
    }

    @Test
    fun `gear not found`() {
        val apiAthlete = ApiAthlete(
            bikes = emptyList(),
            shoes = emptyList()
        )
        val apiActivities = listOf(
            ApiActivity(
                id = 1,
                distance = 1.toBigDecimal(),
                gear_id = "::bikeId::",
                name = "",
                private = false,
                start_date_local = "2020-01-01T00:00:00Z",
                type = ""
            )
        )

        every { getAthlete(accessToken) } returns Id(apiAthlete)
        every { getAthleteActivities(accessToken) } returns Id(apiActivities)

        expect(getActivities(Id.monad(), getAthleteActivities, getAthlete, accessToken)).value.all {
            feature { f(it::gear) }.toBe(null)
        }
    }
}