/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.waridley.chatgame.backend.NamedOAuth2Credential;
import com.waridley.chatgame.backend.RefreshingProvider;
import com.waridley.chatgame.backend.TtvStorageInterface;
import com.waridley.chatgame.clients.GameClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TwitchChatGameClient implements GameClient {
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private IStorageBackend credentialStorage;
	private TtvStorageInterface twitchStorage;
	
	private EventManager eventManager;
	
	private CredentialManager credentialManager;
	private OAuth2IdentityProvider identityProvider;
	
	
	public TwitchChatGameClient(
			OAuth2IdentityProvider provider,
			String channelName,
			TtvStorageInterface twitchStorageInterface) {
		
		this.credentialManager = provider.getCredentialManager();
		this.channelName = channelName;
		this.credentialStorage = provider.getCredentialManager().getStorageBackend();
		this.twitchStorage = twitchStorageInterface;
		this.identityProvider = provider;
		eventManager = twitchStorageInterface.getTwitchClient().getEventManager();
		CommandDispatcher commandDispatcher = new CommandDispatcher(eventManager);
		CommandHandler commandHandler = new CommandHandler(eventManager, twitchStorage);
		
	}
	
	public void start() {
		System.out.println("Starting Twitch Chat Game Client");
		try {
			waitForCredential();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void waitForCredential() throws IOException {
		List<Credential> credentials = credentialStorage.loadCredentials();
		Optional<NamedOAuth2Credential> botCredOpt = Optional.empty();
		for(Credential c : credentials) {
			if(c instanceof NamedOAuth2Credential) {
				if(((NamedOAuth2Credential) c).getName().equals("botCredential")) botCredOpt = Optional.of((NamedOAuth2Credential) c);
			}
		}
		
		if(botCredOpt.isPresent()) {
			System.out.println("Found bot credential.");
			OAuth2Credential credential = botCredOpt.get().getCredential();
			Optional<OAuth2Credential> refreshedCredOpt = Optional.ofNullable(((RefreshingProvider) identityProvider).refreshCredential(credential));
			if(refreshedCredOpt.isPresent()) {
				credential = refreshedCredOpt.get();
				System.out.println("Successfully refreshed token");
			}
			buildClient(credential);
		} else {
			System.out.println("No saved bot credential found. Starting OAuth2 Authorization Code Flow.");
//			ChatbotAuthController authController = new ChatbotAuthController(this::buildClient);
//
//			credentialManager = CredentialManagerBuilder.builder()
//					.withAuthenticationController(authController)
//					.build();
//			authController.setCredentialManager(credentialManager);
//			credentialManager.registerIdentityProvider(identityProvider);
//
			HttpServer server = HttpServer.create(new InetSocketAddress(6464), 0);
			server.createContext("/", new RedirectHandler(this::onReceivedCode));
			server.createContext("/info.html", this::handleInfoPage);
			server.start();
			
			
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
	
	private void handleInfoPage(HttpExchange exchange) throws IOException {
		String authUrl = "";
		URI reqURI = exchange.getRequestURI();
		String[] queryParams = reqURI.getQuery().split("&");
		for(String param : queryParams) {
			if(param.startsWith("authurl=")) {
				authUrl = param.replaceFirst("authurl=", "");
				authUrl = URLDecoder.decode(authUrl, StandardCharsets.UTF_8.toString());
			}
		}
		String response =
				"<html>" +
						"<head>" +
						"</head>" +
						"<body>" +
						"<h1>Log in to your desired chat bot account</h1>" +
						"The following link will take you to the Twitch authentication page to log in.<br>" +
						"If you do not want to use your main account for the chat bot, you can either:<br>" +
						"<p style=\"margin-left: 40px\">1) Click \"Not you?\" on that page, however, this will permanently change the account you are logged into on Twitch until you manually switch back.</p>" +
						"<p style=\"margin-left: 40px\">2) Right-click this link, and open it in a private/incognito window. This will allow you to stay logged in to Twitch on your main account in normal browser windows.</p>" +
						"<a href=" + authUrl + ">" + authUrl + "</a>" +
						"</body>" +
						"</html>";
		exchange.sendResponseHeaders(200, response.length());
		exchange.getResponseBody().write(response.getBytes());
		exchange.getResponseBody().close();
	}
	
	private void onReceivedCode(String code) {
		//System.out.println("Received code " + code + " -- Getting token");
		OAuth2Credential cred = identityProvider.getCredentialByCode(code);
		//System.out.println("Token: " + cred.getAccessToken());
		identityProvider.getCredentialManager().addCredential("twitch", cred);
		buildClient(cred);
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
		
	}
	
	
	
}
