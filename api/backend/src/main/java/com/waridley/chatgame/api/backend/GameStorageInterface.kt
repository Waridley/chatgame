package com.waridley.chatgame.api.backend;

import com.waridley.chatgame.game.Player;
import com.waridley.ttv.TtvUser;
import org.bson.types.ObjectId;

import java.util.Optional;

public interface GameStorageInterface {
	
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param ttvUser The Twitch account corresponding to the requested Player.
	 * @return The Player if it exists, otherwise a newly created Player linked to the TwitchUser.
	 */
	Player findOrCreatePlayer(TtvUser ttvUser);
	
	Player findOrCreatePlayerByTtvId(String ttvUserId);
	
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param gameUsername The current in-game username for the Player.
	 * @return The Player if it exists, otherwise a newly created Player with the given username.
	 */
	Player findOrCreatePlayer(String gameUsername);
	
	Optional<Player> findPlayer(TtvUser ttvUser);
	Optional<Player> findPlayer(ObjectId id);
	Optional<Player> findPlayer(String gameUsername);
	
	Player logMinutes(Player player, long minutes, boolean online);
	
	
	Player savePlayer(Player player);
	
}
