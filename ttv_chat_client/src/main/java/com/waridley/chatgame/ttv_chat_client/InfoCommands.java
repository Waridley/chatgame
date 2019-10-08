package com.waridley.chatgame.ttv_chat_client;

import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.chatgame.game.Player;
import com.waridley.ttv.CommandEvent;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;

public class InfoCommands {
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(TwitchChatGameClient.class);;
	private CommandMediator commandMediator;
	
	final CurrencyCommands currency;
	final StatCommands stats;
	
	InfoCommands(CommandMediator mediator) {
		this.commandMediator = mediator;
		currency = new CurrencyCommands(commandMediator);
		stats = new StatCommands(commandMediator);
	}
	
	
	
	@AllArgsConstructor
	static class StatCommands {
		private CommandMediator commandMediator;
		
		void hours(CommandEvent event) {
			String username;
			if(!event.getArguments().equals("") && (
					event.getPermissions().contains(CommandPermission.BROADCASTER) ||
					event.getPermissions().contains(CommandPermission.MODERATOR)) ||
					event.getPermissions().contains(CommandPermission.VIP)
			) {
				log.debug("User " + event.getUser().getName() + " has permission to check other users' hours.");
				username = event.getArguments().split(" ")[0];
			} else {
				log.debug("User " + event.getUser().getName() + " does not have permission to check other user's hours. Defaulting to invoking user.");
				username = event.getUser().getName();
			}
			
			log.debug("Getting player for Twitch login \"" + username + "\"");
			Player player = commandMediator.getPlayerByTtvLogin(username);
			double onlineHours = player.getTtvUser().getOnlineMinutes() / 60.0;
			double offlineHours = player.getTtvUser().getOfflineMinutes() / 60.0;
			double guestHours = player.getTtvUser().getGuestMinutes() / 60.0;
			double totalHours = onlineHours + offlineHours + guestHours;
			
			event.respondToUser(String.format("%s: %.2fh online | %.2fh offline | %.2fh in hosted channels || %.2fh total",
					player.getUsername(),
					onlineHours,
					offlineHours,
					guestHours,
					totalHours
					)
			);
		}
	}
	
	@AllArgsConstructor
	static class CurrencyCommands {
		
		private CommandMediator commandMediator;
		
		void coins(CommandEvent event) {
			Player player = commandMediator.getPlayerByTtvUserId(String.valueOf(event.getUser().getId()));
			event.respondToUser("@" + event.getUser().getName() + " (" + player.getUsername() + ") is carrying " + player.getBackpack().getCurrencyPouch().getCoins().getAmount() + " coins");
		}
		
		
		
	}
	
}
