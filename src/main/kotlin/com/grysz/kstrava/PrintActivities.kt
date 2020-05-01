package com.grysz.kstrava

private val idColumn = Column<Activity>({ it.id.toString() })
private val startDateColumn = Column(Activity::start_date)
private val typeColumn = Column(Activity::type)
private val nameColumn = Column(Activity::name)
private val distanceColumn = Column<Activity>({ it.distance.toString() })
private val gearIdColumn = Column<Activity>({ it.gear_id ?: "" })
private val privateColumn = Column<Activity>({ if (it.private) "+" else "" })
private val table = listOf(idColumn, startDateColumn, typeColumn, nameColumn, distanceColumn, gearIdColumn, privateColumn)

fun printActivitiesTable(activities: List<Activity>) {
    val columns = adjustColumnsToFitContent(table, activities)
    render(columns, activities)
}
