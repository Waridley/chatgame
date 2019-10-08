package com.waridley.ttv;

import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Set;

/**
 * This event gets called when a message is received in a channel.
 */
@Value
@Getter
@EqualsAndHashCode(callSuper = false)
public class DeletableChannelMessageEvent extends AbstractChannelEvent {
	
	private String messageId;
	
	/**
	 * User
	 */
	private EventUser user;
	
	/**
	 * Message
	 */
	private String message;
	
	/**
	 * Permissions of the user
	 */
	private Set<CommandPermission> permissions;
	
	/**
	 * Event Constructor
	 *
	 * @param channel     The channel that this event originates from.
	 * @param user        The user who triggered the event.
	 * @param message     The plain text of the message.
	 * @param permissions The permissions of the triggering user.
	 */
	public DeletableChannelMessageEvent(String messageId, EventChannel channel, EventUser user, String message, Set<CommandPermission> permissions) {
		super(channel);
		this.messageId = messageId;
		this.user = user;
		this.message = message;
		this.permissions = permissions;
	}
}
