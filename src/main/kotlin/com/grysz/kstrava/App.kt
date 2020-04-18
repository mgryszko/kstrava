package com.grysz.kstrava

import arrow.fx.IO
import com.github.ajalt.clikt.core.CliktCommand
import java.io.File

fun readToken(tokenFileName: String): IO<String> = IO { File(tokenFileName).readText() }

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    override fun run() {
        IO.just("Hello world").unsafeRunSync()
    }
}.main(args)
