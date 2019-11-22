package com.waridley.chatgame.server

import com.github.ajalt.clikt.core.CliktCommand
import com.waridley.chatgame.api.backend.GameStorageInterface
import com.waridley.ttv.TtvStorageInterface

class Server(private val ttvBackend: TtvStorageInterface, private val gameBackend: GameStorageInterface): CliktCommand() {
	
	val commandExecutive: CommandExecutive = CommandExecutive(ttvBackend, gameBackend)
	
	override fun run() {
		println("running server")
	}
}
