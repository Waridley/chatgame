/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.api.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.enums.CommandSource;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.waridley.chatgame.api.ttv_chat_client.CommandEvent;

public class CommandDispatcher {
	private EventManager eventManager;
	
	public CommandDispatcher(EventManager eventManager) {
		this.eventManager = eventManager;
		this.eventManager.onEvent(ChannelMessageEvent.class).subscribe(this::onChannelMessage);
	}
	
	private void onChannelMessage(ChannelMessageEvent event) {
		String message = event.getMessage();
		String prefix;
		String command;
		int commandLength;
		String arguments;
		
		if(message.startsWith("!")) {
			prefix = "!";
			message = message.replaceFirst("!", "");
			commandLength = message.indexOf(' ');
			if(commandLength < 1) commandLength = message.length(); //message contains no spaces .: is command with no arguments
			command = message.substring(0, commandLength);
			arguments = message.substring(commandLength).trim();
			
			eventManager.dispatchEvent(new CommandEvent(
					CommandSource.CHANNEL,
					event.getChannel().getName(),
					event.getUser(),
					prefix,
					command,
					arguments,
					event.getPermissions()));
			
		} else if(message.endsWith(");")) {
			commandLength = message.indexOf('(');
			
			if(commandLength > 0) {
				prefix = "();";
				command = message.substring(0, commandLength).trim();
				if(!command.contains(" ")) {
					arguments = message.substring(commandLength + 1, message.length() - 2).trim();
					
					eventManager.dispatchEvent(new CommandEvent(
							CommandSource.CHANNEL,
							event.getChannel().getName(),
							event.getUser(),
							prefix,
							command,
							arguments,
							event.getPermissions()));
				} else {
					//ignore, talking *about* a method, not calling it
				}
			} else {
				//ignore, no ( present
			}
			
		}
		
	}
}
