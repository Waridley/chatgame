/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.backend.mongo.MongoCredentialBackend;
import com.waridley.chatgame.backend.mongo.MongoGameBackend;
import com.waridley.chatgame.backend.TwitchStorageInterface;
import com.waridley.chatgame.backend.mongo.MongoTwitchBackend;
import com.waridley.chatgame.clients.GameClient;
import com.waridley.chatgame.clients.discord_client.DummyDiscordClient;
import com.waridley.chatgame.clients.ttv_chat_client.TwitchChatGameClient;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Launcher {
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static void launch(String[] args) {
		String channelName = args[0];
		TwitchIdentityProvider idProvider = new TwitchIdentityProvider(args[2], args[3], "http://localhost:6464");
		ConnectionString connectionString = new ConnectionString(args[4]);
		OAuth2Credential twitchCredential = new OAuth2Credential("twitch", args[5]);
		
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withEventManager(new EventManager())
				.withEnableHelix(true)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		
		IStorageBackend credBackend = new MongoCredentialBackend(db, "credentials");
		TwitchStorageInterface ttvBackend = new MongoTwitchBackend(db, twitchClient.getHelix());
		GameStorageInterface gameBackend = new MongoGameBackend(db);
		
		startWatchtimeLogger(channelName, idProvider, ttvBackend, twitchClient, twitchCredential, 6L);
		
		loadClients(idProvider, channelName, credBackend, ttvBackend, twitchClient);
	}
	
	private static void loadClients(TwitchIdentityProvider idProvider, String channelName, IStorageBackend credentialStorage, TwitchStorageInterface twitchStorage, TwitchClient twitchClient) {
		List<GameClient> gameClients = new ArrayList<>();
		gameClients.add(new TwitchChatGameClient(idProvider, channelName, credentialStorage, twitchStorage, twitchClient));
		gameClients.add(new DummyDiscordClient(idProvider, channelName, twitchStorage, twitchClient));
		
		for(GameClient gameClient : gameClients) {
			gameClient.start();
		}
	}
	
	private static void startWatchtimeLogger(String channelName,
	                                         TwitchIdentityProvider identityProvider,
	                                         TwitchStorageInterface storageInterface,
	                                         TwitchClient twitchClient,
	                                         OAuth2Credential twitchCredenial,
	                                         long intervalMinutes) {
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, storageInterface, channelName, twitchCredenial, intervalMinutes);
		logger.start();
	}
}
