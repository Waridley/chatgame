/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.domain.User;
import com.waridley.chatgame.backend.mongo.AdminCredential;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.util.Optional;

public interface StorageInterface {
	
	IStorageBackend getCredentialStorageBackend();
	Optional<AdminCredential> loadAdminCredential(String name);
	void saveAdminCredential(String name, OAuth2Credential credential);
	
	TwitchUser findOrCreateTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(String s) throws TwitchUser.UserNotFoundException;
	TwitchUser findOrCreateTwitchUser(User user);
	
	TwitchUser logMinutes(TwitchUser user, long minutes, boolean online);
	
}
