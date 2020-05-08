package com.grysz.kstrava.table

data class Header(val text: String) {
    fun width(): Int = text.length
}

data class MinWidthColumn(val header: Header, val width: Int) {
    constructor(header: Header) : this(header, width = header.width())

    init {
        require(width > 0) { "Width must be positive" }
    }

    fun fitTo(newWidth: Int): MinWidthColumn = if (newWidth > width) copy(width = newWidth) else this
}

typealias CellRenderer<A> = (A) -> String

data class Table<in A>(val columns: List<MinWidthColumn>, val renderers: List<CellRenderer<A>>) {
    init {
        require(columns.isNotEmpty()) { "Table must have at least one column" }
        require(columns.size == renderers.size) { "Each column must have a corresponding renderer" }
    }

    fun fitContent(values: List<A>): Table<A> {
        val newColumns = values.fold(columns) { newColumns, value ->
            newColumns.zip(renderers) .fold(emptyList()) { acc: List<MinWidthColumn>, (column: MinWidthColumn, renderer: CellRenderer<A>) ->
                    acc + column.fitTo(renderer(value).length)
                }
        }
        return copy(columns = newColumns, renderers = renderers)
    }
}

