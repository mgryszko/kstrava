package kstrava

import com.grysz.kstrava.Activity
import com.grysz.kstrava.Distance
import java.time.LocalDateTime

val activity = Activity(
    id = 0,
    distance = Distance(meters = 1),
    gear_id = null,
    name = "",
    private = false,
    startDate = LocalDateTime.MIN,
    type = ""
)