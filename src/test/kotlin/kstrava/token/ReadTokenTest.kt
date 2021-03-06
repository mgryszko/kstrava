package com.grysz.kstrava.token

import ch.tutteli.atrium.api.fluent.en_GB.isA
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.left
import com.grysz.kstrava.right
import com.grysz.kstrava.runE
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
        fun read() {
            accessTokenFile.writeText("::token::")
            expect(readAccessToken(AccessTokenFileName(accessTokenFile.canonicalPath))).runE.right.toBe(AccessToken("::token::"))
        }
    }

    @Nested
    @DisplayName("missing access token")
    inner class MissingAccessToken {
        @Test
        fun read() {
            expect(readAccessToken(AccessTokenFileName("non-existing"))).runE.left.isA<ReadTokenError>()
        }
    }
}
