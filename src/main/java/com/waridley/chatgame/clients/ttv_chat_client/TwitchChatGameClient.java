/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.waridley.chatgame.backend.twitch.TwitchStorageInterface;
import com.waridley.chatgame.backend.mongo.NamedCredential;
import com.waridley.chatgame.clients.GameClient;
import com.waridley.chatgame.ttv_integration.LambdaAuthenticationController;

import java.util.Arrays;
import java.util.Optional;

public class TwitchChatGameClient implements GameClient {
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private TwitchStorageInterface storageInterface;
	
	private EventManager eventManager;
	private TwitchClient twitchClient;
	
	private CredentialManager credentialManager;
	TwitchIdentityProvider identityProvider;
	
	public TwitchChatGameClient(
			TwitchIdentityProvider provider,
			String channelName,
			TwitchStorageInterface storageInterface,
			TwitchClient twitchClient) {
		
		this.channelName = channelName;
		this.storageInterface = storageInterface;
		this.twitchClient = twitchClient;
		this.identityProvider = provider;
		
	}
	
	private void buildClient(OAuth2Credential credential) {
		Optional<OAuth2Credential> enrichedCred = identityProvider.getAdditionalCredentialInformation(credential);
		if(enrichedCred.isPresent()) { credential = enrichedCred.get(); }
		storageInterface.saveAdminCredential("botCredential", credential);
		System.out.println("Saved bot credential for " + credential.getUserName());
		twitchChat = TwitchChatBuilder.builder()
				.withEventManager(eventManager)
				.withCredentialManager(credentialManager)
				.withChatAccount(credential)
				.build();
		System.out.println("Built TwitchChat");
		twitchChat.joinChannel(channelName);
		System.out.println("Joined channel:" + channelName);
		twitchChat.sendMessage(channelName, "I'm here! TwitchRPG");
		
		CommandDispatcher commandDispatcher = new CommandDispatcher(twitchChat.getEventManager());
		CommandHandler commandHandler = new CommandHandler(twitchClient, storageInterface);
	}
	
	public void start() {
		System.out.println("Starting Twitch Chat Game Client");
		eventManager = twitchClient.getEventManager();
		
		LambdaAuthenticationController authController = new LambdaAuthenticationController(this::buildClient);
		
		credentialManager = CredentialManagerBuilder.builder()
				.withAuthenticationController(authController)
				.build();
		authController.setCredentialManager(credentialManager);
		credentialManager.registerIdentityProvider(identityProvider);
		
		Optional<NamedCredential> botCredential = storageInterface.loadNamedCredential("botCredential");
		if(botCredential.isPresent()) {
			Optional<OAuth2Credential> c = identityProvider.getAdditionalCredentialInformation(botCredential.get().getCredential());
			c.ifPresent(credential -> botCredential.get().setCredential(credential));
			System.out.println("Found bot credential for " + botCredential.get().getCredential().getUserName());
			buildClient(botCredential.get().getCredential());
		} else {
			credentialManager.getAuthenticationController().startOAuth2AuthorizationCodeGrantType(
					identityProvider,
					"http://localhost:6464",
					Arrays.asList(
							TwitchScopes.CHAT_CHANNEL_MODERATE,
							TwitchScopes.CHAT_EDIT,
							TwitchScopes.CHAT_READ,
							TwitchScopes.CHAT_WHISPERS_EDIT,
							TwitchScopes.CHAT_WHISPERS_READ
					));
		}
	}
	
}
