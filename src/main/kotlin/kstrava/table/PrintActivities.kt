package com.grysz.kstrava

import com.grysz.kstrava.table.Align.RIGHT
import com.grysz.kstrava.table.CellRenderer
import com.grysz.kstrava.table.Header
import com.grysz.kstrava.table.MinWidthColumn
import com.grysz.kstrava.table.Table
import com.grysz.kstrava.table.fitContent
import com.grysz.kstrava.table.render
import java.math.BigDecimal
import java.math.RoundingMode

private val idColumn: MinWidthColumn = MinWidthColumn(Header("id"))
private val startDateColumn: MinWidthColumn = MinWidthColumn(Header("start date"))
private val typeColumn: MinWidthColumn = MinWidthColumn(Header("type"))
private val nameColumn: MinWidthColumn = MinWidthColumn(Header("name"))
private val distanceColumn: MinWidthColumn = MinWidthColumn(Header("km"), align = RIGHT)
private val gearIdColumn: MinWidthColumn = MinWidthColumn(Header("gear id"))
private val privateColumn: MinWidthColumn = MinWidthColumn(Header("p"))
private val columns = listOf(idColumn, startDateColumn, typeColumn, nameColumn, distanceColumn, gearIdColumn, privateColumn)

private val idRenderer: CellRenderer<Activity> = { it.id.toString() }
private val startDateRenderer: CellRenderer<Activity> = { it.startDate.toString() }
private val typeRenderer: CellRenderer<Activity> = Activity::type
private val nameRenderer: CellRenderer<Activity> = Activity::name
private val distanceRenderer: CellRenderer<Activity> = { it.distance.toKm().toString() }
private val gearIdRenderer: CellRenderer<Activity> = { it.gear_id ?: "" }
private val privateRenderer: CellRenderer<Activity> = { if (it.private) "+" else "" }
private val renderers = listOf(idRenderer, startDateRenderer, typeRenderer, nameRenderer, distanceRenderer, gearIdRenderer, privateRenderer)
private val table = Table(columns, renderers)

fun Distance.toKm(): BigDecimal = meters.toBigDecimal().divide(1000.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)

fun printActivitiesTable(activities: List<Activity>) {
    table.fitContent(activities).render(activities, ::println)
}
