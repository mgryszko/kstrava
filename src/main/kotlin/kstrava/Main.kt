package com.grysz.kstrava

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

class ListActivitiesCommand : CliktCommand(name = "list") {
    private val config by requireObject<Map<String, String>>()

    override fun run() {
        val accessTokenFileName = config["accessTokenFileName"] ?: error("command context should contain accessTokenFileName")
        listActivitiesApp(accessTokenFileName).unsafeRunSync()
    }
}

class UpdateActivitiesCommand : CliktCommand(name = "update") {
    override fun run() {
        println("update called")
    }
}

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    private val accessTokenFileName: String by option(help = "File name containing access token").default(".access-token")
    private val config by findOrSetObject { mutableMapOf<String, String>() }

    override fun run() {
        config["accessTokenFileName"] = accessTokenFileName
    }
}.subcommands(ListActivitiesCommand(), UpdateActivitiesCommand()).main(args)
