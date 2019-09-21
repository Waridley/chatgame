package com.waridley.chatgame.backend;

import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.types.ObjectId;

public interface GameStorageInterface {
	
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param twitchUser The Twitch account corresponding to the requested Player.
	 * @return The Player if it exists, otherwise a newly created Player linked to the TwitchUser.
	 */
	Player findOrCreatePlayer(TwitchUser twitchUser);
	
	/**
	 * Find a Player record, or create a new account if none exists.
	 *
	 * @param gameUsername The current in-game username for the Player.
	 * @return The Player if it exists, otherwise a newly created Player with the given username.
	 */
	Player findOrCreatePlayer(String gameUsername);
	
	Player findOrCreatePlayer(ObjectId id);
	
	
	Player findPlayer(TwitchUser twitchUser);
	Player findPlayer(String gameUsername);
	Player findPlayer(long userId);
	
	
	
}
