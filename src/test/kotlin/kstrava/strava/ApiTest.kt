package com.grysz.kstrava.strava

import arrow.core.ForId
import arrow.core.Id
import arrow.typeclasses.Applicative
import ch.tutteli.atrium.api.fluent.en_GB.all
import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.grysz.kstrava.Activity
import com.grysz.kstrava.ActivityId
import com.grysz.kstrava.ActivityName
import com.grysz.kstrava.Distance
import com.grysz.kstrava.Gear
import com.grysz.kstrava.IdApplicativeDependency
import com.grysz.kstrava.StravaApiError
import com.grysz.kstrava.left
import com.grysz.kstrava.right
import com.grysz.kstrava.runE
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.value
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.Test

private val accessToken = AccessToken("::token::")

@TestInstance(Lifecycle.PER_CLASS)
class ApiTest {
    val wm: WireMockServer = WireMockServer(options().dynamicPort().notifier(ConsoleNotifier(false)))

    @BeforeAll
    fun setUp() {
        wm.start()
    }

    @AfterAll
    fun tearDown() {
        wm.stop()
    }

    private fun baseUrl() = "http://localhost:${wm.port()}"

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

            expect(getAthleteActivities(accessToken, baseUrl())).runE.right.toBe(
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
            wm.stubFor(get(anyUrl()).willReturn(status(401)))

            expect(getAthleteActivities(accessToken, baseUrl())).runE.left.isA<StravaApiError>()
        }
    }

    @Nested
    @DisplayName("get activities")
    inner class UpdateActivity {
        @Test
        fun ok() {
            wm.stubFor(
                put(urlMatching("/api/v3/activities/123"))
                    .withHeader("Authorization", equalTo("Bearer ${accessToken.token}"))
                    .withRequestBody(
                        equalToJson(
                            """{
    "name": "::updated name::"
}"""
                        )
                    )
                    .willReturn(
                        okJson(
                            """{
    "id": 1,
    "distance": 101.22,
    "gear_id": "::gearId::",
    "name": "::updated name::",
    "private": true,
    "start_date_local": "2020-01-02T03:04:05Z",
    "type": "::type::"
}"""
                        )
                    )
            )

            expect(
                updateAthleteActivity(
                    accessToken = accessToken,
                    id = 123,
                    activity = UpdatableApiActivity(name = "::updated name::"),
                    baseUrl = baseUrl()
                )
            ).runE.right.toBe(
                ApiActivity(
                    id = 1,
                    distance = 101.22.toBigDecimal(),
                    gear_id = "::gearId::",
                    name = "::updated name::",
                    private = true,
                    start_date_local = "2020-01-02T03:04:05Z",
                    type = "::type::"
                )
            )
        }

        @Test
        fun unauthorized() {
            wm.stubFor(put(anyUrl()).willReturn(status(401)))

            expect(
                updateAthleteActivity(
                    accessToken = accessToken,
                    id = 0,
                    activity = UpdatableApiActivity(""),
                    baseUrl = baseUrl()
                )
            ).runE.left.isA<StravaApiError>()
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

            expect(getAthlete(accessToken, baseUrl())).runE.right.toBe(
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

        @Test
        fun unauthorized() {
            wm.stubFor(get(anyUrl()).willReturn(status(401)))

            expect(getAthlete(accessToken, baseUrl())).runE.left.isA<StravaApiError>()
        }
    }
}

@ExtendWith(IdApplicativeDependency::class)
class GetActivitiesTest {
    val getAthlete: (AccessToken) -> Id<ApiAthlete> = mockk()
    val getAthleteActivities: (AccessToken) -> Id<List<ApiActivity>> = mockk()

    @Test
    fun Applicative<ForId>.`get activities`() {
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

        expect(getActivities(getAthleteActivities, getAthlete, accessToken)).value.toBe(activities)
    }

    @Test
    fun Applicative<ForId>.`gear not found`() {
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

        expect(getActivities(getAthleteActivities, getAthlete, accessToken)).value.all {
            feature { f(it::gear) }.toBe(null)
        }
    }
}

@ExtendWith(IdApplicativeDependency::class)
class UpdateActivitiesTest {
    val getAthlete: (AccessToken) -> Id<ApiAthlete> = mockk()
    val updateAthleteActivity: (AccessToken, Long, UpdatableApiActivity) -> Id<ApiActivity> = mockk()

    @Test
    fun Applicative<ForId>.`update activities`() {
        val apiAthlete = ApiAthlete(
            bikes = listOf(
                ApiGear(id = "::bikeId1::", name = "::bikeName1::"),
                ApiGear(id = "::bikeId2::", name = "::bikeName2::")
            ),
            shoes = listOf(
                ApiGear(id = "::shoeId::", name = "::shoeName::")
            )
        )
        val apiActivity1 = ApiActivity(
            id = 1,
            distance = 123.99.toBigDecimal(),
            gear_id = "::bikeId1::",
            name = "::updated name::",
            private = true,
            start_date_local = "2020-01-02T03:04:05Z",
            type = "::type::"
        )
        val apiActivity2 = ApiActivity(
            id = 2,
            distance = 124.0.toBigDecimal(),
            gear_id = "::bikeId2::",
            name = "::updated name::",
            private = false,
            start_date_local = "2020-01-02T03:04:05Z",
            type = "::type::"
        )
        val activity1 = Activity(
            id = 1,
            distance = Distance(123),
            gear = Gear("::bikeId1::", "::bikeName1::"),
            name = "::updated name::",
            private = true,
            startDate = LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            type = "::type::"
        )
        val activity2 = Activity(
            id = 2,
            distance = Distance(124),
            gear = Gear("::bikeId2::", "::bikeName2::"),
            name = "::updated name::",
            private = false,
            startDate = LocalDateTime.of(2020, 1, 2, 3, 4, 5),
            type = "::type::"
        )
        every { getAthlete(accessToken) } returns Id(apiAthlete)
        every { updateAthleteActivity(accessToken, 1, UpdatableApiActivity(name = "::updated name::")) } returns Id(apiActivity1)
        every { updateAthleteActivity(accessToken, 2, UpdatableApiActivity(name = "::updated name::")) } returns Id(apiActivity2)

        expect(
            updateActivities(
                updateAthleteActivity,
                getAthlete,
                accessToken,
                listOf(ActivityId(1), ActivityId(2)),
                ActivityName("::updated name::")
            )
        ).value.toBe(listOf(activity1, activity2))
    }

    @Test
    fun Applicative<ForId>.`gear not found`() {
        val apiAthlete = ApiAthlete(
            bikes = emptyList(),
            shoes = emptyList()
        )
        val apiActivity = ApiActivity(
            id = 1,
            distance = 1.toBigDecimal(),
            gear_id = "::bikeId::",
            name = "",
            private = false,
            start_date_local = "2020-01-01T00:00:00Z",
            type = ""
        )

        every { getAthlete(accessToken) } returns Id(apiAthlete)
        every { updateAthleteActivity(accessToken, 1, UpdatableApiActivity(name = "::updated name::")) } returns Id(apiActivity)

        expect(
            updateActivities(
                updateAthleteActivity,
                getAthlete,
                accessToken,
                listOf(ActivityId(1)),
                ActivityName("::updated name::")
            )
        ).value.all { feature { f(it::gear) }.toBe(null) }
    }
}

