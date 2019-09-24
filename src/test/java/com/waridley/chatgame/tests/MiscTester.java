/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.tests;


import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.Launcher;
import com.waridley.chatgame.backend.*;
import com.waridley.chatgame.backend.mongo.MongoCredentialBackend;
import com.waridley.chatgame.backend.mongo.MongoGameBackend;
import com.waridley.chatgame.backend.mongo.MongoTtvBackend;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TtvUser;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MiscTester {
	
	public static void main(String[] args) throws Exception {
		test(args);
	}
	
	public static void test(String[] args) throws Exception {
		Launcher.launch(args);
		//testCredentialBackend(args[4]);
		//createTtvUsersFromTwitchUsers(args);
	}
	
//	private static void createTtvUsersFromTwitchUsers(String[] args) throws URISyntaxException, TwitchUser.UserNotFoundException {
//		MongoDatabase db = Launcher.connectToDatabase(args[4]);
//		RefreshingProvider idProvider = new RefreshingProvider(args[2], args[3], "http://localhost:6464");
//
//		IStorageBackend credBackend = new MongoCredentialBackend(db, "credentials");
//
//		CredentialManager credentialManager = CredentialManagerBuilder.builder()
//				.withAuthenticationController(new DesktopAuthController("http://localhost:6464/info.html"))
//				.withStorageBackend(credBackend)
//				.build();
//		credentialManager.registerIdentityProvider(idProvider);
//
//
//		TwitchClient twitchClient = TwitchClientBuilder.builder()
//				.withClientId(args[2])
//				.withClientSecret(args[3])
//				.withRedirectUrl("http://localhost:6464")
//				.withCredentialManager(credentialManager)
//				//.withEnableChat(true)
//				.withEnableHelix(true)
//				.withEnableTMI(true)
//				.build();
//		twitchClient.getClientHelper().enableStreamEventListener(args[0]);
//
//		MongoTtvBackend mongoTtvBackend = new MongoTtvBackend(db, twitchClient);
//
//		GameStorageInterface gameStorageInterface = new MongoGameBackend(db);
//
//		MongoCollection<TwitchUser> collection = mongoTtvBackend.getTwitchUserCollection();
//
//		for(TwitchUser twitchUser : collection.find()) {
//			System.out.println("Found TwitchUser " + twitchUser.getUserId() + ":" + twitchUser.getLogin());
//			if(twitchUser.getUserId() == null) twitchUser = mongoTtvBackend.findOrCreateTwitchUser(twitchUser.getLogin());
//			if(twitchUser.getHelixUser() == null) twitchUser = mongoTtvBackend.findOrCreateTwitchUser(twitchUser.getUserId());
//			TtvUser ttvUser = mongoTtvBackend.findOrCreateTtvUser(twitchUser.getUserId());
//			ttvUser.setOfflineMinutes(twitchUser.getOfflineMinutes());
//			ttvUser.setOnlineMinutes(twitchUser.getOnlineMinutes());
//			mongoTtvBackend.saveTtvUser(ttvUser);
//			Player player = twitchUser.getPlayerId() == null ? new Player(ttvUser) : new Player(twitchUser.getPlayerId(), ttvUser);
//			gameStorageInterface.savePlayer(player);
//		}
//		System.exit(0);
//
//	}
	
	public static void testCredentialBackend(String s) {
		ConnectionString connectionString = new ConnectionString(s);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		IStorageBackend storageBackend = new MongoCredentialBackend(db, "test");
		
		List<Credential> credentials = new ArrayList<>();
		credentials.add(new Credential("me", "testid") { });
		credentials.add(new OAuth2Credential("twitch", "testtoken"));
		credentials.add(new NamedOAuth2Credential("Inamedit", new OAuth2Credential("meagain", "access denied")));
		credentials.add(new OAuth2Credential("twitch", "accessToken", "refreshToken", "userid", "username", 3600, null));
		
		storageBackend.saveCredentials(credentials);
		List<Credential> result = storageBackend.loadCredentials();
		for(Credential c : result) {
			System.out.println(c.getIdentityProvider() + ":" + c.getUserId());
		}
	}
}
