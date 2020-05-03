package com.grysz.kstrava

import ch.tutteli.atrium.api.fluent.en_GB.containsExactly
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class AsciiTableTest {
    @Test
    fun `adjust columns to fit content`() {
        val renderers = listOf<CellRenderer<String>>({ it }, { it + it })
        val values = listOf("", "a", "aa", "aaa")

        expect(columnsFittingContent(renderers, values)).toBe(listOf(MinWidthColumn(3), MinWidthColumn(6)))
    }

    @Test
    fun render() {
        val table = Table(
            columns = listOf(MinWidthColumn(3), MinWidthColumn(6)),
            renderers = listOf<(String) -> String>({ it }, { it + it })
        )
        val values = listOf("", "a", "aa", "aaa")

        val rows = mutableListOf<String>()
        table.render(values) { rows += it }

        expect(rows.toList()).containsExactly(
            "    |       ",
            "a   | aa    ",
            "aa  | aaaa  ",
            "aaa | aaaaaa"
        )
    }
}