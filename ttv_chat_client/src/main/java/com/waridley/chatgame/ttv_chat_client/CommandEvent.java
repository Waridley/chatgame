/**
 * Original source Copyright (c) 2018 Philipp Heuer
 * Licensed under the MIT License
 * https://github.com/twitch4j/twitch4j/blob/master/chat/src/main/java/com/github/twitch4j/chat/events/CommandEvent.java
 *
 * Modifications:
 *      Separated command from arguments to allow cleaner handling
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_chat_client;

import com.github.twitch4j.chat.enums.CommandSource;
import com.github.twitch4j.chat.events.TwitchEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventUser;

import java.util.Set;

//import lombok.EqualsAndHashCode;
//import lombok.Getter;
//import lombok.Value;

/**
 * This event gets called when a message is received in a channel.
 */
//@Value
//@Getter
//@EqualsAndHashCode(callSuper = false)
public class CommandEvent extends TwitchEvent {
	
	/**
	 * Source: channel / privateMessage
	 */
	private CommandSource source;
	
	/**
	 * Source Id: channelName or userName
	 */
	private String sourceId;
	
	/**
	 * User
	 */
	private EventUser user;
	
	/**
	 * Command Prefix
	 */
	private String commandPrefix;
	
	/**
	 * Command
	 */
	private String command;
	
	/**
	 * Arguments
	 */
	private String arguments;
	
	/**
	 * Permissions of the user
	 */
	private Set<CommandPermission> permissions;
	
	/**
	 * Event Constructor
	 *
	 * @param source        Source (used for response method)
	 * @param sourceId      Source Id (used for response method)
	 * @param user          The user who triggered the event.
	 * @param commandPrefix The command prefix used.
	 * @param command       The plain command without prefix.
	 * @param permissions   The permissions of the triggering user.
	 */
	public CommandEvent(CommandSource source, String sourceId, EventUser user, String commandPrefix, String command, String arguments, Set<CommandPermission> permissions) {
		super();
		this.source = source;
		this.sourceId = sourceId;
		this.user = user;
		this.commandPrefix = commandPrefix;
		this.command = command;
		this.arguments = arguments;
		this.permissions = permissions;
	}
	
	/**
	 * Respond to the command origin (channel or private)
	 *
	 * @param message Message
	 */
	public void respondToUser(String message) {
		if(source.equals(CommandSource.CHANNEL)) {
			getTwitchChat().sendMessage(sourceId, message);
		} else if(source.equals(CommandSource.PRIVATE_MESSAGE)) {
			getTwitchChat().sendMessage(sourceId, "/w " + sourceId + " " + message);
		}
	}
	
	
	public CommandSource getSource() {
		return source;
	}
	
	public String getSourceId() {
		return sourceId;
	}
	
	public String getCommandPrefix() {
		return commandPrefix;
	}
	
	public EventUser getUser() {
		return user;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getArguments() {
		return arguments;
	}
	
	public Set<CommandPermission> getPermissions() {
		return permissions;
	}
	
}