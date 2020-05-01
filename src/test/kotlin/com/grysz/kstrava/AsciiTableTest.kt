package com.grysz.kstrava

import ch.tutteli.atrium.api.fluent.en_GB.feature
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class AsciiTableTest {
    @Test
    fun `adjust columns to fit content`() {
        val table = listOf(Column({ it }), Column<String>({ it + it }))
        val values = listOf("", "a", "aa", "aaa")

        expect(adjustColumnsToFitContent(table, values)).feature("widths") { map(Column<String>::width) }.toBe(listOf(3, 6))
    }
}