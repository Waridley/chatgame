/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.ttv_integration.TtvUser;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Optional;

public interface TtvStorageInterface {
	
	TwitchClient getTwitchClient();
	
//	@Deprecated
//	Optional<NamedOAuth2Credential> loadNamedCredential(String name);
//
//	@Deprecated
//	void saveNamedCredential(String name, OAuth2Credential credential);
//
//	IStorageBackend getCredentialStorageBackend();
	
//	TwitchUser findOrCreateTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException;
//	TwitchUser findOrCreateTwitchUser(String ttvLogin) throws TwitchUser.UserNotFoundException;
//	TwitchUser findOrCreateTwitchUser(User user);
	
	TtvUser findOrCreateTtvUser(long ttvUserId);
	TtvUser findOrCreateTtvUser(String ttvLogin);
	TtvUser findOrCreateTtvUser(User user);
	
//	TwitchUser findTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException;
//	TwitchUser findTwitchUser(String ttvLogin) throws TwitchUser.UserNotFoundException;
//	TwitchUser findTwitchUser(User user) throws TwitchUser.UserNotFoundException;
	
	Optional<TtvUser> findTtvUser(long ttvUserId);
	Optional<TtvUser> findTtvUser(String ttvLogin);
	Optional<TtvUser> findTtvUser(User user);
	
//	TwitchUser logMinutes(TwitchUser user, long minutes, boolean online);
	
	TtvUser logMinutes(TtvUser user, long minutes, boolean online);
}
