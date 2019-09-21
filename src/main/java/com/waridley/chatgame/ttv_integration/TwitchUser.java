/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_integration;

import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.game.Player;
import org.bson.types.ObjectId;


/* TODO:
 *  Log chat messages
 */

public class TwitchUser {
	
	private Long userid = null;
	public Long getUserId() { return userid; }
	public void setUserid(Long id) {
		this.userid = id;
	}
	
	private String login = null;
	public String getLogin() { return login; }
	public void setLogin(String login) { this.login = login; }
	
	private long offlineMinutes = 0L;
	public long getOfflineMinutes() { return offlineMinutes; }
	public double getOfflineHours() { return toHours(offlineMinutes); }
	public void setOfflineMinutes(long minutes) { this.offlineMinutes = minutes; }
	
	private long onlineMinutes = 0L;
	public long getOnlineMinutes() { return onlineMinutes; }
	public double getOnlineHours() { return toHours(onlineMinutes); }
	public void setOnlineMinutes(long minutes) { this.onlineMinutes = minutes; }
	
	public long getTotalMinutes() { return onlineMinutes + offlineMinutes; }
	public double getTotalHours() { return toHours(getTotalMinutes()); }
	
	private ObjectId playerId = null;
	public ObjectId getPlayerId() { return playerId; }
	public void setPlayerId(ObjectId playerId) { this.playerId = playerId; }
	
	private User helixUser = null;
	public User getHelixUser() { return helixUser; }
	public void setHelixUser(User helixUser) {
		this.helixUser = helixUser;
		setUserid(helixUser.getId());
	}
	
	public static double toHours(long mintues) {
		return ((double) mintues) / 60.0;
	}
	
	public static class UserNotFoundException extends Exception {
		
		public UserNotFoundException(String message) {
			super(message);
		}
		
		public UserNotFoundException(String message, Throwable e) {
			super(message, e);
		}
		
	}
}
