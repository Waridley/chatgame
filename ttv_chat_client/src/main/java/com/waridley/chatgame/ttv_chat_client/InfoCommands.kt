package com.waridley.chatgame.ttv_chat_client

import com.github.twitch4j.common.enums.CommandPermission
import com.waridley.chatgame.api.frontend.CommandMediator
import com.waridley.ttv.CommandEvent
import org.slf4j.LoggerFactory

class InfoCommands internal constructor(private val commandMediator: CommandMediator) {
	val currency: CurrencyCommands
	val stats: StatCommands
	
	class StatCommands(private val commandMediator: CommandMediator) {
		fun hours(event: CommandEvent) {
			val username: String
			username = if (event.arguments != "" && (event.permissions.contains(CommandPermission.BROADCASTER) ||
							event.permissions.contains(CommandPermission.MODERATOR)) ||
					event.permissions.contains(CommandPermission.VIP)) {
				log.debug("User " + event.user.name + " has permission to check other users' hours.")
				event.arguments.split(" ").toTypedArray()[0]
			} else {
				log.debug("User " + event.user.name + " does not have permission to check other user's hours. Defaulting to invoking user.")
				event.user.name
			}
			log.debug("Getting player for Twitch login \"$username\"")
			val player = commandMediator.getPlayerByTtvLogin(username)
			val onlineHours = player!!.ttvUser!!.onlineMinutes / 60.0
			val offlineHours = player.ttvUser!!.offlineMinutes / 60.0
			val guestHours = player.ttvUser!!.guestMinutes / 60.0
			val totalHours = onlineHours + offlineHours + guestHours
			event.respondToUser(String.format("%s: %.2fh online | %.2fh offline | %.2fh in hosted channels || %.2fh total",
					player.username,
					onlineHours,
					offlineHours,
					guestHours,
					totalHours
			))
		}
		
	}
	
	class CurrencyCommands(private val commandMediator: CommandMediator) {
		fun coins(event: CommandEvent) {
			val player = commandMediator.getPlayerByTtvUserId(event.user.id.toString())
			event.respondToUser("@" + event.user.name + " (" + player!!.username + ") is carrying " + player.backpack.currencyPouch.coins.amount + " coins")
		}
		
	}
	
	companion object {
		private val log = LoggerFactory.getLogger(TwitchChatGameClient::class.java)
	}
	
	init {
		currency = CurrencyCommands(commandMediator)
		stats = StatCommands(commandMediator)
	}
}