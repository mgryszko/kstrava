package com.grysz.kstrava

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import kstrava.token.readAccessToken
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