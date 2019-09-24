package com.waridley.chatgame.ttv_integration;

import com.github.twitch4j.helix.domain.User;

public class TtvUser {
	
	private long id;
	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
	
	private User helixUser;
	public User getHelixUser() { return helixUser; }
	public void setHelixUser(User helixUser) { this.helixUser = helixUser; }
	
	private long offlineMinutes = 0L;
	public long getOfflineMinutes() { return offlineMinutes; }
//	public double getOfflineHours() { return toHours(offlineMinutes); }
	public void setOfflineMinutes(long minutes) { this.offlineMinutes = minutes; }
	
	private long onlineMinutes = 0L;
	public long getOnlineMinutes() { return onlineMinutes; }
//	public double getOnlineHours() { return toHours(onlineMinutes); }
	public void setOnlineMinutes(long minutes) { this.onlineMinutes = minutes; }
	
	public TtvUser(User helixUser) {
		setHelixUser(helixUser);
	}
	
	public long totalMinutes() { return onlineMinutes + offlineMinutes; }
//	public double getTotalHours() { return toHours(getTotalMinutes()); }
	
	public static double toHours(long mintues) {
		return ((double) mintues) / 60.0;
	}
}
