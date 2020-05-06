package com.grysz.kstrava

import ch.tutteli.atrium.api.fluent.en_GB.containsExactly
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class AsciiTableTest {
    @Test
    fun `adjust table to fit content`() {
        val table = Table(
            columns = listOf(MinWidthColumn(header = Header("bb")), MinWidthColumn(header = Header("bbbbbbbb"))),
            renderers = listOf<(String) -> String>({ it }, { it + it })
        )
        val values = listOf("", "a", "aa", "aaa")

        expect(table.fitContent(values)).toBe(
            Table(
                columns = listOf(MinWidthColumn(header = Header("bb"), width = 3), MinWidthColumn(header = Header("bbbbbbbb"), width = 8)),
                renderers = table.renderers
            )
        )
    }

    @Test
    fun render() {
        val table = Table(
            columns = listOf(MinWidthColumn(header = Header("first"), width = 5), MinWidthColumn(header = Header("second"), width = 6)),
            renderers = listOf<(String) -> String>({ it }, { it + it })
        )
        val values = listOf("", "a", "aa", "aaa")

        val rows = mutableListOf<String>()
        table.render(values) { rows += it }

        expect(rows.toList()).containsExactly(
            "first | second",
            "----- | ------",
            "      |       ",
            "a     | aa    ",
            "aa    | aaaa  ",
            "aaa   | aaaaaa"
        )
    }
}