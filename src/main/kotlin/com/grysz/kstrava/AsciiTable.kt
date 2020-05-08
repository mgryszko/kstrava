package com.grysz.kstrava

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

    fun render(values: List<A>, renderRow: (String) -> Unit = ::println) {
        val format = columnStringFormat()
        renderRow(headerRow(format))
        renderRow(separatorRow())
        values.forEach { renderRow(valueRow(format, it)) }
    }

    private val columnSeparator = " "

    private fun columnStringFormat() = columns.joinToString(columnSeparator) { "%-${it.width}s" }

    private fun headerRow(formatSpec: String) = formatSpec.format(*columns.map { it.header.text }.toTypedArray())

    private fun separatorRow() = columns.joinToString(columnSeparator, transform = { "-".repeat(it.width) })

    private fun valueRow(formatSpec: String, value: A) = formatSpec.format(*renderers.map { it(value) }.toTypedArray())

    fun fitContent(values: List<A>): Table<A> {
        val newColumns = values.fold(columns) { newColumns, value ->
            newColumns.zip(renderers) .fold(emptyList()) { acc: List<MinWidthColumn>, (column: MinWidthColumn, renderer: CellRenderer<A>) ->
                    acc + column.fitTo(renderer(value).length)
                }
        }
        return copy(columns = newColumns, renderers = renderers)
    }
}

