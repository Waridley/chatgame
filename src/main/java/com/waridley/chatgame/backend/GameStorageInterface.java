package com.waridley.chatgame.backend;

import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TtvUser;
import org.bson.types.ObjectId;

import java.util.Optional;

public interface GameStorageInterface {
	
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param twitchUser The Twitch account corresponding to the requested Player.
	 * @return The Player if it exists, otherwise a newly created Player linked to the TwitchUser.
	 */
	Player findOrCreatePlayer(TtvUser ttvUser);
	
	Player findOrCreatePlayer(long ttvUserId);
	
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
	
	Player logMintues(Player player, long minutes, boolean online);
	
	
	Player savePlayer(Player player);
	
}
