package com.grysz.kstrava.token

import arrow.core.Either
import arrow.fx.IO
import com.grysz.kstrava.IOE
import com.grysz.kstrava.ListActivitiesError
import com.grysz.kstrava.TokenAccessError
import java.io.File

fun readAccessToken(tokenFileName: String): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(File(tokenFileName).readText()) })
}

fun readAccessTokenFN(fileName: AccessTokenFileName): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(fileName.toFile().readText()) })
}

private fun AccessTokenFileName.toFile() = File(name)