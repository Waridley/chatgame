package com.waridley.ttv;

import com.github.twitch4j.chat.events.AbstractChannelEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.common.events.domain.EventUser;

import java.util.Set;

/**
 * This event gets called when a message is received in a channel.
 */
public final class DeletableChannelMessageEvent extends AbstractChannelEvent {
	
	private final String messageId;
	
	/**
	 * User
	 */
	private final EventUser user;
	
	/**
	 * Message
	 */
	private final String message;
	
	/**
	 * Permissions of the user
	 */
	private final Set<CommandPermission> permissions;
	
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
	
	public String getMessageId() {
		return this.messageId;
	}
	
	public EventUser getUser() {
		return this.user;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Set<CommandPermission> getPermissions() {
		return this.permissions;
	}
	
	public String toString() {
		return "DeletableChannelMessageEvent(messageId=" + this.getMessageId() + ", user=" + this.getUser() + ", message=" + this.getMessage() + ", permissions=" + this.getPermissions() + ")";
	}
	
	public boolean equals(final Object o) {
		if(o == this) return true;
		if(!(o instanceof DeletableChannelMessageEvent)) return false;
		final DeletableChannelMessageEvent other = (DeletableChannelMessageEvent) o;
		if(!other.canEqual((Object) this)) return false;
		final Object this$messageId = this.getMessageId();
		final Object other$messageId = other.getMessageId();
		if(this$messageId == null ? other$messageId != null : !this$messageId.equals(other$messageId)) return false;
		final Object this$user = this.getUser();
		final Object other$user = other.getUser();
		if(this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if(this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		final Object this$permissions = this.getPermissions();
		final Object other$permissions = other.getPermissions();
		if(this$permissions == null ? other$permissions != null : !this$permissions.equals(other$permissions))
			return false;
		return true;
	}
	
	protected boolean canEqual(final Object other) {
		return other instanceof DeletableChannelMessageEvent;
	}
	
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $messageId = this.getMessageId();
		result = result * PRIME + ($messageId == null ? 43 : $messageId.hashCode());
		final Object $user = this.getUser();
		result = result * PRIME + ($user == null ? 43 : $user.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		final Object $permissions = this.getPermissions();
		result = result * PRIME + ($permissions == null ? 43 : $permissions.hashCode());
		return result;
	}
}
