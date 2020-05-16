package com.grysz.kstrava.table

import arrow.typeclasses.Show
import ch.tutteli.atrium.api.fluent.en_GB.containsExactly
import ch.tutteli.atrium.api.verbs.expect
import com.grysz.kstrava.table.Align.RIGHT
import org.junit.jupiter.api.Test

class RendererTest {
    @Test
    fun render() {
        val table = Table(
            columns = listOf(
                MinWidthColumn(header = Header("first"), width = 5),
                MinWidthColumn(header = Header("second"), width = 6, align = RIGHT)
            ),
            renderers = listOf<Show<String>>(Show { this }, Show { this + this })
        )
        val values = listOf("", "a", "aa", "aaa")

        val rows = mutableListOf<String>()
        table.render(values) { rows += it }

        expect(rows).containsExactly(
            "first second",
            "----- ------",
            "            ",
            "a         aa",
            "aa      aaaa",
            "aaa   aaaaaa"
        )
    }
}