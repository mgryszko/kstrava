package kstrava.token

import arrow.core.Either
import arrow.fx.IO
import com.grysz.kstrava.AccessToken
import com.grysz.kstrava.IOE
import com.grysz.kstrava.ListActivitiesError
import com.grysz.kstrava.TokenAccessError
import java.io.File

fun readAccessToken(tokenFileName: String): IOE<ListActivitiesError, AccessToken> = IO {
    Either.catch({ _ -> TokenAccessError }, { AccessToken(File(tokenFileName).readText()) })
}