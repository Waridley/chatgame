package com.waridley.chatgame.ttv_chat_client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.waridley.chatgame.api.CommandMediator
import com.waridley.ttv.TtvClientOptions

class TtvChatGameClientOptions(
		val ttvClientOptions: TtvClientOptions): CliktCommand(name = "ttv-chat-client") {
	
	val channelName by requireObject<String>()
	
	val commandMediator by requireObject<CommandMediator>()
	
	override fun run() {
		TwitchChatGameClient(this)
	}
	
	
}