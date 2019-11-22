/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.server;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.api.frontend.GameClient;
import com.waridley.chatgame.mongo.MongoGameBackend;
import com.waridley.chatgame.ttv_chat_client.TwitchChatGameClient;
import com.waridley.credentials.DesktopAuthController;
import com.waridley.credentials.mongo.MongoCredentialStorageBackend;
import com.waridley.ttv.TtvStorageInterface;
import com.waridley.ttv.mongo.MongoTtvBackend;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LauncherJ {
	
	private static String channelName;
	private static OAuth2IdentityProvider idProvider;
	private static MongoDatabase db;
	private static OAuth2Credential twitchCredential;
	private static IStorageBackend credBackend;
	private static CredentialManager credentialManager;
	private static TwitchClient twitchClient;
	private static TtvStorageInterface ttvBackend;
	private static GameStorageInterface gameBackend;
	private static ServerOptions serverOptions;
	
	public static void main(String[] args) throws Exception {
		Properties props = System.getProperties();
		props.put("org.slf4j.simpleLogger.defaultLogLevel", "warn");
		props.put("org.slf4j.simpleLogger.showThreadName", "false");
		props.put("org.slf4j.simpleLogger.showLogName", "false");
		props.put("org.slf4j.simpleLogger.showShortLogName", "true");
		props.put("org.slf4j.simpleLogger.log." + TwitchChatGameClient.class.getName(), "trace");
		
		System.setProperties(props);
		
		serverOptions = ServerOptions.fromArgs(args);
		init(args);
		launch();
	}
	
	public static void launch() throws Exception {
		
		//startWatchtimeLogger(6L);
		GameServer server = new GameServer(ttvBackend, gameBackend, serverOptions);
		server.start();
		loadClients(idProvider, channelName, server.commandExecutive);
		
	}
	
	private static void loadClients(OAuth2IdentityProvider idProvider, String channelName, CommandExecutive exec) {
		List<GameClient> gameClients = new ArrayList<>();
		gameClients.add(new TwitchChatGameClient(idProvider, channelName, new EmbeddedCommandMediator(exec)));
//		gameClients.add(new DummyDiscordClient(idProvider, ttvBackend));

		for(GameClient gameClient : gameClients) {
			gameClient.start();
		}
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
	
	private static void init(String... args) throws URISyntaxException {
		channelName = args[0];
		serverOptions.embeddedChatClientOptions.channelName = channelName;
		idProvider = new TwitchIdentityProvider(args[2], args[3], "http://localhost:6464");
		serverOptions.embeddedChatClientOptions.identityProvider = idProvider;
		db = connectToDatabase(args[4]);
		twitchCredential = new OAuth2Credential("twitch", args[5]);
		
		credBackend = new MongoCredentialStorageBackend(db, "credentials");
		
		credentialManager = CredentialManagerBuilder.builder()
				.withAuthenticationController(new DesktopAuthController("http://localhost:6464/info.html"))
				.withStorageBackend(credBackend)
				.build();
		credentialManager.registerIdentityProvider(idProvider);
		
		twitchClient = TwitchClientBuilder.builder()
				.withClientId(args[2])
				.withClientSecret(args[3])
				.withRedirectUrl("http://localhost:6464")
				.withCredentialManager(credentialManager)
				.withEnableHelix(true)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		ttvBackend = new MongoTtvBackend(db, twitchClient.getHelix(), twitchCredential);
		gameBackend = new MongoGameBackend(db, twitchClient.getHelix(), twitchCredential);
		
	}
	
}
