/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
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
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import sun.plugin2.uitoolkit.impl.awt.OldPluginAWTUtil;

import java.util.*;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoBackend implements StorageInterface {
	
	private MongoCollection<TwitchUser> twitchUsersCollection;
	private MongoCollection<AdminCredential> adminCollection;
	private TwitchHelix helix;
	
	private MongoCredStorageBackend credStorageBackend;
	
	public MongoBackend(ConnectionString connectionString, TwitchHelix helix, TwitchIdentityProvider provider) {
		this.helix = helix;
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		List<Convention> conventions= new ArrayList(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider codecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.conventions(conventions)
				.register(User.class)
				.register(TwitchUser.class)
				.register(Player.class)
				.register(StorableOAuth2Credential.class)
				.register(MongoCredStorageBackend.CredentialWrapper.class)
				.register(AdminCredential.class)
				.build();
		CodecRegistry pojoCodecRegistry = fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				fromProviders(codecProvider)
		);
		
		twitchUsersCollection = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(pojoCodecRegistry);
		adminCollection = db.getCollection("admin", AdminCredential.class).withCodecRegistry(pojoCodecRegistry);
		MongoCollection<OAuth2Credential> clientCredentialCollection =  db.getCollection("credentials", OAuth2Credential.class).withCodecRegistry(pojoCodecRegistry);
		this.credStorageBackend = new MongoCredStorageBackend(clientCredentialCollection, provider);
		
	}
	
	@Override
	public IStorageBackend getCredentialStorageBackend() {
		return credStorageBackend;
	}
	
	@Override
	public Optional<AdminCredential> loadAdminCredential(String name) {
		return Optional.ofNullable(adminCollection.find(Filters.eq("name", name)).first());
	}
	
	@Override
	public void saveAdminCredential(String name, OAuth2Credential credential) {
		StorableOAuth2Credential storableCred = new StorableOAuth2Credential(credential);
		adminCollection.findOneAndUpdate(
				Filters.eq("name", name),
				new Document("$set", new Document("credential", storableCred)),
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
		} catch(ArrayIndexOutOfBoundsException e) {
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
		} catch(ArrayIndexOutOfBoundsException e) {
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
