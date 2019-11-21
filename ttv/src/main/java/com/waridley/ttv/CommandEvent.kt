/**
 * Original source Copyright (c) 2018 Philipp Heuer
 * Licensed under the MIT License
 * https://github.com/twitch4j/twitch4j/blob/master/chat/src/main/java/com/github/twitch4j/chat/events/CommandEvent.java
 *
 * Modifications:
 * Separated command from arguments to allow cleaner handling
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.ttv

import com.github.twitch4j.chat.enums.CommandSource
import com.github.twitch4j.chat.events.TwitchEvent
import com.github.twitch4j.common.enums.CommandPermission
import com.github.twitch4j.common.events.domain.EventUser

//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.Value;
/**
 * This event gets called when a message is received in a channel.
 */
//@Value
//@Getter
//@EqualsAndHashCode(callSuper = false)
class CommandEvent
/**
 * Event Constructor
 *
 * @param source        Source (used for response method)
 * @param sourceId      Source Id (used for response method)
 * @param user          The user who triggered the event.
 * @param commandPrefix The command prefix used.
 * @param command       The plain command without prefix.
 * @param permissions   The permissions of the triggering user.
 */(
		/**
		 * Source: channel / privateMessage
		 */
		val source: CommandSource,
		/**
		 * Source Id: channelName or userName
		 */
		val sourceId: String,
		/**
		 * User
		 */
		val user: EventUser,
		/**
		 * Command Prefix
		 */
		val commandPrefix: String,
		/**
		 * Command
		 */
		val command: String,
		/**
		 * Arguments
		 */
		val arguments: String,
		/**
		 * Permissions of the user
		 */
		val permissions: Set<CommandPermission>) : TwitchEvent() {
	
	/**
	 * Respond to the command origin (channel or private)
	 *
	 * @param message Message
	 */
	fun respondToUser(message: String) {
		if (source == CommandSource.CHANNEL) {
			twitchChat.sendMessage(sourceId, message)
		} else if (source == CommandSource.PRIVATE_MESSAGE) {
			twitchChat.sendMessage(sourceId, "/w $sourceId $message")
		}
	}
	
}