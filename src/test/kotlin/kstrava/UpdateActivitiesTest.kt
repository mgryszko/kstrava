package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.typeclasses.Monad
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.token.AccessToken
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(IdMonadDependency::class)
class UpdateActivitiesTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val updateActivity: (AccessToken, ActivityId, ActivityName) -> Kind<ForId, Activity> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val activityId = ActivityId(0)
    val activityName = ActivityName(":")

    @Test
    fun Monad<ForId>.execute() {
        every { readAccessToken(accessTokenFileName) } returns Id(anyAccessToken)
        every { updateActivity(anyAccessToken, activityId, activityName) } returns Id(anyActivity)

        expect(updateActitivies(readAccessToken, updateActivity, accessTokenFileName, activityId, activityName)).value.toBe(anyActivity)
    }
}
