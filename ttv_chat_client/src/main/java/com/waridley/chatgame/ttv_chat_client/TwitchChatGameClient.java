/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_chat_client;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.auth.domain.TwitchScopes;
import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelJoinEvent;
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.chatgame.api.frontend.GameClient;
import com.waridley.credentials.NamedCredentialStorageBackend;
import com.waridley.ttv.RefreshingProvider;
import com.waridley.ttv.DeletableChannelMessageEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class TwitchChatGameClient implements GameClient {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(TwitchChatGameClient.class);
	private String channelName;
	
	private TwitchChat twitchChat;
	public TwitchChat getTwitchChat() { return twitchChat; }
	
	private NamedCredentialStorageBackend credentialStorage;
	private CommandMediator commandMediator;
	
	private CredentialManager credentialManager;
	private OAuth2IdentityProvider identityProvider;
	
	
	public TwitchChatGameClient(
			OAuth2IdentityProvider provider,
			String channelName,
			CommandMediator commandMediator) {
		
		this.credentialManager = provider.getCredentialManager();
		this.channelName = channelName;
		this.credentialStorage = (NamedCredentialStorageBackend) provider.getCredentialManager().getStorageBackend();
		this.identityProvider = provider;
		this.commandMediator = commandMediator;
		
	}
	
	public void start() {
		log.info("Starting Twitch Chat Game Client");
		waitForCredential();
	}
	
	private void waitForCredential() {
//		List<Credential> credentials = credentialStorage.loadCredentials();
//		Optional<NamedOAuth2Credential> botCredOpt = Optional.empty();
//		for(Credential c : credentials) {
//			if(c instanceof NamedOAuth2Credential) {
//				if(((NamedOAuth2Credential) c).getName().equals("botCredential")) botCredOpt = Optional.of((NamedOAuth2Credential) c);
//			}
//		}
		
		Optional<Credential> botCredOpt = credentialStorage.getCredentialByName("botCredential");
		
		if(botCredOpt.isPresent()) {
			log.info("Found bot credential.");
			OAuth2Credential credential = (OAuth2Credential) botCredOpt.get();
			Optional<OAuth2Credential> refreshedCredOpt = ((RefreshingProvider) identityProvider).refreshCredential(credential);
			if(refreshedCredOpt.isPresent()) {
				credential = refreshedCredOpt.get();
				log.info("Successfully refreshed token");
			} else {
				System.err.println("Failed to refresh token. Delete the credential from storage to generate a new one.");
			}
			buildChat(credential);
		} else {
			log.info("No saved bot credential found. Starting OAuth2 Authorization Code Flow.");
			try {
				getNewCredential();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getNewCredential() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(6464), 0);
		server.createContext("/", this::onReceivedCode);
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
	
	private void onReceivedCode(HttpExchange exchange) {
		String code = null;
		
		URI uri = exchange.getRequestURI();
		String response = "<html>" +
				"<head>" +
				"</head>" +
				"<body>" +
				"<h1>Success!</h1>" +
				"Received authorization code. Getting token and joining chat." +
				"</body>" +
				"</html>";
		try {
			exchange.sendResponseHeaders(200, response.length());
			exchange.getResponseBody().write(response.getBytes());
			exchange.getResponseBody().close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		String query = uri.getQuery();
		String[] splitQuery = query.split("&");
		for(String s : splitQuery) {
			String[] splitField = s.split("=");
			if(splitField[0].equals("code")) {
				code = splitField[1];
				break;
			}
		}
		OAuth2Credential cred = identityProvider.getCredentialByCode(code);
		identityProvider.getCredentialManager().addCredential("twitch", cred);
		buildChat(cred);
	}
	
	private void buildChat(OAuth2Credential credential) {
		Optional<OAuth2Credential> c = identityProvider.getAdditionalCredentialInformation(credential);
		if(c.isPresent()) credential = c.get();
//		NamedOAuth2Credential botCredential = new NamedOAuth2Credential("botCredential", credential);
		credentialStorage.saveCredential("botCredential", credential);
		log.info("Saved bot credential for " + credential.getUserName());
		twitchChat = TwitchChatBuilder.builder()
				.withCredentialManager(credentialManager)
				.withChatAccount(credential)
				.build();
		twitchChat.joinChannel(channelName);
		
		
		CommandDispatcher commandDispatcher = new CommandDispatcher(twitchChat.getEventManager());
		CommandParser commandParser = new CommandParser(twitchChat.getEventManager(), commandMediator);
		twitchChat.getEventManager().onEvent(ChannelJoinEvent.class).subscribe(this::userJoined);
		twitchChat.getEventManager().onEvent(ChannelLeaveEvent.class).subscribe(this::userLeft);
		
		log.info("Joined channel: " + channelName);
		twitchChat.sendMessage(channelName, "I'm here! TwitchRPG");
		
	}
	
	
	private void userJoined(ChannelJoinEvent event) {
		//log.info(event.getUser().getName() + " just joined this channel!");
	}
	
	private void userLeft(ChannelLeaveEvent event) {
//		log.info("Someone just left this channel snowpoSOB");
	}
	
}