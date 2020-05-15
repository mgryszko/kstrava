package com.grysz.kstrava

import arrow.Kind
import arrow.core.ForId
import arrow.core.Id
import arrow.core.extensions.id.monad.monad
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import kotlin.test.AfterTest
import kotlin.test.Test

class ReadTokenTest {
    @Nested
    @DisplayName("existing access token")
    inner class ExistingAccessToken {
        val accessTokenFile = createTempFile().apply {
            delete()
        }

        @AfterTest
        fun afterTest() {
            accessTokenFile.delete()
        }

        @Test
        fun `read token`() {
            accessTokenFile.writeText("::token::")
            expect(readAccessToken(accessTokenFile.canonicalPath)).runE.right.toBe(AccessToken("::token::"))
        }
    }

    @Nested
    @DisplayName("missing access token")
    inner class MissingAccessToken {
        @Test
        fun `read token`() {
            expect(readAccessToken("non-existing")).runE.left.toBe(TokenAccessError)
        }
    }
}

class ListActivitiesWorkflowTest {
    val readAccessToken: (String) -> Kind<ForId, AccessToken> = mockk("readAccessToken")
    val getActivities: (AccessToken) -> Kind<ForId, List<Activity>> = mockk("getActivities")

    val accessTokenFileName = "::file::"
    val accessToken = AccessToken("::token::")
    val activity = Activity(0, Distance(1), "", "", false, "", "", "", "")
    val activities = listOf(activity)

    @Test
    fun list() {
        every { readAccessToken(accessTokenFileName) } returns Id(accessToken)
        every { getActivities(accessToken) } returns Id(activities)

        expect(listActitivies(Id.monad(), readAccessToken, getActivities, accessTokenFileName)).value.toBe(activities)
    }
}


