/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_chat_client;

import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.ttv.CommandEvent;
import org.slf4j.Logger;

public class CommandParser {
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(TwitchChatGameClient.class);;
	private EventManager eventManager;
	private CommandMediator commandMediator;
	private InfoCommands info;
	private String channelName;
	
	public CommandParser(EventManager eventManager, CommandMediator commandMediator) {
		this.eventManager = eventManager;
		this.eventManager.onEvent(CommandEvent.class).subscribe(this::onCommand);
		this.commandMediator = commandMediator;
		
		this.info = new InfoCommands(commandMediator);
	}
	
	private void onCommand(CommandEvent event) {
		log.debug("Attempting to execute command:\n" +
				"{\n" +
				"   prefix: \"" + event.getCommandPrefix() + "\",\n" +
				"   command: \"" + event.getCommand() + "\",\n" +
				"   arguments: \"" + event.getArguments() + "\"\n" +
				"}");
		
		switch(event.getCommandPrefix()) {
			case("!"):
				standardCommand(event);
				break;
			case("();"):
				javaCommand(event);
				break;
			default:
				notCommand(event);
		}
		
	}
	
	private void standardCommand(CommandEvent event) {
		String command = event.getCommand();
		String argString = event.getArguments();
		
		switch(command) {
			case("echo"):
				event.respondToUser(argString);
				break;
			case("hours"):
				info.stats.hours(event);
				break;
			case("loop"):
				if(event.getPermissions().contains(CommandPermission.BROADCASTER)) event.respondToUser("!loop");
				break;
			case("snowflake"):
				event.respondToUser("\u2744\uFE0F");
				break;
			case("coins"):
				info.currency.coins(event);
				break;
			default:
				notCommand(event);
			
		}
	}
	
	private void javaCommand(CommandEvent event) {
		switch(event.getCommand()) {
			case("halt"):
				if(event.getPermissions().contains(CommandPermission.BROADCASTER)) {
					eventManager.onEvent(ChannelLeaveEvent.class).subscribe(e -> System.exit(0));
					event.getTwitchChat().sendMessage(event.getSourceId(), "(x_x)");
					event.getTwitchChat().leaveChannel(event.getSourceId());
					try { Thread.sleep(60 * 1000); } catch(InterruptedException e) { log.debug("Interrupted while waiting for shutdown"); }
				} else {
					event.respondToUser("@" + event.getUser().getName() + " -- ONLY " + event.getSourceId().toUpperCase() + " CAN KILL ME! TwitchRPG");
				}
				break;
		}
	}
	
	private void notCommand(CommandEvent event) {
		log.info("Did not recognize command: " + event.getCommand());
	}

}
