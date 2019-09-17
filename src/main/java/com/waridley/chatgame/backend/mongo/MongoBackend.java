/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.mongo;

import com.github.twitch4j.helix.domain.User;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.waridley.chatgame.backend.StorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoBackend implements StorageInterface {
	
	private MongoCollection<TwitchUser> twitchUsersCollection;
	
	public MongoBackend(ConnectionString connectionString) {
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		PojoCodecProvider codecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.register(TwitchUser.class)
				.register(Player.class)
				.build();
		CodecRegistry pojoCodecRegistry = fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				fromProviders(codecProvider));
		twitchUsersCollection = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(pojoCodecRegistry);
		
	}
	
	@Override
	public TwitchUser findOrCreateTwitchUser(User user) {
		
		TwitchUser twitchUser = twitchUsersCollection.findOneAndUpdate(
				Filters.eq(
						"userid",
						user.getId()
				),
				Updates.combine(
						new Document(
								"$setOnInsert",
								new Document("userid", user.getId())
										.append("onlineMinutes", 0L)
										.append("offlineMinutes", 0L)
						),
						new Document(
								"$set",
								new Document("login", user.getLogin())
						)
				),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		);
		
		return twitchUser;
	}
	
	@Override
	public TwitchUser logMinutes(TwitchUser user, long minutes, boolean online) {
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
	
}
