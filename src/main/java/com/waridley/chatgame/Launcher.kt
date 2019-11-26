package com.waridley.chatgame

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.twitch4j.TwitchClient
import com.waridley.ttv.MongoTtvOptions
import com.waridley.ttv.TtvClientOptions

class Launcher: CliktCommand(name = "chatgame", invokeWithoutSubcommand = true) {
	
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
				
				}
		}
		
		
	}
	
}

val launchCmd = Launcher().subcommands(TtvClientOptions())

fun main(args: Array<String>) {
	launchCmd.main(args)
	StdinParser().start()
}