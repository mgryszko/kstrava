package com.grysz.kstrava.table

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

typealias CellRenderer<A> = (A) -> String

data class Table<in A>(val columns: List<MinWidthColumn>, val renderers: List<CellRenderer<A>>) {
    init {
        require(columns.isNotEmpty()) { "Table must have at least one column" }
        require(columns.size == renderers.size) { "Each column must have a corresponding renderer" }
    }
}

fun <A> Table<A>.fitContent(values: List<A>): Table<A> {
    val newColumns = values.fold(columns) { newColumns, value ->
        newColumns.zip(renderers).fold(emptyList()) { acc: List<MinWidthColumn>, (column: MinWidthColumn, renderer: CellRenderer<A>) ->
            acc + column.fitTo(renderer(value).length)
        }
    }
    return copy(columns = newColumns, renderers = renderers)
}

private fun MinWidthColumn.fitTo(newWidth: Int): MinWidthColumn = if (newWidth > width) copy(width = newWidth) else this
