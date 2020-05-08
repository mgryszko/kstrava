package com.grysz.kstrava.table

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class AsciiTableTest {
    @Test
    fun `adjust table to fit content`() {
        val table = Table(
            columns = listOf(MinWidthColumn(header = Header("bb")), MinWidthColumn(header = Header("bbbbbbbb"))),
            renderers = listOf<CellRenderer<String>>({ it }, { it + it })
        )
        val values = listOf("", "a", "aa", "aaa")

        expect(table.fitContent(values)).toBe(
            Table(
                columns = listOf(MinWidthColumn(header = Header("bb"), width = 3), MinWidthColumn(header = Header("bbbbbbbb"), width = 8)),
                renderers = table.renderers
            )
        )
    }
}