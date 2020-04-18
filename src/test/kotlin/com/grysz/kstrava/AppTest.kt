package com.grysz.kstrava

import org.assertj.core.api.Assertions.assertThat
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test

class AppTest {
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

        assertThat(readToken(accessTokenFile.canonicalPath).unsafeRunSync()).isEqualTo("::token::")
    }
}
