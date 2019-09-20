/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.mongo.twitch;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.waridley.chatgame.backend.mongo.MongoCredStorageBackend;
import com.waridley.chatgame.backend.mongo.NamedCredential;
import com.waridley.chatgame.backend.mongo.game.PlayerCodec;
import com.waridley.chatgame.backend.mongo.twitch.OAuth2Codec;
import com.waridley.chatgame.backend.twitch.TwitchStorageInterface;
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

//TODO: import single methods
import static org.bson.codecs.configuration.CodecRegistries.*;

public class MongoTwitchBackend implements TwitchStorageInterface {
	
	private MongoCollection<TwitchUser> twitchUsersCollection;
	private MongoCollection<NamedCredential> adminCollection;
	private TwitchHelix helix;
	
	private MongoCredStorageBackend credStorageBackend;
	
	public MongoTwitchBackend(MongoDatabase db, TwitchHelix helix, TwitchIdentityProvider provider) {
		this.helix = helix;
		
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(false)
				.conventions(conventions)
				.register(User.class)
				.register(TwitchUser.class)
				//.register(Player.class)
				//.register(OAuth2Codec.class)
				.register(MongoCredStorageBackend.CredentialWrapper.class)
				.register(NamedCredential.class)
				.build();
		CodecRegistry codecRegistry = fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				fromCodecs(new OAuth2Codec()),
				//fromCodecs(new PlayerCodec(this)),
				fromProviders(pojoCodecProvider)
		);
		
		twitchUsersCollection = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(codecRegistry);
		adminCollection = db.getCollection("admin", NamedCredential.class).withCodecRegistry(codecRegistry);
		MongoCollection<OAuth2Credential> clientCredentialCollection =  db.getCollection("credentials", OAuth2Credential.class).withCodecRegistry(codecRegistry);
		this.credStorageBackend = new MongoCredStorageBackend(clientCredentialCollection, provider);
		
	}
	
	@Override
	public IStorageBackend getCredentialStorageBackend() {
		return credStorageBackend;
	}
	
	@Override
	public Optional<NamedCredential> loadNamedCredential(String name) {
		return Optional.ofNullable(adminCollection.find(Filters.eq("name", name)).first());
	}
	
	@Override
	public void saveAdminCredential(String name, OAuth2Credential credential) {
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
										.append("helixUser", user)
						)
				),
				new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		);
		
		assert twitchUser != null;
		if(twitchUser.getPlayerId() == null) {
			Player player = new Player(twitchUser);
			//TODO: Save player to GameStorageInterface
			twitchUser.setPlayerId(player.getObjectId());
			twitchUser = twitchUsersCollection.findOneAndUpdate(
					Filters.eq(
							"userid",
							twitchUser.getUserId()
					),
					Updates.combine(
							new Document("$set", new Document("playerId", player.getObjectId())
							)
					),
					new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)
			);
		}
		return twitchUser;
	}
	
	@Override
	public TwitchUser find(User user) throws TwitchUser.UserNotFoundException {
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
	public TwitchUser find(long ttvUserId) throws TwitchUser.UserNotFoundException {
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
	public TwitchUser find(String username) throws TwitchUser.UserNotFoundException {
		UserList chatters = helix.getUsers(
				null,
				null,
				Collections.singletonList(username.toLowerCase())
		).execute();
		for(User user : chatters.getUsers()) {
			System.out.println("Found Helix user: " + user.getDisplayName());
			return find(user);
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
		
		return updatedUser;
	}
	
	
}
