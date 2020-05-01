package com.grysz.kstrava

fun <A> adjustColumnsToFitContent(
    initial: List<Column>,
    values: List<A>,
    renderers: List<CellRenderer<A>>
): List<Column> = values.fold(initial) { columns, value ->
    columns.zip(renderers).fold(emptyList()) { acc: List<Column>, (column: Column, renderer: CellRenderer<A>) ->
        acc + if (renderer.width(value) > column.width) column.copy(width = renderer.width(value)) else column
    }
}

fun <A> render(columns: List<Column>, renderers: List<CellRenderer<A>>, values: List<A>) {
    val formatSpec = columns.joinToString(" | ", transform = Column::format)
    values.forEach { activity ->
        val activityRow = formatSpec.format(*renderers.map { it.render(activity) }.toTypedArray())
        println(activityRow)
    }
}

data class Column(val width: Int) {
    init {
        require(width > 0) { "Width must be positive" }
    }

    fun format(): String = "%-${width}s"
}

class CellRenderer<in A>(val value: (A) -> String) {
    fun width(a: A): Int = value(a).length

    fun render(a: A): String = value(a)
}

