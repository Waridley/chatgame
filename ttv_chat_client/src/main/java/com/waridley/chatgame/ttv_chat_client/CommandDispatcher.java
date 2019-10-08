/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.enums.CommandSource;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import com.waridley.ttv.CommandEvent;
import com.waridley.ttv.DeletableChannelMessageEvent;
import org.slf4j.Logger;

import java.util.Optional;

class CommandDispatcher {
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(TwitchChatGameClient.class);;
	private EventManager eventManager;
	
	CommandDispatcher(EventManager eventManager) {
		this.eventManager = eventManager;
		eventManager.onEvent(ChannelMessageEvent.class).subscribe(this::onChannelMessage);
		eventManager.onEvent(IRCMessageEvent.class).subscribe(this::dispatchDeletableMessage);
		eventManager.onEvent(IRCMessageEvent.class).subscribe(e -> log.trace(e.getRawMessage()));
		
	}
	
	private void onChannelMessage(ChannelMessageEvent event) {
//		if(event.getUser().getName().equalsIgnoreCase("snowpoke"))
//			event.getTwitchChat().sendMessage(event.getChannel().getName(), "\u2744\uFE0F️ Snowpoke is the best streamer! \u2744\uFE0F️");
		
		String message = event.getMessage();
		String prefix = null;
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
			prefix = "();";
			commandLength = message.indexOf('(');
			
			if(commandLength > 0) {
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
					log.debug("Recognized prefix " + prefix + " but no spaces are present in message.");
				}
			} else {
				log.debug("Recognized prefix " + prefix + " but no opening ( is present in message.");
			}
			
		}
		if(prefix != null) log.debug("Recognized command prefix \"" + prefix + "\"");
		
		
	}
	
	private void dispatchDeletableMessage(IRCMessageEvent event) {
		Optional<String> id = event.getTagValue("id");
		
		if(id.isPresent()) {
			EventChannel channel = event.getChannel();
			EventUser user = event.getUser();
			eventManager.dispatchEvent(new DeletableChannelMessageEvent(id.get(), channel, user, event.getMessage().orElse(""), event.getClientPermissions()));
		}
	}
}
