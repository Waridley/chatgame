package com.waridley.chatgame.clients.discord_client;

import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.waridley.chatgame.backend.TtvStorageInterface;
import com.waridley.chatgame.clients.GameClient;

public class DummyDiscordClient implements GameClient {
	
	private TtvStorageInterface storageInterface;
	
	OAuth2IdentityProvider identityProvider;
	
	public DummyDiscordClient(
			OAuth2IdentityProvider provider,
			TtvStorageInterface storageInterface) {
		
		this.storageInterface = storageInterface;
		this.identityProvider = provider;
		
	}
	
	@Override
	public void start() {
		System.out.println("Started dummy Discord client");
	}
	
}
