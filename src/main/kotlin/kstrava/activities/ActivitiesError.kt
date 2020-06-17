package com.grysz.kstrava.activities

sealed class ActivitiesError

object AccessTokenFileNameBlankError : ActivitiesError()

data class TokenAccessError(val exception: Throwable) : ActivitiesError()

data class StravaError(val exception: Throwable) : ActivitiesError()
