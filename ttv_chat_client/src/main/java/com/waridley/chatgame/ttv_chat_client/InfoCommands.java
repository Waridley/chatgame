package com.waridley.chatgame.ttv_chat_client;

import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.chatgame.game.Player;

public class InfoCommands {
	
	private CommandMediator commandMediator;
	
	public InfoCommands(CommandMediator mediator) {
		this.commandMediator = mediator;
	}
	
	void hours(CommandEvent event) {
		String username;
		if(!event.getArguments().equals("") && (
				event.getPermissions().contains(CommandPermission.BROADCASTER) ||
				event.getPermissions().contains(CommandPermission.MODERATOR)) ||
				event.getPermissions().contains(CommandPermission.VIP)
		) {
			System.out.println("User " + event.getUser().getName() + " has permission to see other users' hours.");
			username = event.getArguments().split(" ")[0];
		} else {
			System.out.println("User " + event.getUser().getName() + " does not have permission to view other user's hours. Defaulting to invoking user.");
			username = event.getUser().getName();
		}
		
		System.out.println("Getting player for Twitch login \"" + username + "\"");
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
