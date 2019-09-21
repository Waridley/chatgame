/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.waridley.chatgame.backend.NamedOAuth2Credential;
import com.waridley.chatgame.backend.mongo.codecs.OAuth2Codec;
import com.waridley.chatgame.backend.TwitchStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bson.codecs.configuration.CodecRegistries;

public class MongoTwitchBackend implements TwitchStorageInterface {
	
	private MongoCollection<TwitchUser> twitchUsersCollection;
	private MongoCollection<NamedOAuth2Credential> adminCollection;
	private TwitchHelix helix;
	
	//private MongoCredStorageBackend credStorageBackend;
	
	public MongoTwitchBackend(
			MongoDatabase db
			, TwitchHelix helix
			//, TwitchIdentityProvider provider
			) {
		this.helix = helix;
		
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(false)
				.conventions(conventions)
				.register(User.class)
				.register(TwitchUser.class)
				.register(NamedOAuth2Credential.class)
				.build();
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs(new OAuth2Codec()),
				CodecRegistries.fromProviders(pojoCodecProvider)
		);
		
		if(!collectionExists(db, "twitch_users")) db.createCollection("twitch_users");
		twitchUsersCollection = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(codecRegistry);
		if(!collectionExists(db, "admin")) db.createCollection("admin");
		adminCollection = db.getCollection("admin", NamedOAuth2Credential.class).withCodecRegistry(codecRegistry);
		//MongoCollection<OAuth2Credential> clientCredentialCollection =  db.getCollection("credentials", OAuth2Credential.class).withCodecRegistry(codecRegistry);
		//this.credStorageBackend = new MongoCredStorageBackend(clientCredentialCollection, provider);
		
	}
	
	private boolean collectionExists(MongoDatabase db, String collectionName) {
		boolean collectionExists = false;
		for(String name : db.listCollectionNames()) {
			if(name.equals(collectionName)) collectionExists = true;
		}
		return collectionExists;
	}
	
	/*@Override
	public IStorageBackend getCredentialStorageBackend() {
		return credStorageBackend;
	}*/
	
	@Override
	@Deprecated
	public Optional<NamedOAuth2Credential> loadNamedCredential(String name) {
		return Optional.ofNullable(adminCollection.find(Filters.eq("name", name)).first());
	}
	
	@Override
	@Deprecated
	public void saveNamedCredential(String name, OAuth2Credential credential) {
		//OAuth2Codec storableCred = new OAuth2Codec(credential);
		adminCollection.findOneAndUpdate(
				Filters.eq("name", name),
				new Document("$set", new Document("credential", credential)),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		);
	}
	
	@Override
	public TwitchUser findOrCreateTwitchUser(long ttvUserId) throws TwitchUser.UserNotFoundException {
		UserList chatters = helix.getUsers(
				null,
				Collections.singletonList(ttvUserId),
				null
		).execute();
		try {
			return findOrCreateTwitchUser(chatters.getUsers().get(0));
		} catch(IndexOutOfBoundsException e) {
			throw new TwitchUser.UserNotFoundException("Couldn't find helix user", e);
		}
	}
	
	@Override
	public TwitchUser findOrCreateTwitchUser(String username) throws TwitchUser.UserNotFoundException {
		UserList chatters = helix.getUsers(
				null,
				null,
				Collections.singletonList(username.toLowerCase())
		).execute();
		try {
			return findOrCreateTwitchUser(chatters.getUsers().get(0));
		} catch(IndexOutOfBoundsException e) {
			throw new TwitchUser.UserNotFoundException("Couldn't find helix user", e);
		}
	}
	
	@Override
	public TwitchUser findOrCreateTwitchUser(User user) {
		
		TwitchUser twitchUser = twitchUsersCollection.findOneAndUpdate(
				Filters.eq("userid", user.getId()),
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
										.append("helixUser", user)
						)
				),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		);
		
		assert twitchUser != null;
		if(twitchUser.getPlayerId() == null) {
			System.out.println("Creating player for Twitch user " + twitchUser.getHelixUser().getDisplayName());
			Player player = new Player(twitchUser);
			System.out.println("Created player " + player.getUsername());
			//TODO: Save player to GameStorageInterface
			twitchUser.setPlayerId(player.getObjectId());
			twitchUsersCollection.updateOne(
					Filters.eq("userid", twitchUser.getUserId()),
					new Document("$set", new Document("playerId", twitchUser.getPlayerId())),
					new UpdateOptions()
			);
		}
		return twitchUser;
	}
	
	@Override
	public TwitchUser findTwitchuser(User user) throws TwitchUser.UserNotFoundException {
		TwitchUser result = null;
		FindIterable<TwitchUser> userIterable = twitchUsersCollection.find(Filters.eq("userid", user.getId()));
		for(TwitchUser twitchUser : userIterable) {
			if(result == null) result = twitchUser;
			else throw new TwitchUser.UserNotFoundException("More than one user found for id: " + user.getId());
		}
		if(result == null) throw new TwitchUser.UserNotFoundException("User not found for id: " + user.getId());
		return result;
	}
	
	@Override
	public TwitchUser findTwitchuser(long ttvUserId) throws TwitchUser.UserNotFoundException {
		UserList chatters = helix.getUsers(
				null,
				Collections.singletonList(ttvUserId),
				null
		).execute();
		try {
			return findOrCreateTwitchUser(chatters.getUsers().get(0));
		} catch(IndexOutOfBoundsException e) {
			throw new TwitchUser.UserNotFoundException("Couldn't find helix user", e);
		}
	}
	
	@Override
	public TwitchUser findTwitchuser(String username) throws TwitchUser.UserNotFoundException {
		UserList chatters = helix.getUsers(
				null,
				null,
				Collections.singletonList(username.toLowerCase())
		).execute();
		for(User user : chatters.getUsers()) {
			System.out.println("Found Helix user: " + user.getDisplayName());
			return findTwitchuser(user);
		}
		throw new TwitchUser.UserNotFoundException("Couldn't find Helix user for " + username);
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
		
		//System.out.println("Logged " + minutes + " minutes for " + updatedUser.getHelixUser().getDisplayName());
		
		return updatedUser;
	}
	
	
}
