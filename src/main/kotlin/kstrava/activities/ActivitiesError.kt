package com.grysz.kstrava.activities

sealed class ListActivitiesError

object AccessTokenFileNameBlankError : ListActivitiesError()

data class TokenAccessError(val exception: Throwable) : ListActivitiesError()

data class StravaError(val exception: Throwable) : ListActivitiesError()
