package com.grysz.kstrava

import arrow.typeclasses.Show
import com.grysz.kstrava.table.Align.RIGHT
import com.grysz.kstrava.table.Header
import com.grysz.kstrava.table.MinWidthColumn
import com.grysz.kstrava.table.Table
import com.grysz.kstrava.table.fitContent
import com.grysz.kstrava.table.render
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

private val idColumn: MinWidthColumn = MinWidthColumn(Header("id"))
private val startDateColumn: MinWidthColumn = MinWidthColumn(Header("start date"))
private val typeColumn: MinWidthColumn = MinWidthColumn(Header("type"))
private val nameColumn: MinWidthColumn = MinWidthColumn(Header("name"))
private val distanceColumn: MinWidthColumn = MinWidthColumn(Header("km"), align = RIGHT)
private val gearIdColumn: MinWidthColumn = MinWidthColumn(Header("gear id"))
private val privateColumn: MinWidthColumn = MinWidthColumn(Header("p"))
private val columns = listOf(idColumn, startDateColumn, typeColumn, nameColumn, distanceColumn, gearIdColumn, privateColumn)

private val idRenderer: Show<Activity> = Show { id.toString() }
private val startDateFormatter = DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .appendValue(ChronoField.HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
    .toFormatter()
val startDateRenderer: Show<Activity> = Show { startDate.format(startDateFormatter).toString() }
private val typeRenderer: Show<Activity> = Show { type }
private val nameRenderer: Show<Activity> = Show { name }
val distanceRenderer: Show<Activity> = Show { distance.toKm().toString() }
private val gearIdRenderer: Show<Activity> = Show { gearId ?: "" }
private val privateRenderer: Show<Activity> = Show { if (private) "+" else "" }
private val renderers = listOf(idRenderer, startDateRenderer, typeRenderer, nameRenderer, distanceRenderer, gearIdRenderer, privateRenderer)
private val table = Table(columns, renderers)

private fun Distance.toKm(): BigDecimal = meters.toBigDecimal().divide(1000.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)

fun printActivitiesTable(activities: List<Activity>) {
    table.fitContent(activities).render(activities, ::println)
}
