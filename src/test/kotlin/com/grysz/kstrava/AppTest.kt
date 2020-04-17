package com.grysz.kstrava

import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class AppTest {
    @Test
    fun testAppHasAGreeting() {
        assertThat(App().greeting.unsafeRunSync()).isEqualTo("Hello world")
    }
}
