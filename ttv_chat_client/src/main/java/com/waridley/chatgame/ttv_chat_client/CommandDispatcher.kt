/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.ttv_chat_client

import com.github.philippheuer.events4j.EventManager
import com.github.twitch4j.chat.enums.CommandSource
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chat.events.channel.IRCMessageEvent
import com.waridley.ttv.CommandEvent
import com.waridley.ttv.DeletableChannelMessageEvent
import org.slf4j.LoggerFactory

internal class CommandDispatcher(private val eventManager: EventManager) {
	private fun onChannelMessage(event: ChannelMessageEvent) { //		if(event.getUser().getName().equalsIgnoreCase("snowpoke"))
//			event.getTwitchChat().sendMessage(event.getChannel().getName(), "\u2744\uFE0F️ Snowpoke is the best streamer! \u2744\uFE0F️");
		var message = event.message
		var prefix: String? = null
		val command: String
		var commandLength: Int
		val arguments: String
		if (message.startsWith("!")) {
			prefix = "!"
			message = message.replaceFirst("!".toRegex(), "")
			commandLength = message.indexOf(' ')
			if (commandLength < 1) commandLength = message.length //message contains no spaces .: is command with no arguments
			command = message.substring(0, commandLength)
			arguments = message.substring(commandLength).trim { it <= ' ' }
			eventManager.dispatchEvent(CommandEvent(
					CommandSource.CHANNEL,
					event.channel.name,
					event.user,
					prefix,
					command,
					arguments,
					event.permissions))
		} else if (message.endsWith(");")) {
			prefix = "();"
			commandLength = message.indexOf('(')
			if (commandLength > 0) {
				command = message.substring(0, commandLength).trim { it <= ' ' }
				if (!command.contains(" ")) {
					arguments = message.substring(commandLength + 1, message.length - 2).trim { it <= ' ' }
					eventManager.dispatchEvent(CommandEvent(
							CommandSource.CHANNEL,
							event.channel.name,
							event.user,
							prefix,
							command,
							arguments,
							event.permissions))
				} else {
					log.debug("Recognized prefix $prefix but no spaces are present in message.")
				}
			} else {
				log.debug("Recognized prefix $prefix but no opening ( is present in message.")
			}
		}
		if (prefix != null) log.debug("Recognized command prefix \"$prefix\"")
	}
	
	private fun dispatchDeletableMessage(event: IRCMessageEvent) {
		val id = event.getTagValue("id")
		if (id.isPresent) {
			val channel = event.channel
			val user = event.user
			eventManager.dispatchEvent(DeletableChannelMessageEvent(id.get(), channel, user, event.message.orElse(""), event.clientPermissions))
		}
	}
	
	companion object {
		private val log = LoggerFactory.getLogger(TwitchChatGameClient::class.java)
	}
	
	init {
		eventManager.onEvent(ChannelMessageEvent::class.java).subscribe { event: ChannelMessageEvent -> onChannelMessage(event) }
		eventManager.onEvent(IRCMessageEvent::class.java).subscribe { event: IRCMessageEvent -> dispatchDeletableMessage(event) }
		eventManager.onEvent(IRCMessageEvent::class.java).subscribe { e: IRCMessageEvent -> log.trace(e.rawMessage) }
	}
}