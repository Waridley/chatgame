package com.waridley.chatgame.api.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.helix.domain.UserList;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Collections;

public class CommandHandler {
	private TwitchClient twitchClient;
	private EventManager eventManager;
	private StorageInterface storageInterface;
	
	public CommandHandler(TwitchClient twitchClient, StorageInterface storageInterface) {
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
					UserList chatters = twitchClient.getHelix().getUsers(
							null,
							Collections.singletonList(event.getUser().getId()),
							null
					).execute();
					TwitchUser user = storageInterface.findOrCreateTwitchUser(chatters.getUsers().get(0));
					String onlineHours = String.format("%.2f", user.getOnlineHours());
					String offlineHours = String.format("%.2f", user.getOfflineHours());
					String totalHours = String.format("%.2f", user.getTotalHours());
					
					event.respondToUser("@" + chatters.getUsers().get(0).getDisplayName() + " :: "
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
