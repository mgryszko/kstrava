package com.grysz.kstrava

fun printActivitiesTable(activities: List<Activity>) {
    val idRenderer = CellRenderer<Activity> { it.id.toString() }
    val startDateRenderer = CellRenderer(Activity::start_date)
    val typeRenderer = CellRenderer(Activity::type)
    val nameRenderer = CellRenderer(Activity::name)
    val distanceRenderer = CellRenderer<Activity> { it.distance.toString() }
    val gearIdRenderer = CellRenderer<Activity> { it.gear_id ?: "" }
    val privateRenderer = CellRenderer<Activity> { if (it.private) "+" else "" }

    val renderers = listOf(idRenderer, startDateRenderer, typeRenderer, nameRenderer, distanceRenderer, gearIdRenderer, privateRenderer)

    val columns = adjustColumnsToFitContent(List(renderers.size) { Column(1) }, activities, renderers)

    val formatSpec = columns.joinToString(" | ", transform = Column::format)
    activities.forEach { activity ->
        val activityRow = formatSpec.format(*renderers.map { it.render(activity) }.toTypedArray())
        println(activityRow)
    }
}

private fun <A> adjustColumnsToFitContent(
    initial: List<Column>,
    values: List<A>,
    renderers: List<CellRenderer<A>>
): List<Column> = values.fold(initial) { columns, value ->
    columns.zip(renderers).fold(emptyList()) { acc: List<Column>, (column: Column, renderer: CellRenderer<A>) ->
        acc + if (renderer.width(value) > column.width) column.copy(width = renderer.width(value)) else column
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

