package com.grysz.kstrava

import arrow.Kind
import arrow.core.Either
import arrow.core.EitherPartialOf
import arrow.core.Nel
import arrow.core.Tuple2
import arrow.core.Validated
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.either.monadError.monadError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.core.extensions.validated.bifunctor.mapLeft
import arrow.core.fix
import arrow.core.nel
import arrow.core.right
import arrow.typeclasses.ApplicativeError
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
    val ME = Either.monadError<Nel<ActivitiesError>>()
    val readAccessToken: (AccessTokenFileName) -> Kind<EitherPartialOf<Nel<ActivitiesError>>, AccessToken> = mockk("readAccessToken")
    val updateActivities: (AccessToken, List<ActivityId>, ActivityName) -> Kind<EitherPartialOf<Nel<ActivitiesError>>, List<Activity>> =
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
    fun `invalid arguments`() {
        ME.run {
            expect(updateActitivies(readAccessToken, updateActivities, "", emptyList(), activityName).fix())
                .left.toBe(Nel(EmptyActivityIdsError, AccessTokenFileNameBlankError))
        }
    }
}

class AccumulatingVsShortcircuitingValidationTest {
    val AE = Validated.applicativeError(Nel.semigroup<ActivitiesError>())
    val ME = Either.applicativeError<Nel<ActivitiesError>>()

    @Test
    fun `accumulate errors`() {
        AE.run {
            expect(validate(emptyList(), "").fix()).invalid.toBe(Nel(EmptyActivityIdsError, AccessTokenFileNameBlankError))
        }
    }

    @Test
    fun `short-circuit validation`() {
        ME.run {
            expect(validate(emptyList(), "").fix()).left.toBe(EmptyActivityIdsError.nel())
        }
    }

    private fun <F> ApplicativeError<F, Nel<ActivitiesError>>.validate(
        activityIds: List<ActivityId>,
        accessTokenFileName: String
    ): Kind<F, Tuple2<List<ActivityId>, AccessTokenFileName>> {
        val validActivityIds = if (activityIds.isEmpty()) raiseError(EmptyActivityIdsError.nel()) else activityIds.just()
        val validFileName = AccessTokenFileName.create(accessTokenFileName)
            .mapLeft { AccessTokenFileNameBlankError }
            .fold({ raiseError<AccessTokenFileName>(it.nel()) }, this::just)
        return validActivityIds.product(validFileName)
    }
}
