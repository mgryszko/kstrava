package com.grysz.kstrava

import arrow.fx.IO
import com.github.ajalt.clikt.core.CliktCommand

class App {
    val greeting: IO<String>
        get() = IO.just("Hello world")
}

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    override fun run() {
        App().greeting.unsafeRunSync()
    }
}.main(args)
