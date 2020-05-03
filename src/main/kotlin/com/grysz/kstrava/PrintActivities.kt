package com.grysz.kstrava

private val idColumn: CellRenderer<Activity> = { it.id.toString() }
private val startDateColumn: CellRenderer<Activity> = Activity::start_date
private val typeColumn: CellRenderer<Activity> = Activity::type
private val nameColumn: CellRenderer<Activity> = Activity::name
private val distanceColumn: CellRenderer<Activity> = { it.distance.toString() }
private val gearIdColumn: CellRenderer<Activity> = { it.gear_id ?: "" }
private val privateColumn: CellRenderer<Activity> = { if (it.private) "+" else "" }
private val renderers = listOf(idColumn, startDateColumn, typeColumn, nameColumn, distanceColumn, gearIdColumn, privateColumn)

fun printActivitiesTable(activities: List<Activity>) {
    val columns = columnsFittingContent(renderers, activities)
    val table = Table(columns, renderers)
    table.render(activities)
}
