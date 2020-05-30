package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.typeclasses.Monad
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(IdMonadDependency::class)
class ListActivitiesTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<ForId, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val accessToken = AccessToken("::token::")
    val activities = listOf(anyActivity)

    @Test
    fun Monad<ForId>.execute() {
        every { readAccessToken(accessTokenFileName) } returns Id(accessToken)
        every { getActivities(accessToken) } returns Id(activities)

        expect(listActitivies(readAccessToken, getActivities, accessTokenFileName)).value.toBe(activities)
    }
}
