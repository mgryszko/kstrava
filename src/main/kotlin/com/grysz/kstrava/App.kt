package com.grysz.kstrava

import com.github.ajalt.clikt.core.CliktCommand

class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    override fun run() {
        println("Hello world!")
    }
}.main(args)
