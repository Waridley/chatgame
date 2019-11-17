/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.game

import com.waridley.chatgame.game.inventory.Backpack
import com.waridley.ttv.TtvUser
import org.bson.types.ObjectId

/* TODO:
 *  Add currency field
 *  Implement skills
 *  Implement inventory
 *  Implement bank
 *  Implement items
 */
class Player @JvmOverloads constructor(id: ObjectId?, username: String? = null, ttvUser: TtvUser? = null) : Comparable<Any?> {
	var id: ObjectId? = null
		private set
	
	private fun setId(id: ObjectId) {
		this.id = id
	}
	
	var username: String? = null
		private set
	
	private fun setUsername(username: String) {
		this.username = username
	}
	
	fun changeUsername(username: String, really: Boolean) {
		if (really) {
			setUsername(username)
		}
	}
	
	var ttvUser: TtvUser? = null
		private set
	
	private fun setTtvUser(ttvUser: TtvUser) {
		if (username == null || username.equals(ttvUser.helixUser.displayName, ignoreCase = true)) setUsername(ttvUser.helixUser.displayName)
		this.ttvUser = ttvUser
		//		this.twitchUserId = ttvUser.getId();
	}
	
	fun changeTwitchAccount(ttvUser: TtvUser, changeUsername: Boolean) {
		changeUsername(ttvUser.helixUser.displayName, changeUsername)
		setTtvUser(ttvUser)
	}
	
	val backpack = Backpack()
	
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
	protected constructor() : this(ObjectId()) {}
	
	constructor(ttvUser: TtvUser) : this(ObjectId(), ttvUser) {}
	constructor(username: String?, ttvUser: TtvUser?) : this(ObjectId(), username, ttvUser) {}
	constructor(id: ObjectId?, ttvUser: TtvUser) : this(id, ttvUser.helixUser.displayName, ttvUser) {}
	
	//endregion
	private val unknownProperties: Map<String, Any>? = null
	
	override fun compareTo(o: Any?): Int {
		return (o as Player?)!!.id!!.compareTo(id)
	}
	
	init {
		this.id = id
		this.username = username
		this.ttvUser = ttvUser
	}
}