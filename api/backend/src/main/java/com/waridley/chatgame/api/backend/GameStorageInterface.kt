package com.waridley.chatgame.api.backend

import com.waridley.chatgame.game.Player
import com.waridley.ttv.TtvUser
import org.bson.types.ObjectId
import java.util.*

interface GameStorageInterface {
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param ttvUser The Twitch account corresponding to the requested Player.
	 * @return The Player if it exists, otherwise a newly created Player linked to the TwitchUser.
	 */
	fun findOrCreatePlayer(ttvUser: TtvUser?): Player?
	
	fun findOrCreatePlayerByTtvId(ttvUserId: String?): Player?
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param gameUsername The current in-game username for the Player.
	 * @return The Player if it exists, otherwise a newly created Player with the given username.
	 */
	fun findOrCreatePlayer(gameUsername: String?): Player?
	
	fun findPlayer(ttvUser: TtvUser?): Player?
	fun findPlayer(id: ObjectId?): Player?
	fun findPlayer(gameUsername: String?): Player?
	fun logMinutes(player: Player?, minutes: Long, online: Boolean): Player?
	fun savePlayer(player: Player?): Player?
}