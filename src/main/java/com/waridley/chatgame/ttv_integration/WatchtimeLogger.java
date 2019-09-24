/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.ttv_integration;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.waridley.chatgame.backend.TtvStorageInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* TODO:
 *  Use AuthenticationController instead of passing token to constructor
 *  Link Player object to each TwitchUser
 *  Implement currency logging
 *  Implement blacklist
 *  Methods have been made synchronized, but that doesn't make fields thread-safe
 */

public class WatchtimeLogger {
	
	private boolean online;
	
	private TwitchClient twitchClient;
	private TtvStorageInterface storageInterface;
	private String channelName;
	private OAuth2Credential tmiCredential;
	
	private List<TtvUser> usersInChat;
	public List<TtvUser> getUsersInChat() { return usersInChat; }
	
	private LoggerTask loggerTask;
	private ScheduledExecutorService scheduler;
	private long interval;
	public long getInterval() { return interval; }
	private long lastUpdate;
	public long getLastUpdate() { return lastUpdate; }
	private boolean running;
	
	/**
	 * Constructs a WatchtimeLogger with the default interval of 10 minutes
	 */
	public WatchtimeLogger (
			TwitchClient client,
			TtvStorageInterface storageInterface,
			String channelName,
			OAuth2Credential botChatCred) {
		this(client, storageInterface, channelName, botChatCred, 10);
	}
	
	public WatchtimeLogger (
			TwitchClient client,
			TtvStorageInterface storageInterface,
			String channelName,
			OAuth2Credential tmiCredential,
			long intervalMinutes) {
		
		this.twitchClient = client;
		
		this.storageInterface = storageInterface;
		this.channelName = channelName;
		this.tmiCredential = tmiCredential;
		this.interval = intervalMinutes;
		this.loggerTask = new LoggerTask();
		this.lastUpdate = 0L;
		this.running = false;
		
		List<Stream> streams = twitchClient.getHelix().getStreams(
				tmiCredential.getAccessToken(),
				"",
				null,
				1,
				null,
				null,
				null,
				null,
				Collections.singletonList(channelName)
		).execute().getStreams();
		if(streams.size() > 0) { //channel is streaming?
			Stream stream = streams.get(0);
			if(stream.getType().equalsIgnoreCase("live")) {
				goOnline(stream.getTitle(), stream.getGameId());
			} else {
				System.out.println("Stream found but type is: " + stream.getType());
				goOffline();
			}
		} else {
			goOffline();
		}
		
		client.getEventManager().onEvent(ChannelGoLiveEvent.class).subscribe(event -> goOnline(event.getTitle(), event.getGameId()));
		client.getEventManager().onEvent(ChannelGoOfflineEvent.class).subscribe(event -> goOffline());
		
	}
	
	public boolean start() {
		boolean started = false;
		if(!running) {
			if(scheduler != null) {
				try {
					scheduler.shutdown();
					scheduler.awaitTermination(30L, TimeUnit.SECONDS);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(loggerTask, 0L, this.interval, TimeUnit.MINUTES);
			started = true;
			running = true;
		} else {
			System.err.println("WatchtimeLogger already running for " + channelName + "!");
		}
		return started;
	}
	
	public void stop() {
		if(running) {
			if(scheduler != null) {
				try {
					scheduler.shutdown();
					scheduler.awaitTermination(30L, TimeUnit.SECONDS);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("Running is true, but scheduler is null!");
			}
		} else {
			System.err.println("WatchtimeLogger is already stopped!");
		}
	}
	
	public synchronized void setInterval(long minutes) {
		this.interval = minutes;
		if(running) {
			stop();
			start();
		}
	}
	
	private synchronized void updateChatters() {
		//Update no more often than 30 seconds
		if(new Date().getTime() - getLastUpdate() >= 30 * 1000) {
			List<String> namesInChat = twitchClient.getMessagingInterface()
							.getChatters(this.channelName)
							.execute()
							.getAllViewers();
			
			System.out.println("Users in chat at " + new Date().toString());
			List<TtvUser> tmpUsers = new ArrayList<>();
			for(String username : namesInChat) {
				System.out.println("    " + username);
				tmpUsers.add(storageInterface.findOrCreateTtvUser(username));
			}
			
			this.usersInChat = tmpUsers;
			lastUpdate = new Date().getTime();
			
		} else {
			throw new RuntimeException("Attempting to update chatters less than 30s after " + lastUpdate);
		}
	}
	
	private synchronized TtvUser logMinutes(TtvUser user, long minutes) {
		TtvUser updatedUser = storageInterface.logMinutes(user, minutes, online);
		return updatedUser;
	}
	
	private synchronized void logAllMinutes(long minutes) {
		List<TtvUser> users = getUsersInChat();
		for(int i = 0; i < users.size(); i++) {
			users.set(i, logMinutes(users.get(i), minutes));
			//System.out.println(users.get(i).getHelixUser().getDisplayName() + " now has " + String.format("%.2f", users.get(i).getTotalHours()) + "h");
		}
	}
	
	public synchronized void goOnline(String title, long gameId) {
		online = true;
		System.out.println(channelName + " is streaming! Title: " + title);
	}
	
	public synchronized void goOffline() {
		online = false;
		System.out.println(channelName + " is offline.");
	}
	
	private class LoggerTask implements Runnable {
		WatchtimeLogger parent = WatchtimeLogger.this;
		
		@Override
		public void run() {
			//System.out.println("Updating chatters");
			parent.updateChatters();
			//System.out.println("Logging all minutes");
			parent.logAllMinutes(parent.getInterval());
		}
	}
	
}
