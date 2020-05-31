package com.grysz.kstrava

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

class ListActivitiesCommand : CliktCommand(name = "list") {
    private val config by requireObject<Map<String, String>>()

    override fun run() {
        val accessTokenFileName = config["accessTokenFileName"] ?: error("command context should contain accessTokenFileName")
        listActivitiesApp(accessTokenFileName).unsafeRunSync()
    }
}

class UpdateActivitiesCommand : CliktCommand(name = "update") {
    private val name by option(help = "Activity name").required()
    private val activityIds: List<String> by argument().multiple()
    private val config by requireObject<Map<String, String>>()

    override fun run() {
        val accessTokenFileName = config["accessTokenFileName"] ?: error("command context should contain accessTokenFileName")
        updateActivitiesApp(accessTokenFileName, activityIds, name).unsafeRunSync()
    }
}

fun main(args: Array<String>) = object : CliktCommand(name = "kstrava") {
    private val accessTokenFileName: String by option(help = "File name containing access token").default(".access-token")
    private val config by findOrSetObject { mutableMapOf<String, String>() }

    override fun run() {
        config["accessTokenFileName"] = accessTokenFileName
    }
}.subcommands(ListActivitiesCommand(), UpdateActivitiesCommand()).main(args)
