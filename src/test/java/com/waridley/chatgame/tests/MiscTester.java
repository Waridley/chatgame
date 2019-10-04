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
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.waridley.chatgame.server.Launcher;
import com.waridley.credentials.DesktopAuthController;
import com.waridley.credentials.mongo.MongoCredentialStorageBackend;
import com.waridley.chatgame.mongo.MongoGameBackend;
import com.waridley.credentials.NamedOAuth2Credential;
import com.waridley.credentials.RefreshingProvider;
import com.waridley.ttv.mongo.MongoTtvBackend;
import com.waridley.chatgame.game.Player;
import com.waridley.ttv.TtvUser;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiscTester {
	
	public static void main(String[] args) throws Exception {
		test(args);
	}
	
	public static void test(String[] args) throws Exception {
//		testSpeeds(args);
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
		IStorageBackend storageBackend = new MongoCredentialStorageBackend(db, "test");
		
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
	
//	public static void testSpeeds(String[] args) throws URISyntaxException, InterruptedException {
//		MongoDatabase db = Launcher.connectToDatabase(args[4]);
//		RefreshingProvider idProvider = new RefreshingProvider(args[2], args[3], "http://localhost:6464");
//
//		IStorageBackend credBackend = new MongoCredentialStorageBackend(db, "credentials");
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
//		OAuth2Credential credential = new OAuth2Credential("twitch", args[5]);
//
//		MongoTtvBackend mongoTtvBackend = new MongoTtvBackend(db, twitchClient.getHelix(), credential);
//
//		MongoGameBackend mongoGameBackend = new MongoGameBackend(db, twitchClient.getHelix(), credential);
//
//
//		List<String> singleUsername = Collections.singletonList("waridley");
//		List<String> usernames = new ArrayList<>();
//		List<Long> singleUserId = Collections.singletonList(43394066L);
//		List<Long> userIds = new ArrayList<>();
//		long start, end;
//		UserList userList;
//
//		MongoCollection<TtvUser> ttvUserCollection = mongoTtvBackend.getTtvUserCollection();
//		MongoCollection<Player> playerCollection = mongoGameBackend.getPlayerCollection();
//		FindIterable<TtvUser> ttvResult;
//		FindIterable<Player> playerResult;
//
//		int records = 0;
//		for(TtvUser u : ttvUserCollection.find()) {
//			usernames.add(u.getHelixUser().getLogin());
//			System.out.println("Added username " + u.getHelixUser().getLogin());
//			userIds.add(u.getId());
//			System.out.println("Added userId " + u.getId());
//			records++;
//			if(records >= 100) break;
//		}
//
//
//		for(int i = 0; i < 30; i++) {
//			start = System.currentTimeMillis();
//			userList = twitchClient.getHelix().getUsers(
//					args[5],
//					null,
//					singleUsername
//			).execute();
//			for(User u : userList.getUsers()) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Helix getUsers singleton username: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			twitchClient.getHelix().getUsers(
//					args[5],
//					singleUserId,
//					null
//			).execute();
//			for(User u : userList.getUsers()) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Helix getUsers singleton userId: " + (end - start));
//
//
//			userList = twitchClient.getHelix().getUsers(
//					args[5],
//					null,
//					usernames
//			).execute();
//			for(User u : userList.getUsers()) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Helix getUsers lots of usernames: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			twitchClient.getHelix().getUsers(
//					args[5],
//					userIds,
//					null
//			).execute();
//			for(User u : userList.getUsers()) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Helix getUsers lots of userIds: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			ttvUserCollection.find(Filters.eq("login", singleUsername)).first();
//			end = System.currentTimeMillis();
//			System.out.println("Mongo ttv find username first: " + (end - start));
//
//			start = System.currentTimeMillis();
//			ttvResult = ttvUserCollection.find(Filters.in("helixUser.login", usernames));
//			for(TtvUser ttvUser : ttvResult) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Mongo ttv find in usernames: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			ttvUserCollection.find(Filters.eq("_id", singleUserId)).first();
//			end = System.currentTimeMillis();
//			System.out.println("Mongo ttv find userId first: " + (end - start));
//
//			start = System.currentTimeMillis();
//			ttvResult = ttvUserCollection.find(Filters.in("_id", userIds));
//			for(TtvUser ttvUser : ttvResult) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Mongo ttv find in userIds: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			playerCollection.find(Filters.eq("ttvUser.helixUser.login", singleUsername)).first();
//			end = System.currentTimeMillis();
//			System.out.println("Mongo player find username first: " + (end - start));
//
//			start = System.currentTimeMillis();
//			playerResult = playerCollection.find(Filters.in("ttvUser.helixUser.login", usernames));
//			for(Player player : playerResult) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Mongo player find in usernames: " + (end - start));
//
//
//			start = System.currentTimeMillis();
//			playerCollection.find(Filters.eq("ttvUser._id", singleUserId)).first();
//			end = System.currentTimeMillis();
//			System.out.println("Mongo player find userid first: " + (end - start));
//
//			start = System.currentTimeMillis();
//			playerResult = playerCollection.find(Filters.in("ttvUser._id", userIds));
//			for(Player player : playerResult) {
//
//			}
//			end = System.currentTimeMillis();
//			System.out.println("Mongo player find in userIds: " + (end - start));
//
//
//			System.out.println("\n\n\n");
//
//			Thread.sleep(60 * 1000);
//		}
//	}
}
