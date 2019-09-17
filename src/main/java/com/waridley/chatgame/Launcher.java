/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.mongodb.ConnectionString;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.backend.mongo.MongoBackend;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;

public class Launcher {
	
	public static void main(String[] args) {
		String channelName = args[0];
		OAuth2Credential ttvCred = new OAuth2Credential("twitch", args[1]);
		ConnectionString connectionString = new ConnectionString(args[2]);
		
		long intervalMinutes = 6;
		
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withEnableChat(true)
				.withChatAccount(ttvCred)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		StorageInterface mongoBackend = new MongoBackend(connectionString);
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, mongoBackend, channelName, ttvCred, intervalMinutes);
		logger.start();
	}
	
}
