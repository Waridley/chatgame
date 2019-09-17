/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend;

import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.ttv_integration.TwitchUser;

public interface StorageInterface {
	
	TwitchUser findOrCreateTwitchUser(User user);
	
	TwitchUser logMinutes(TwitchUser user, long minutes, boolean online);
	
}
