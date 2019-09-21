/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.waridley.chatgame.backend.NamedOAuth2Credential;
import com.waridley.chatgame.backend.TwitchStorageInterface;
import com.waridley.chatgame.clients.GameClient;

import java.util.*;

public class TwitchChatGameClient implements GameClient {
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private IStorageBackend credentialStorage;
	private TwitchStorageInterface twitchStorage;
	
	private EventManager eventManager;
	private TwitchClient twitchClient;
	
	private CredentialManager credentialManager;
	private TwitchIdentityProvider identityProvider;
	
	public TwitchChatGameClient(
			TwitchIdentityProvider provider,
			String channelName,
			IStorageBackend credentialStorage,
			TwitchStorageInterface twitchStorageInterface,
			TwitchClient twitchClient) {
		
		this.channelName = channelName;
		this.credentialStorage = credentialStorage;
		this.twitchStorage = twitchStorageInterface;
		this.twitchClient = twitchClient;
		this.identityProvider = provider;
		this.eventManager = twitchClient.getEventManager();
		
	}
	
	public void start() {
		System.out.println("Starting Twitch Chat Game Client");
		waitForCredential();
	}
	
	private void waitForCredential() {
		List<Credential> credentials = credentialStorage.loadCredentials();
		Optional<NamedOAuth2Credential> botCredOpt = Optional.empty();
		for(Credential c : credentials) {
			if(c instanceof NamedOAuth2Credential) {
				if(((NamedOAuth2Credential) c).getName().equals("boCredential")) botCredOpt = Optional.of((NamedOAuth2Credential) c);
			}
		}
		
		if(botCredOpt.isPresent()) {
			System.out.println("Found bot credential.");
			buildClient(botCredOpt.get().getCredential());
		} else {
			System.out.println("No saved bot credential found. Starting OAuth2 Authorization Code Flow.");
			ChatbotAuthController authController = new ChatbotAuthController(this::buildClient);
			
			credentialManager = CredentialManagerBuilder.builder()
					.withAuthenticationController(authController)
					.build();
			authController.setCredentialManager(credentialManager);
			credentialManager.registerIdentityProvider(identityProvider);
			
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
	
	private void buildClient(OAuth2Credential credential) {
		Optional<OAuth2Credential> c = identityProvider.getAdditionalCredentialInformation(credential);
		if(c.isPresent()) credential = c.get();
		NamedOAuth2Credential botCredential = new NamedOAuth2Credential("botCredential", credential);
		credentialStorage.saveCredentials(Collections.singletonList(botCredential));
		System.out.println("Saved bot credential for " + credential.getUserName());
		twitchChat = TwitchChatBuilder.builder()
				.withEventManager(eventManager)
				.withCredentialManager(credentialManager)
				.withChatAccount(credential)
				.build();
		twitchChat.joinChannel(channelName);
		System.out.println("Joined channel: " + channelName);
		twitchChat.sendMessage(channelName, "I'm here! TwitchRPG");
		
		CommandDispatcher commandDispatcher = new CommandDispatcher(eventManager);
		CommandHandler commandHandler = new CommandHandler(eventManager, twitchStorage);
	}
	
	
	
}
