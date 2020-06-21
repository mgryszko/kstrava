package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.extensions.either.monadError.monadError
import arrow.core.fix
import arrow.core.right
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.activities.AccessTokenFileNameBlankError
import com.grysz.kstrava.activities.ActivitiesError
import com.grysz.kstrava.activities.Activity
import com.grysz.kstrava.activities.ActivityId
import com.grysz.kstrava.activities.ActivityName
import com.grysz.kstrava.activities.EmptyActivityIdsError
import com.grysz.kstrava.kstrava.activities.updateActitivies
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class UpdateActivitiesTest {
    val ME = Either.monadError<ActivitiesError>()
    val readAccessToken: (AccessTokenFileName) -> Kind<EitherPartialOf<ActivitiesError>, AccessToken> = mockk("readAccessToken")
    val updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<EitherPartialOf<ActivitiesError>, List<Activity>> =
        mockk("updateActivities")

    val accessTokenFileName = "::file::"
    val activityIds = listOf(ActivityId(1), ActivityId(2))
    val activities = listOf(anyActivity.copy(id = 1), anyActivity.copy(id = 2))
    val activityName = ActivityName(":")

    @Test
    fun execute() {
        ME.run {
            every { readAccessToken(AccessTokenFileName(accessTokenFileName)) } returns anyAccessToken.right()
            every { updateActivities(anyAccessToken, activityIds, activityName) } returns activities.right()

            expect(updateActitivies(readAccessToken, updateActivities, accessTokenFileName, activityIds, activityName).fix())
                .right.toBe(activities)
        }
    }
    
    @Test
    fun `empty activity ids`() {
        ME.run {
            expect(updateActitivies(readAccessToken, updateActivities, accessTokenFileName, emptyList(), activityName).fix())
                .left.toBe(EmptyActivityIdsError)
        }
    }

    @Test
    fun `invalid access token file name`() {
        ME.run {
            expect(updateActitivies(readAccessToken, updateActivities, "", activityIds, activityName).fix())
                .left.toBe(AccessTokenFileNameBlankError)
        }
    }
}
