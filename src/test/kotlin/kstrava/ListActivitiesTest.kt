package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import kotlin.test.Test

class ListActivitiesWorkflowTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<ForId, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val accessToken = AccessToken("::token::")
    val activity = Activity(0, Distance(1), "", "", false, LocalDateTime.MIN, "")
    val activities = listOf(activity)

    @Test
    fun list() {
        every { readAccessToken(accessTokenFileName) } returns Id(accessToken)
        every { getActivities(accessToken) } returns Id(activities)

        expect(listActitivies(Id.monad(), readAccessToken, getActivities, accessTokenFileName)).value.toBe(activities)
    }
}

