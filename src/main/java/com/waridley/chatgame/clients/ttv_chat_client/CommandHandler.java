/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.backend.twitch.TwitchStorageInterface;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Optional;

public class CommandHandler {
	private TwitchClient twitchClient;
	private EventManager eventManager;
	private TwitchStorageInterface storageInterface;
	
	public CommandHandler(TwitchClient twitchClient, TwitchStorageInterface storageInterface) {
		this.twitchClient = twitchClient;
		this.eventManager = twitchClient.getEventManager();
		this.eventManager.onEvent(CommandEvent.class).subscribe(this::onCommand);
		this.storageInterface = storageInterface;
	}
	
	private void onCommand(CommandEvent event) {
		String prefix = event.getCommandPrefix();
		String command = event.getCommand();
		String argString = event.getArguments();
		
		if(prefix.equals("!")) {
			switch(command) {
				case("echo"):
					event.respondToUser(argString);
					break;
				case("halt"):
					if(event.getPermissions().contains(CommandPermission.BROADCASTER)) {
						eventManager.onEvent(ChannelLeaveEvent.class).subscribe(e -> System.exit(0));
						event.getTwitchChat().sendMessage(event.getSourceId(), "(x_x)");
						event.getTwitchChat().leaveChannel(event.getSourceId());
					} else {
						event.respondToUser("@" + event.getUser().getName() + " -- ONLY WARIDLEY CAN KILL ME! MrDestructoid");
					}
					break;
				case("hours"):
					Optional<TwitchUser> user = Optional.empty();
					try {
						if(!event.getArguments().equals("") && (
								event.getPermissions().contains(CommandPermission.BROADCASTER) ||
								event.getPermissions().contains(CommandPermission.MODERATOR)) ||
								event.getPermissions().contains(CommandPermission.VIP)
								) {
							user = Optional.ofNullable(storageInterface.findOrCreateTwitchUser(event.getArguments().split(" ")[0]));
						} else {
							user = Optional.ofNullable(storageInterface.findOrCreateTwitchUser(event.getUser().getId()));
						}
					} catch(TwitchUser.UserNotFoundException e) {
						e.printStackTrace();
					}
					if(user.isPresent()) {
						String onlineHours = String.format("%.2f", user.get().getOnlineHours());
						String offlineHours = String.format("%.2f", user.get().getOfflineHours());
						String totalHours = String.format("%.2f", user.get().getTotalHours());
						
						event.respondToUser(user.get().getHelixUser().getDisplayName() + " :: "
								+ onlineHours + "h online" + " | "
								+ offlineHours + "h offline" + " | "
								+ totalHours + "h total"
						);
					} else {
						event.respondToUser("Error: Couldn't find user " + event.getUser().getName());
					}
				
				default:
					//do nothing
			}
			
		}
	}

}
