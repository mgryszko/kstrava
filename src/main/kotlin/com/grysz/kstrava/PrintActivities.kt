package com.grysz.kstrava

private val idRenderer = CellRenderer<Activity> { it.id.toString() }
private val startDateRenderer = CellRenderer(Activity::start_date)
private val typeRenderer = CellRenderer(Activity::type)
private val nameRenderer = CellRenderer(Activity::name)
private val distanceRenderer = CellRenderer<Activity> { it.distance.toString() }
private val gearIdRenderer = CellRenderer<Activity> { it.gear_id ?: "" }
private val privateRenderer = CellRenderer<Activity> { if (it.private) "+" else "" }
private val renderers = listOf(idRenderer, startDateRenderer, typeRenderer, nameRenderer, distanceRenderer, gearIdRenderer, privateRenderer)

fun printActivitiesTable(activities: List<Activity>) {
    val columns = adjustColumnsToFitContent(List(renderers.size) { Column(1) }, activities, renderers)
    render(columns, renderers, activities)
}
