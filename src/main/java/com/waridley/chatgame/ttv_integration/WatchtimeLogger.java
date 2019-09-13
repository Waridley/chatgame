package com.waridley.chatgame.ttv_integration;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* TODO:
 *  Implement currency logging
 *  Link Player object to each TwitchUser
 */

public class WatchtimeLogger {
	
	private boolean online;
	
	private TwitchClient twitchClient;
	private MongoCollection<TwitchUser> twitchUsersCollection;
	private String channelName;
	
	private List<TwitchUser> usersInChat;
	public List<TwitchUser> getUsersInChat() { return usersInChat; }
	
	private LoggerTask loggerTask;
	private ScheduledExecutorService scheduler;
	private long interval;
	public long getInterval() { return interval; }
	private long lastUpdate;
	public long getLastUpdate() { return lastUpdate; }
	
	public WatchtimeLogger (
			TwitchClient client,
			MongoCollection<TwitchUser> twitchUsers,
			String channelName,
			long intervalMinutes) {
		
		this.twitchClient = client;
		
		this.twitchUsersCollection = twitchUsers;
		this.channelName = channelName;
		
		List<Stream> resultList = twitchClient.getHelix().getStreams(
				"",
				"",
				null,
				1,
				null,
				null,
				null,
				null,
				Arrays.asList(new String[] {channelName})
		).execute().getStreams();
		if(resultList.size() > 0) {
			goOnline();
			System.out.println(channelName + " is streaming! Title: " + resultList.get(0).getTitle());
		} else {
			goOffline();
		}
		
		client.getEventManager().onEvent(ChannelGoLiveEvent.class).subscribe(event -> {
			if(event.getChannel().getName().equalsIgnoreCase(channelName)) {
				goOnline();
			}
		});
		client.getEventManager().onEvent(ChannelGoOfflineEvent.class).subscribe(event -> {
			if(event.getChannel().getName().equalsIgnoreCase(channelName)) {
				goOffline();
			}
		});
		
		loggerTask = new LoggerTask();
		lastUpdate = 0;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		try {
			setInterval(intervalMinutes);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setInterval(long minutes) throws InterruptedException {
		this.interval = minutes;
		scheduler.awaitTermination(30L, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(loggerTask, 0L, this.interval, TimeUnit.MINUTES);
	}
	
	private void updateChatters() throws RateLimitException {
		//Update no more often than 30 seconds
		if(new Date().getTime() - getLastUpdate() >= 30 * 1000) {
			usersInChat = new ArrayList<>();
			
			UserList chatters = twitchClient.getHelix().getUsers(
					null,
					null,
					twitchClient.getMessagingInterface()
							.getChatters(this.channelName)
							.execute()
							.getAllViewers()
			).execute();
			
			for(User user : chatters.getUsers()) {
				
				long initOnline = 0L, initOffline = 0L;
				if(online) initOnline = interval;
				else initOffline = interval;
				
				TwitchUser twitchUser = twitchUsersCollection.findOneAndUpdate(
					Filters.eq(
							"userid",
							user.getId()
					),
					Updates.combine(
						new Document(
							"$setOnInsert",
							new Document("userid", user.getId())
								.append("onlineMinutes", initOnline)
								.append("offlineMinutes", initOffline)
						),
						new Document(
							"$set",
							new Document("login", user.getLogin())
						)
					),
					new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
				);
				usersInChat.add(twitchUser);
			}
			
			lastUpdate = new Date().getTime();
			
		} else {
			throw new RateLimitException("Chatters updated <30s ago at " + lastUpdate);
		}
	}
	
	
	private TwitchUser logMinutes(TwitchUser user, long minutes) {
		String status;
		long currentMinutes;
		
		if(online) {
			status = "online";
			currentMinutes = user.getOnlineMinutes();
		} else {
			status = "offline";
			currentMinutes = user.getOfflineMinutes();
		}
		
		
		TwitchUser updatedUser = twitchUsersCollection.findOneAndUpdate(
				Filters.eq("userid", user.getUserId()),
				new Document("$set", new Document()
						.append(status + "Minutes", currentMinutes + minutes)),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		);
		
		return updatedUser;
	}
	
	private void logAllMinutes(long minutes) {
		List<TwitchUser> users = getUsersInChat();
		for(int i = 0; i < users.size(); i++) {
			users.set(i, logMinutes(users.get(i), minutes));
		}
	}
	
	public void goOnline() {
		online = true;
		
	}
	
	public void goOffline() {
		online = false;
		
	}
	
	private class LoggerTask implements Runnable {
		WatchtimeLogger parent = WatchtimeLogger.this;
		
		@Override
		public void run() {
			try {
				parent.updateChatters();
				parent.logAllMinutes(parent.getInterval());
				System.out.println("Users in chat:");
				for(TwitchUser user : parent.getUsersInChat()) {
					System.out.println(user.toString());
				}
			} catch(RateLimitException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static class RateLimitException extends Exception {
		
		public RateLimitException() {
			super();
		}
		
		public RateLimitException(String message) {
			super(message);
		}
		
	}
	
}
