package com.grysz.kstrava

fun <A> adjustColumnsToFitContent(
    initial: List<Column<A>>,
    values: List<A>
): List<Column<A>> = values.fold(initial) { columns, value ->
    columns.fold(emptyList()) { acc: List<Column<A>>, column: Column<A> ->
        acc + if (column.width(value) > column.width) column.copy(width = column.width(value)) else column
    }
}

fun <A> render(table: List<Column<A>>, values: List<A>) {
    val formatSpec = table.joinToString(" | ", transform = Column<A>::format)
    values.forEach { value ->
        val row = formatSpec.format(*table.map { it.render(value) }.toTypedArray())
        println(row)
    }
}

data class Column<in A>(val cellRenderer: (A) -> String, val width: Int = 1) {
    init {
        require(width > 0) { "Width must be positive" }
    }

    fun format(): String = "%-${width}s"

    fun width(a: A): Int = cellRenderer(a).length

    fun render(a: A): String = cellRenderer(a)
}
