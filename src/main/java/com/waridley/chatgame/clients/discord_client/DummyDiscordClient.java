package com.waridley.chatgame.clients.discord_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.waridley.chatgame.backend.TwitchStorageInterface;
import com.waridley.chatgame.clients.GameClient;

public class DummyDiscordClient implements GameClient {
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private TwitchStorageInterface storageInterface;
	
	private EventManager eventManager;
	private TwitchClient twitchClient;
	
	private CredentialManager credentialManager;
	TwitchIdentityProvider identityProvider;
	
	public DummyDiscordClient(
			TwitchIdentityProvider provider,
			String channelName,
			TwitchStorageInterface storageInterface,
			TwitchClient twitchClient) {
		
		this.channelName = channelName;
		this.storageInterface = storageInterface;
		this.twitchClient = twitchClient;
		this.identityProvider = provider;
		
	}
	
	@Override
	public void start() {
		System.out.println("Started dummy Discord client");
	}
	
}
