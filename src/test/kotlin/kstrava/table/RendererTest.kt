package com.grysz.kstrava.table

import ch.tutteli.atrium.api.fluent.en_GB.containsExactly
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class RendererTest {
    @Test
    fun render() {
        val table = Table(
            columns = listOf(MinWidthColumn(header = Header("first"), width = 5), MinWidthColumn(header = Header("second"), width = 6)),
            renderers = listOf<CellRenderer<String>>({ it }, { it + it })
        )
        val values = listOf("", "a", "aa", "aaa")

        val rows = mutableListOf<String>()
        table.render(values) { rows += it }

        expect(rows.toList()).containsExactly(
            "first second",
            "----- ------",
            "            ",
            "a     aa    ",
            "aa    aaaa  ",
            "aaa   aaaaaa"
        )
    }
}