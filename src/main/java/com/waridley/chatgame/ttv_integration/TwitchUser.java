/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_integration;

import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.game.Player;


/* TODO:
 *  Log chat messages
 */

public class TwitchUser {
	
	private long userid;
	public long getUserId() { return userid; }
	public void setUserid(long id) {
		this.userid = id;
	}
	
	private String login;
	public String getLogin() { return login; }
	public void setLogin(String login) { this.login = login; }
	
	private long offlineMinutes;
	public long getOfflineMinutes() { return offlineMinutes; }
	public double getOfflineHours() { return toHours(offlineMinutes); }
	public void setOfflineMinutes(long minutes) { this.offlineMinutes = minutes; }
	
	private long onlineMinutes;
	public long getOnlineMinutes() { return onlineMinutes; }
	public double getOnlineHours() { return toHours(onlineMinutes); }
	public void setOnlineMinutes(long minutes) { this.onlineMinutes = minutes; }
	
	public long getTotalMinutes() { return onlineMinutes + offlineMinutes; }
	public double getTotalHours() { return toHours(getTotalMinutes()); }
	
	private Player gameAcct;
	public Player getGameAcct() { return gameAcct; }
	public void setGameAcct(Player gameAcct) { this.gameAcct = gameAcct; }
	
	private User helixUser;
	public User getHelixUser() { return helixUser; }
	public void setHelixUser(User helixUser) {
		this.helixUser = helixUser;
		userid = helixUser.getId();
	}
	
	public static double toHours(long mintues) {
		return ((double) mintues) / 60.0;
	}
	
	public static class UserNotFoundException extends Exception {
		
		public UserNotFoundException(String message, Throwable e) {
			super(message, e);
		}
		
	}
}
