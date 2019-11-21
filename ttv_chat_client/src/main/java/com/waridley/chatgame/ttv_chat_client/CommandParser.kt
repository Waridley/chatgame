/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.ttv_chat_client

import com.github.philippheuer.events4j.EventManager
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent
import com.github.twitch4j.common.enums.CommandPermission
import com.waridley.chatgame.api.frontend.CommandMediator
import com.waridley.ttv.CommandEvent
import org.slf4j.LoggerFactory

internal class CommandParser(private val eventManager: EventManager, commandMediator: CommandMediator) {
	private val commandMediator: CommandMediator
	private val info: InfoCommands
	private val channelName: String? = null
	private fun onCommand(event: CommandEvent) {
		log.debug("Attempting to execute command:\n" +
				"{\n" +
				"   prefix: \"" + event.commandPrefix + "\",\n" +
				"   command: \"" + event.command + "\",\n" +
				"   arguments: \"" + event.arguments + "\"\n" +
				"}")
		when (event.commandPrefix) {
			"!" -> standardCommand(event)
			"();" -> javaCommand(event)
			else -> notCommand(event)
		}
	}
	
	private fun standardCommand(event: CommandEvent) {
		val command = event.command
		val argString = event.arguments
		when (command) {
			"loop" -> if (event.permissions.contains(CommandPermission.BROADCASTER)) event.respondToUser("!loop")
			"coins" -> info.currency.coins(event)
			else -> notCommand(event)
		}
	}
	
	private fun javaCommand(event: CommandEvent) {
		when (event.command) {
			"halt" -> if (event.permissions.contains(CommandPermission.BROADCASTER)) {
				eventManager.onEvent(ChannelLeaveEvent::class.java).subscribe { e: ChannelLeaveEvent? -> System.exit(0) }
				event.twitchChat.sendMessage(event.sourceId, "(x_x)")
				event.twitchChat.leaveChannel(event.sourceId)
				try {
					Thread.sleep(60 * 1000.toLong())
				} catch (e: InterruptedException) {
					log.debug("Interrupted while waiting for shutdown")
				}
			} else {
				event.respondToUser("@" + event.user.name + " -- ONLY " + event.sourceId.toUpperCase() + " CAN KILL ME! TwitchRPG")
			}
		}
	}
	
	private fun notCommand(event: CommandEvent) {
		log.info("Did not recognize command: " + event.command)
	}
	
	companion object {
		private val log = LoggerFactory.getLogger(TwitchChatGameClient::class.java)
	}
	
	init {
		eventManager.onEvent(CommandEvent::class.java).subscribe { event: CommandEvent -> onCommand(event) }
		this.commandMediator = commandMediator
		info = InfoCommands(commandMediator)
	}
}