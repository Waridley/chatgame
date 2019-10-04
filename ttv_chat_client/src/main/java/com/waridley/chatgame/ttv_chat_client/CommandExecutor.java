/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.api.frontend.CommandMediator;

public class CommandExecutor {
	private EventManager eventManager;
	private CommandMediator commandMediator;
	private InfoCommands infoCommands;
	
	public CommandExecutor(EventManager eventManager, CommandMediator commandMediator) {
		this.eventManager = eventManager;
		this.eventManager.onEvent(CommandEvent.class).subscribe(this::onCommand);
		this.commandMediator = commandMediator;
		
		this.infoCommands = new InfoCommands(commandMediator);
	}
	
	private void onCommand(CommandEvent event) {
		String prefix = event.getCommandPrefix();
		String command = event.getCommand();
		String argString = event.getArguments();
		
		if(prefix.equals("!")) {
			System.out.println("Attempting to execute command: " + command);
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
						event.respondToUser("@" + event.getUser().getName() + " -- ONLY WARIDLEY CAN KILL ME! TwitchRPG");
					}
					break;
				case("hours"):
					infoCommands.hours(event);
					
				default:
					//do nothing
			}
			
		}
	}

}
