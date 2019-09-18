/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.ConnectionString;
import com.waridley.chatgame.api.ttv_chat_client.TwitchChatGameClient;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.backend.mongo.MongoBackend;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;

import java.util.Arrays;

public class Launcher {
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public static void launch(String[] args) {
		String channelName = args[0];
		String botAcctName = args[1];
		TwitchIdentityProvider idProvider = new TwitchIdentityProvider(args[2], args[3], "http://localhost:6464");
		ConnectionString connectionString = new ConnectionString(args[4]);
		OAuth2Credential ttvCred = new OAuth2Credential("twitch", args[5]);
		
		long intervalMinutes = 6;
		
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withEventManager(new EventManager())
				.withEnableHelix(true)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		StorageInterface storageInterface = new MongoBackend(connectionString, twitchClient.getHelix(), idProvider);
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, storageInterface, channelName, ttvCred, intervalMinutes);
		logger.start();
		
		UserList resultList = twitchClient.getHelix().getUsers(null, null, Arrays.asList(botAcctName)).execute();
		User botAcctUser = resultList.getUsers().get(0);
		
		TwitchChatGameClient gameClient = new TwitchChatGameClient(idProvider,"waridley", storageInterface, twitchClient);
		
	}
}
