package com.grysz.kstrava.token

import arrow.core.Either
import arrow.fx.IO
import com.grysz.kstrava.IOE
import java.io.File

data class ReadTokenError(val exception: Throwable)

fun readAccessToken(fileName: AccessTokenFileName): IOE<ReadTokenError, AccessToken> = IO {
    Either.catch(::ReadTokenError) { AccessToken(fileName.toFile().readText()) }
}

private fun AccessTokenFileName.toFile() = File(name)