/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.backend.TtvStorageInterface;
import com.waridley.chatgame.ttv_integration.TtvUser;

public class CommandHandler {
	private EventManager eventManager;
	private TtvStorageInterface storageInterface;
	
	public CommandHandler(EventManager eventManager, TtvStorageInterface storageInterface) {
		this.eventManager = eventManager;
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
					String username;
					if(!event.getArguments().equals("") && (
							event.getPermissions().contains(CommandPermission.BROADCASTER) ||
							event.getPermissions().contains(CommandPermission.MODERATOR)) ||
							event.getPermissions().contains(CommandPermission.VIP)
							) {
						username = event.getArguments().split(" ")[0];
					} else {
						username = event.getUser().getName();
					}
					TtvUser user = storageInterface.findOrCreateTtvUser(username);
					String onlineHours = String.format("%.2f", user.getOnlineMinutes() / 60.0);
					String offlineHours = String.format("%.2f", user.getOfflineMinutes() / 60.0);
					String totalHours = String.format("%.2f", user.totalMinutes() / 60.0);
					
					event.respondToUser(user.getHelixUser().getDisplayName() + " :: "
							+ onlineHours + "h online" + " | "
							+ offlineHours + "h offline" + " | "
							+ totalHours + "h total"
					);
				default:
					//do nothing
			}
			
		}
	}

}
