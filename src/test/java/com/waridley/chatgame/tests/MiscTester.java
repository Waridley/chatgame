/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.tests;


import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.Launcher;
import com.waridley.chatgame.backend.NamedOAuth2Credential;
import com.waridley.chatgame.backend.mongo.MongoCredentialBackend;

import java.util.ArrayList;
import java.util.List;

public class MiscTester {
	
	public static void main(String[] args) {
		test(args);
	}
	
	public static void test(String[] args) {
		Launcher.launch(args);
		//testCredentialBackend(args[4]);
	}
	
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
