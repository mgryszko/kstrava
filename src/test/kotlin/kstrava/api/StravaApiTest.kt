package com.grysz.kstrava

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import kstrava.api.ApiActivity
import kstrava.api.getActivities
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class GetAthleteActivitiesTest {
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
