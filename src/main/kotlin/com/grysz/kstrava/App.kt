package com.grysz.kstrava

import arrow.core.Either
import arrow.fx.IO
import arrow.fx.extensions.fx
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

typealias IOE<A, B> = IO<Either<A, B>>

fun readToken(tokenFileName: String): IOE<TokenAccessError, String> = IO {
    Either.catch({ _ -> TokenAccessError }, { File(tokenFileName).readText() })
}

object TokenAccessError

fun app(accessTokenFileName: String): IO<Unit> =
    IO.fx {
        val tokenOrError = !readToken(accessTokenFileName)
        !tokenOrError.fold({ e -> IO { println("error: $e") } }, { token -> IO { println("token: $token") } })
    }

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    val accessTokenFileName: String by option(help="File name containing access token").default(".access-token")

    override fun run() {
        app(accessTokenFileName).unsafeRunSync()
    }
}.main(args)
