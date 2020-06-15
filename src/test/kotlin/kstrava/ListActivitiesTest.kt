package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.extensions.either.monadError.monadError
import arrow.core.fix
import arrow.core.right
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.token.AccessToken
import com.grysz.kstrava.token.AccessTokenFileName
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class ListActivitiesTest {
    val ME = Either.monadError<ListActivitiesError>()
    val readAccessToken: (AccessTokenFileName) -> Kind<EitherPartialOf<ListActivitiesError>, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<EitherPartialOf<ListActivitiesError>, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val accessToken = AccessToken("::token::")
    val activities = listOf(anyActivity)

    @Test
    fun execute() {
        ME.run {
            every { readAccessToken(AccessTokenFileName(accessTokenFileName)) } returns accessToken.right()
            every { getActivities(accessToken) } returns activities.right()

            expect(listActitivies(readAccessToken, getActivities, accessTokenFileName).fix()).right.toBe(activities)
        }
    }

    @Test
    fun `invalid access token file name`() {
        ME.run {
            every { readAccessToken(AccessTokenFileName(accessTokenFileName)) } returns accessToken.right()
            every { getActivities(accessToken) } returns activities.right()

            expect(listActitivies(readAccessToken, getActivities, "").fix()).left.toBe(AccessTokenFileNameBlankError)
        }
    }
}
