package com.waridley.chatgame.api.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;

public class CommandHandler {
	private EventManager eventManager;
	
	public CommandHandler(EventManager eventManager) {
		this.eventManager = eventManager;
		this.eventManager.onEvent(CommandEvent.class).subscribe(this::onCommand);
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
				default:
					//do nothing
			}
			
		}
	}

}
