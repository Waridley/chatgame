package com.waridley.chatgame.tests;


import com.github.twitch4j.tmi.TwitchMessagingInterface;
import com.github.twitch4j.tmi.TwitchMessagingInterfaceBuilder;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MiscTester {
	
	public static void main(String[] args) {
		TwitchMessagingInterface client = TwitchMessagingInterfaceBuilder.builder()
				.withClientId("WaridleyTestBot")
				.withClientSecret("oauth:2bshf4j3qkrakt32ays8yky8zp2toz")
				.build();
		
		final Morphia morphia = new Morphia();
		morphia.mapPackage("com.waridley.chatgame");
		//final Datastore datastore = morphia.createDatastore(new MongoClient(), "chatgame_db");
		//datastore.ensureIndexes();
		
		WatchtimeLogger logger = new WatchtimeLogger(client, "waridley", morphia);
		
		System.out.println(logger.getAllViewers());
	}
	
}
