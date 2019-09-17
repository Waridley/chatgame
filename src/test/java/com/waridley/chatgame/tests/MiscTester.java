/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.tests;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.waridley.chatgame.api.ttv_chat_client.TwitchChatGameClient;

import java.io.*;
import java.util.Arrays;

public class MiscTester {
	
	public static void main(String[] args) throws IOException {
		String clientId = args[0];
		String clientSecret = args[1];
		
		TwitchClient twitchClient = TwitchClientBuilder.builder().withEnableHelix(true).build();
		UserList resultList = twitchClient.getHelix().getUsers(null, null, Arrays.asList("WaridleyTestBot")).execute();
		User botAcctUser = resultList.getUsers().get(0);
		
		TwitchChatGameClient gameClient = new TwitchChatGameClient(String.valueOf(botAcctUser.getId()), clientId, clientSecret, "waridley");
	}
	
}
