/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.backend.DesktopAuthController;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.backend.mongo.MongoCredentialBackend;
import com.waridley.chatgame.backend.mongo.MongoGameBackend;
import com.waridley.chatgame.backend.TtvStorageInterface;
import com.waridley.chatgame.backend.mongo.MongoTtvBackend;
import com.waridley.chatgame.clients.GameClient;
import com.waridley.chatgame.clients.discord_client.DummyDiscordClient;
import com.waridley.chatgame.backend.RefreshingProvider;
import com.waridley.chatgame.clients.ttv_chat_client.TwitchChatGameClient;
import com.waridley.chatgame.game.Game;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;

import java.util.ArrayList;
import java.util.List;

public class Launcher {
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}
	
	public static void launch(String[] args) throws Exception {
		String channelName = args[0];
		RefreshingProvider idProvider = new RefreshingProvider(args[2], args[3], "http://localhost:6464");
		MongoDatabase db = connectToDatabase(args[4]);
		OAuth2Credential twitchCredential = new OAuth2Credential("twitch", args[5]);
		
		IStorageBackend credBackend = new MongoCredentialBackend(db, "credentials");
		
		CredentialManager credentialManager = CredentialManagerBuilder.builder()
				.withAuthenticationController(new DesktopAuthController("http://localhost:6464/info.html"))
				.withStorageBackend(credBackend)
				.build();
		credentialManager.registerIdentityProvider(idProvider);
		
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withClientId(args[2])
				.withClientSecret(args[3])
				.withRedirectUrl("http://localhost:6464")
				.withCredentialManager(credentialManager)
				//.withEnableChat(true)
				.withEnableHelix(true)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		TtvStorageInterface ttvBackend = new MongoTtvBackend(db, twitchClient);
		
		
		startWatchtimeLogger(channelName, idProvider, ttvBackend, twitchClient, twitchCredential, 6L);
		
		loadClients(credentialManager.getOAuth2IdentityProviderByName("twitch").get(), channelName, ttvBackend);
		
		GameStorageInterface gameBackend = new MongoGameBackend(db);
		Game game = new Game(gameBackend);
	}
	
	private static void loadClients(OAuth2IdentityProvider idProvider, String channelName, TtvStorageInterface twitchStorage) {
		List<GameClient> gameClients = new ArrayList<>();
		gameClients.add(new TwitchChatGameClient(idProvider, channelName, twitchStorage));
		gameClients.add(new DummyDiscordClient(idProvider, twitchStorage));
		
		for(GameClient gameClient : gameClients) {
			gameClient.start();
		}
	}
	
	private static void startWatchtimeLogger(String channelName,
	                                         OAuth2IdentityProvider identityProvider,
	                                         TtvStorageInterface storageInterface,
	                                         TwitchClient twitchClient,
	                                         OAuth2Credential twitchCredenial,
	                                         long intervalMinutes) {
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, storageInterface, channelName, twitchCredenial, intervalMinutes);
		logger.start();
	}
	
	public static MongoDatabase connectToDatabase(String connectionURI) {
		ConnectionString connStr = new ConnectionString(connectionURI);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connStr)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		return mongoClient.getDatabase("chatgame");
	}
}
