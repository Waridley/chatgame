/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.api.ttv_chat_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.waridley.chatgame.api.GameClient;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.ttv_integration.ReflexiveAuthenticationController;

import java.util.Arrays;

public class TwitchChatGameClient implements GameClient {
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private StorageInterface storageInterface;
	
	private EventManager eventManager;
	private TwitchClient twitchClient;
	
	private CredentialManager credentialManager;
	
	public TwitchChatGameClient(TwitchIdentityProvider provider, String channelName, StorageInterface storageInterface, TwitchClient twitchClient) {
		this.channelName = channelName;
		this.storageInterface = storageInterface;
		this.twitchClient = twitchClient;
		
		eventManager = twitchClient.getEventManager();
		
		ReflexiveAuthenticationController authController = new ReflexiveAuthenticationController(this::buildClient);
		
		credentialManager = CredentialManagerBuilder.builder()
				.withAuthenticationController(authController)
				.build();
		authController.setCredentialManager(credentialManager);
		credentialManager.registerIdentityProvider(provider);
		credentialManager.getAuthenticationController().startOAuth2AuthorizationCodeGrantType(
				provider,
				"http://localhost:6464",
				Arrays.asList(new TwitchScopes[]{
						TwitchScopes.CHAT_CHANNEL_MODERATE,
						TwitchScopes.CHAT_EDIT,
						TwitchScopes.CHAT_READ,
						TwitchScopes.CHAT_WHISPERS_EDIT,
						TwitchScopes.CHAT_WHISPERS_READ
				}));
		
	}
	
	private void buildClient(OAuth2Credential credential) {
		twitchChat = TwitchChatBuilder.builder()
				.withEventManager(eventManager)
				.withCredentialManager(credentialManager)
				.withChatAccount(credential)
				.build();
		System.out.println("Built TwitchChat");
		twitchChat.joinChannel(channelName);
		System.out.println("Joined channel:");
		System.out.println(channelName);
		twitchChat.sendMessage(channelName, "I'm here! TwitchRPG");
		
		CommandDispatcher commandDispatcher = new CommandDispatcher(twitchChat.getEventManager());
		CommandHandler commandHandler = new CommandHandler(twitchClient, storageInterface);
	}
	
}
