package com.waridley.chatgame

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.twitch4j.TwitchClient
import com.waridley.ttv.MongoTtvOptions
import com.waridley.ttv.TtvClientOptions
import com.waridley.ttv.mongo.MongoTtvBackend

class Launcher: CliktCommand(name = "chatgame") {
	
	private lateinit var twitchClient: TwitchClient
	
	private lateinit var ttvClientOptions: TtvClientOptions
	
	private val ttvBackendOptions
			by option("--ttv-backend").groupChoice(
					"mongodb" to MongoTtvOptions(),
					"mongo" to MongoTtvOptions(),
					"m" to MongoTtvOptions()
			)
	
	override fun run() {
		
		when(val backOpts = ttvBackendOptions) {
				is MongoTtvOptions -> {
					val ttvBackend = MongoTtvBackend(backOpts.db, twitchClient.helix, ttvClientOptions.idProvider.get)
				}
		}
		
		CliParser(ttvBackend, gameBackend).start()
	}
	
}

fun main(args: Array<String>) = Launcher().main(args)