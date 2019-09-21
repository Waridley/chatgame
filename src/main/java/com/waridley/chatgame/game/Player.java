/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.game;

import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.types.ObjectId;

/* TODO:
 *  Add currency field
 *  Implement skills
 *  Implement inventory
 *  Implement bank
 *  Implement items
 */

public class Player {
	
	private ObjectId _id = new ObjectId();
	public ObjectId getObjectId() {
		return _id;
	}
	public void setObjectId(ObjectId id) {
		this._id = id;
	}
	
	private String username = null;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	private Long twitchUserId = null;
	public Long getTwitchUserId() { return twitchUserId; }
	public void setTwitchUserId(Long twitchUserId) { this.twitchUserId = twitchUserId; }
	
	public Player(String username) {
		this.username = username;
	}
	
	public Player(TwitchUser twitchUser) {
		this.twitchUserId = twitchUser.getUserId();
		if(twitchUser.getHelixUser() != null) {
			this.username = twitchUser.getHelixUser().getDisplayName();
		}
	}
	
	public Player(String username, TwitchUser twitchUser) {
		this.username = username;
		this.twitchUserId = twitchUser.getUserId();
	}
	
	public Player(String username, Long twitchUserId) {
		this.username = username;
		this.twitchUserId = twitchUserId;
	}
}
