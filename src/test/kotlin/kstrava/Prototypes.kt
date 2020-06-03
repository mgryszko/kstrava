package com.grysz.kstrava

import com.grysz.kstrava.token.AccessToken
import java.time.LocalDateTime

val anyAccessToken = AccessToken(":")

val anyActivity = Activity(
    id = 0,
    distance = Distance(meters = 1),
    gear = null,
    name = "",
    private = false,
    startDate = LocalDateTime.MIN,
    type = ""
)