/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.twitch;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.backend.mongo.NamedCredential;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Optional;

public interface TwitchStorageInterface {
	
	IStorageBackend getCredentialStorageBackend();
	Optional<NamedCredential> loadNamedCredential(String name);
	void saveAdminCredential(String name, OAuth2Credential credential);
	
	TwitchUser findOrCreateTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(String ttvLogin) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(User user);
	
	TwitchUser find(User user) throws TwitchUser.UserNotFoundException;
	TwitchUser find(long ttvUserId) throws TwitchUser.UserNotFoundException;
	TwitchUser find(String ttvLogin) throws TwitchUser.UserNotFoundException;
	
	TwitchUser logMinutes(TwitchUser user, long minutes, boolean online);
	
}
