package kstrava

import com.grysz.kstrava.Activity
import com.grysz.kstrava.Distance
import java.time.LocalDateTime

val anyActivity = Activity(
    id = 0,
    distance = Distance(meters = 1),
    gear = null,
    name = "",
    private = false,
    startDate = LocalDateTime.MIN,
    type = ""
)