package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.typeclasses.Monad
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.token.AccessToken
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(IdMonadDependency::class)
class UpdateActivitiesTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<ForId, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val activityIds = listOf(ActivityId(1), ActivityId(2))
    val activities = listOf(anyActivity.copy(id = 1), anyActivity.copy(id = 2))
    val activityName = ActivityName(":")

    @Test
    fun Monad<ForId>.execute() {
        every { readAccessToken(accessTokenFileName) } returns Id(anyAccessToken)
        every { updateActivities(anyAccessToken, activityIds, activityName) } returns Id(activities)

        expect(updateActitivies(readAccessToken, updateActivities, accessTokenFileName, activityIds, activityName)).value.toBe(activities)
    }
}
