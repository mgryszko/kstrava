package com.grysz.kstrava.table

import arrow.typeclasses.Show
import com.grysz.kstrava.table.Align.LEFT

data class Header(val text: String) {
    fun width(): Int = text.length
}

enum class Align {
    LEFT, RIGHT
}

data class MinWidthColumn(val header: Header, val width: Int, val align: Align = LEFT) {
    constructor(header: Header, align: Align = LEFT) : this(header = header, width = header.width(), align = align)

    init {
        require(width > 0) { "Width must be positive" }
    }
}

data class Table<in A>(val columns: List<MinWidthColumn>, val renderers: List<Show<A>>) {
    init {
        require(columns.isNotEmpty()) { "Table must have at least one column" }
        require(columns.size == renderers.size) { "Each column must have a corresponding renderer" }
    }
}

fun <A> Table<A>.fitContent(values: List<A>): Table<A> {
    val newColumns = values.fold(columns) { newColumns, value ->
        newColumns.zip(renderers).fold(emptyList()) { acc: List<MinWidthColumn>, (column: MinWidthColumn, renderer: Show<A>) ->
            acc + column.fitTo(renderer.run { value.show().length })
        }
    }
    return copy(columns = newColumns, renderers = renderers)
}

private fun MinWidthColumn.fitTo(newWidth: Int): MinWidthColumn = if (newWidth > width) copy(width = newWidth) else this
