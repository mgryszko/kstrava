package com.grysz.kstrava

import arrow.core.Either
import arrow.fx.IO
import com.github.ajalt.clikt.core.CliktCommand
import java.io.File

fun readToken(tokenFileName: String): IO<Either<TokenAccessError, String>> = IO {
    Either.catch({ _ -> TokenAccessError }, { File(tokenFileName).readText() })
}

object TokenAccessError

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    override fun run() {
        IO.just("Hello world").unsafeRunSync()
    }
}.main(args)
