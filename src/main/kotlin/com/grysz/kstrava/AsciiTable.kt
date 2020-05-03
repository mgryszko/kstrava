package com.grysz.kstrava

data class MinWidthColumn(val width: Int) {
    init {
        require(width > 0) { "Width must be positive" }
    }

    fun fitTo(newWidth: Int): MinWidthColumn = if (newWidth > width) copy(width = newWidth) else this

    fun format(): String = "%-${width}s"
}

typealias CellRenderer<A> = (A) -> String

data class Table<in A>(val columns: List<MinWidthColumn>, val renderers: List<CellRenderer<A>>) {
    init {
        require(columns.isNotEmpty()) { "Table must have at least one column" }
        require(columns.size == renderers.size) { "Each column must have a corresponding renderer" }
    }

    fun render(values: List<A>, forEachRow: (String) -> Unit = ::println) {
        val formatSpec = columns.joinToString(" | ", transform = MinWidthColumn::format)
        values.forEach { value ->
            val row = formatSpec.format(*renderers.map { it(value) }.toTypedArray())
            forEachRow(row)
        }
    }
}

fun <A> columnsFittingContent(renderers: List<CellRenderer<A>>, values: List<A>): List<MinWidthColumn> {
    val initial = renderers.map { MinWidthColumn(1) }
    return values.fold(initial) { columns, value ->
        columns.zip(renderers).fold(emptyList()) { acc: List<MinWidthColumn>, (column: MinWidthColumn, renderer: CellRenderer<A>) ->
            acc + column.fitTo(renderer(value).length)
        }
    }
}
