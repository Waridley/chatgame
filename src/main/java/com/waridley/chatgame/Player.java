package com.waridley.chatgame;

import com.waridley.chatgame.ttv_integration.TwitchUser;

/* TODO:
 *  Add currency field
 *  Implement skills
 *  Implement inventory
 *  Implement bank
 *  Implement items
 */

public class Player {
	private String username;
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	private TwitchUser twitchAcct;
	public TwitchUser getTwitchAcct() { return twitchAcct; }
	public void setTwitchAcct(TwitchUser twitchAcct) { this.twitchAcct = twitchAcct; }
	
	
	
}
