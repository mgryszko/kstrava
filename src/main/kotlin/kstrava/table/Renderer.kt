package com.grysz.kstrava.table

fun <A> Table<A>.render(values: List<A>, renderRow: (String) -> Unit) {
    val format = columnStringFormat()
    renderRow(headerRow(format))
    renderRow(separatorRow())
    values.forEach { renderRow(valueRow(format, it)) }
}

private const val COLUMN_SEPARATOR = " "

private fun <A> Table<A>.columnStringFormat() = columns.joinToString(COLUMN_SEPARATOR) { "%${alignFlag(it.align)}${it.width}s" }

private fun alignFlag(align: Align) = if (align == Align.LEFT) "-" else ""

private fun <A> Table<A>.headerRow(formatSpec: String) = formatSpec.format(*columns.map { it.header.text }.toTypedArray())

private fun <A> Table<A>.separatorRow() = columns.joinToString(COLUMN_SEPARATOR, transform = { "-".repeat(it.width) })

private fun <A> Table<A>.valueRow(formatSpec: String, value: A) =
    formatSpec.format(*cells(value).toTypedArray())

private fun <A> Table<A>.cells(value: A) = renderers.map { it.run { value.show() } }
