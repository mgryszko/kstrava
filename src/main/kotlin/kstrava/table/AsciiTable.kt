package com.grysz.kstrava.table

import arrow.Kind
import arrow.core.ListK
import arrow.core.extensions.listk.applicative.applicative
import arrow.core.extensions.listk.foldable.foldable
import arrow.core.extensions.listk.monoidK.monoidK
import arrow.core.fix
import arrow.core.k
import arrow.typeclasses.Applicative
import arrow.typeclasses.Foldable
import arrow.typeclasses.MonoidK
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
        val columnsWithRenderers: List<Pair<MinWidthColumn, Show<A>>> = newColumns.zip(renderers)
        fitToRenderedWidths(
            FD = ListK.foldable(),
            MN = ListK.monoidK(),
            AP = ListK.applicative(),
            columnsWithRenderers = columnsWithRenderers.k(),
            value = value
        ).fix()
    }
    return copy(columns = newColumns, renderers = renderers)
}

private fun <F, A> fitToRenderedWidths(
    FD: Foldable<F>,
    MN: MonoidK<F>,
    AP: Applicative<F>,
    columnsWithRenderers: Kind<F, Pair<MinWidthColumn, Show<A>>>,
    value: A
): Kind<F, MinWidthColumn> = FD.run { AP.run {
    columnsWithRenderers.foldMap(MN.algebra()) { (column: MinWidthColumn, renderer: Show<A>) ->
        fitToRenderedWidth(column, renderer, value).just()
    }
} }

private fun <A> fitToRenderedWidth(
    column: MinWidthColumn,
    renderer: Show<A>,
    value: A
): MinWidthColumn = column.fitTo(with(renderer) { value.show().length })

private fun MinWidthColumn.fitTo(newWidth: Int): MinWidthColumn = if (newWidth > width) copy(width = newWidth) else this

