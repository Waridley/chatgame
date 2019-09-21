/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Optional;

public interface TwitchStorageInterface {
	
	//IStorageBackend getCredentialStorageBackend();
	
	@Deprecated
	Optional<NamedOAuth2Credential> loadNamedCredential(String name);
	
	@Deprecated
	void saveNamedCredential(String name, OAuth2Credential credential);
	
	TwitchUser findOrCreateTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(String ttvLogin) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(User user);
	
	TwitchUser findTwitchuser(User user) throws TwitchUser.UserNotFoundException;
	TwitchUser findTwitchuser(long ttvUserId) throws TwitchUser.UserNotFoundException;
	TwitchUser findTwitchuser(String ttvLogin) throws TwitchUser.UserNotFoundException;
	
	TwitchUser logMinutes(TwitchUser user, long minutes, boolean online);
	
}
