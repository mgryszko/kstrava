package com.grysz.kstrava

import com.grysz.kstrava.table.CellRenderer
import com.grysz.kstrava.table.Header
import com.grysz.kstrava.table.MinWidthColumn
import com.grysz.kstrava.table.Table
import com.grysz.kstrava.table.render

private val idColumn: MinWidthColumn = MinWidthColumn(Header("id"))
private val startDateColumn: MinWidthColumn = MinWidthColumn(Header("start date"))
private val typeColumn: MinWidthColumn = MinWidthColumn(Header("type"))
private val nameColumn: MinWidthColumn = MinWidthColumn(Header("name"))
private val distanceColumn: MinWidthColumn = MinWidthColumn(Header("m"))
private val gearIdColumn: MinWidthColumn = MinWidthColumn(Header("gear id"))
private val privateColumn: MinWidthColumn = MinWidthColumn(Header("p"))
private val columns = listOf(idColumn, startDateColumn, typeColumn, nameColumn, distanceColumn, gearIdColumn, privateColumn)

private val idRenderer: CellRenderer<Activity> = { it.id.toString() }
private val startDateRenderer: CellRenderer<Activity> = Activity::start_date
private val typeRenderer: CellRenderer<Activity> = Activity::type
private val nameRenderer: CellRenderer<Activity> = Activity::name
private val distanceRenderer: CellRenderer<Activity> = { it.distance.toString() }
private val gearIdRenderer: CellRenderer<Activity> = { it.gear_id ?: "" }
private val privateRenderer: CellRenderer<Activity> = { if (it.private) "+" else "" }
private val renderers = listOf(idRenderer, startDateRenderer, typeRenderer, nameRenderer, distanceRenderer, gearIdRenderer, privateRenderer)
private val table = Table(columns, renderers)

fun printActivitiesTable(activities: List<Activity>) {
    table.fitContent(activities).render(activities)
}
