/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.ConnectionString;
import com.waridley.chatgame.api.ttv_chat_client.TwitchChatGameClient;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.backend.mongo.MongoBackend;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;

import java.util.Arrays;

public class Launcher {
	private static String botAcctName;
	private static StorageInterface storageInterface;
	private static TwitchClient twitchClient;
	
	public static void main(String[] args) {
		String channelName = args[0];
		botAcctName = args[1];
		String clientId = args[2];
		String clientSecret = args[3];
		ConnectionString connectionString = new ConnectionString(args[4]);
		OAuth2Credential ttvCred = new OAuth2Credential("twitch", args[5]);
		
		long intervalMinutes = 6;
		
		twitchClient = TwitchClientBuilder.builder()
				.withEventManager(new EventManager())
				.withEnableHelix(true)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		storageInterface = new MongoBackend(connectionString);
		
		startChatClient(clientId, clientSecret);
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, storageInterface, channelName, ttvCred, intervalMinutes);
		logger.start();
		
		//GameClient gameClient = new TwitchChatGameClient(ttvCred);
	}
	
	//TODO Clean up and move
	private static void startChatClient(String clientId, String clientSecret) {
		
		TwitchClient twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).build();
		UserList resultList = twitchClient.getHelix().getUsers(null, null, Arrays.asList(botAcctName)).execute();
		User botAcctUser = resultList.getUsers().get(0);
		
		TwitchChatGameClient gameClient = new TwitchChatGameClient(String.valueOf(botAcctUser.getId()), clientId, clientSecret, "waridley", storageInterface, twitchClient);
		
	}
	
}
