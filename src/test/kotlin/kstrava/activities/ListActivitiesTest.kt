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
import com.grysz.kstrava.kstrava.activities.listActitivies
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class ListActivitiesTest {
    val ME = Either.monadError<ActivitiesError>()
    val readAccessToken: (AccessTokenFileName) -> Kind<EitherPartialOf<ActivitiesError>, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<EitherPartialOf<ActivitiesError>, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val activities = listOf(anyActivity)

    @Test
    fun execute() {
        ME.run {
            every { readAccessToken(AccessTokenFileName(accessTokenFileName)) } returns anyAccessToken.right()
            every { getActivities(anyAccessToken) } returns activities.right()

            expect(listActitivies(readAccessToken, getActivities, accessTokenFileName).fix()).right.toBe(activities)
        }
    }

    @Test
    fun `invalid access token file name`() {
        ME.run {
            expect(listActitivies(readAccessToken, getActivities, "").fix()).left.toBe(AccessTokenFileNameBlankError)
        }
    }
}
