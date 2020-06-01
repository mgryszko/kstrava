package com.grysz.kstrava.strava

import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.jackson.defaultMapper

fun Request.objectBody(bodyObject: Any): Request {
    val body = defaultMapper.writeValueAsBytes(bodyObject)
    this[Headers.CONTENT_TYPE] = "application/json"
    return body(body)
}