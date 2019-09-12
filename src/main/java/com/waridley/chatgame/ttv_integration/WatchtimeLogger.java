package com.waridley.chatgame.ttv_integration;

import com.github.twitch4j.tmi.TwitchMessagingInterface;
import com.github.twitch4j.tmi.domain.Chatters;
import dev.morphia.Morphia;

import java.util.List;

public class WatchtimeLogger {
	
	private TwitchMessagingInterface tmi;
	private String channelName;
	private Morphia morphia;
	private Chatters chatters;
	
	private int interval;
	
	public WatchtimeLogger (TwitchMessagingInterface tmi, String channelName, Morphia morphia) {
		this.tmi = tmi;
		this.morphia = morphia;
		this.channelName = channelName;
		
		interval = 10 * 60 * 1000;
		
		morphia.mapPackage("com.waridley.chatgame.ttv_integration");
		
		updateChatters();
		
		System.out.println("Admins: " + chatters.getAdmins());
	}
	
	public List<String> getAllViewers() {
		return chatters.getAllViewers();
	}
	
	public int getInterval() {
		return interval;
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	private Chatters updateChatters() {
		chatters = tmi.getChatters(this.channelName).execute();
		return chatters;
	}
}
