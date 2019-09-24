/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.game;

import com.mongodb.client.MongoCollection;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.ttv_integration.TtvUser;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/* TODO:
 *  Add currency field
 *  Implement skills
 *  Implement inventory
 *  Implement bank
 *  Implement items
 */

public class Player implements Comparable {
	
	private ObjectId id;
	public ObjectId getId() { return id; }
	private void setId(ObjectId id) { this.id = id; }
	
	private String username;
	public String getUsername() { return username; }
	private void setUsername(String username) { this.username = username; }
	public void changeUsername(String username, boolean really) {
		if(really) {
			setUsername(username);
		}
	}
	
	private TtvUser ttvUser;
	public TtvUser getTtvUser() { return ttvUser; }
	private void setTtvUser(TtvUser ttvUser) {
		if(this.username == null || this.username.equalsIgnoreCase(ttvUser.getHelixUser().getDisplayName())) setUsername(ttvUser.getHelixUser().getDisplayName());
		this.ttvUser = ttvUser;
//		this.twitchUserId = ttvUser.getId();
	}
	public void changeTwitchAccount(TtvUser ttvUser, boolean changeUsername) {
		changeUsername(ttvUser.getHelixUser().getDisplayName(), changeUsername);
		setTtvUser(ttvUser);
	}
	
//	private Long twitchUserId = null;
//	public Long getTwitchUserId() { return twitchUserId; }
//	private void setTwitchUserId(Long twitchUserId) {
//		if(ttvUser == null) {
//			//TODO find TtvUser in database
//		} else if(!ttvUser.getId().equals(twitchUserId)) {
//			throw new RuntimeException("User ID does not match existing ttvUser id");
//		}
//		this.twitchUserId = twitchUserId;
//	}
	
//	@Deprecated
//	public Player(TwitchUser twitchUser) {
//		setTwitchUserId(twitchUser.getUserId());
//	}
//
//	@Deprecated
//	public Player(String username, Long twitchUserId) {
//		setUsername(username);
//		setTwitchUserId(twitchUserId);
//	}
	
	//region Constructors
	//for pojo deserialization
	protected Player() {
		this(new ObjectId());
	}
	
	public Player(TtvUser ttvUser) {
		this(new ObjectId(), ttvUser);
	}
	
	public Player(String username, TtvUser ttvUser) {
		this(new ObjectId(), username, ttvUser);
	}
	
	public Player(ObjectId id) {
		this(id, null, null);
	}
	
	public Player(ObjectId id, String username) {
		this(id, username, null);
	}
	
	public Player(ObjectId id, TtvUser ttvUser) {
		this(id, ttvUser.getHelixUser().getDisplayName(), ttvUser);
	}
	
	public Player(ObjectId id, String username, TtvUser ttvUser) {
		this.id = id;
		this.username = username;
		this.ttvUser = ttvUser;
	}
	//endregion
	
	@Override
	public int compareTo(@NotNull Object o) {
		return ((Player) o).getId().compareTo(id);
	}
}
