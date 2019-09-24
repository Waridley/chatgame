package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TtvUser;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoGameBackend extends MongoBackend implements GameStorageInterface {
	
	private MongoCollection<Player> playerCollection;
	
	public MongoCollection<Player> getPlayerCollection() {
		return playerCollection;
	}
	
	private Map<ObjectId, Player> playerCache = Collections.synchronizedSortedMap(new TreeMap<>());
	private TwitchHelix helix;
	private OAuth2Credential helixCredential;
	
	public MongoGameBackend(MongoDatabase db, TwitchHelix twitchHelix, OAuth2Credential helixCredential) {
		super(db);
		this.helix = twitchHelix;
		this.helixCredential = helixCredential;
		
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.conventions(conventions)
				.build();
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
//				CodecRegistries.fromCodecs(new PlayerCodec()),
				CodecRegistries.fromProviders(pojoCodecProvider)
		);
		
		playerCollection = createCollectionIfNotExists("players", Player.class).withCodecRegistry(codecRegistry);
	}
	
	//region findOrCreatePlayer()
	@Override
	public Player findOrCreatePlayer(TtvUser ttvUser) {
		Player player = checkCacheFor(ttvUser);
		if(player == null) {
			playerCollection.findOneAndUpdate(
					Filters.eq("ttvUser._id", ttvUser.getId()),
					Updates.setOnInsert(
							new Document("ttvUser", ttvUser)
								.append("username", ttvUser.getHelixUser().getDisplayName())
						),
						new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
					);
		}
		return player;
	}
	@Override
	public Player findOrCreatePlayer(long ttvUserId) {
		Player player = checkCacheFor(ttvUserId);
		if(player == null) {
			TtvUser ttvUser = null;
			UserList userList = helix.getUsers(
					helixCredential.getAccessToken(),
					Collections.singletonList(ttvUserId),
					null
			).execute();
			for(User user : userList.getUsers()) {
				ttvUser = new TtvUser(user);
			}
			player = playerCollection.findOneAndUpdate(
					Filters.eq("ttvUser._id", ttvUserId),
					Updates.setOnInsert(new Document("ttvUser", ttvUser)),
					new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
			);
		}
		
		return player;
	}
	
	@Override
	public Player findOrCreatePlayer(String username) {
		Player player = checkCacheFor(username);
		if(player == null) {
			player = playerCollection.find(Filters.eq("username", username)).first();
			if(player == null) {
				TtvUser ttvUser = null;
				UserList userList = helix.getUsers(
						helixCredential.getAccessToken(),
						null,
						Collections.singletonList(username)
				).execute();
				for(User user : userList.getUsers()) {
					ttvUser = new TtvUser(user);
				}
				player = playerCollection.findOneAndUpdate(
						Filters.eq("username", username),
						Updates.setOnInsert(new Document("ttvUser", ttvUser)),
						new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
				);
			}
			
		}
		return player;
	}
	//endregion
	
	//region findPlayer()
	@Override
	public Optional<Player> findPlayer(TtvUser ttvUser) {
		Player player = checkCacheFor(ttvUser);
		if(player == null) {
			player = playerCollection.find(Filters.eq("ttvUser._id", ttvUser.getId())).first();
			if(player != null) playerCache.put(player.getId(), player);
		}
		return Optional.ofNullable(player);
	}
	
	@Override
	public Optional<Player> findPlayer(String username) {
		Player player = checkCacheFor(username);
		if(player == null) {
			player = playerCollection.find(Filters.eq("username", username)).first();
			if(player != null) playerCache.put(player.getId(), player);
		}
		return Optional.ofNullable(player);
	}
	
	@Override
	public Optional<Player> findPlayer(ObjectId id) {
		Player player = playerCache.get(id);
		if(player == null) {
			player = playerCollection.find(Filters.eq("_id", id)).first();
			if(player != null) playerCache.put(player.getId(), player);
		}
		return Optional.ofNullable(player);
	}
	//endregion
	
	@Override
	public Player logMinutes(Player player, long minutes, boolean online) {
		//TODO implement player watchtime logging
		return null;
	}
	
	/**
	 * WARNING: This will overwrite any existing record and erase any missing fields. Be sure the player object is up-to-date before saving.
	 *
	 * @param player The player to save
	 * @return The saved player
	 */
	@Override
	public Player savePlayer(Player player) {
		Player result = playerCollection.findOneAndReplace(
				Filters.eq("_id", player.getId()),
				player,
				new FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
				);
		if(result != null) playerCache.put(player.getId(), player);
		return result;
	}
	
	//region checkCacheFor()
	private Player checkCacheFor(TtvUser ttvUser) {
		Player player = null;
		for(Player p : playerCache.values()) {
			if(p.getTtvUser().equals(ttvUser)) {
				if(player == null) player = p;
				else throw new RuntimeException("Found more than one player for Twitch user ID " + ttvUser.getId());
			}
		}
		return player;
	}
	
	private Player checkCacheFor(String username) {
		Player player = null;
		for(Player p : playerCache.values()) {
			if(p.getUsername().equals(username)) {
				if(player == null) player = p;
				else throw new RuntimeException("Found more than one player for username " + username);
			}
		}
		return player;
	}
	
	private Player checkCacheFor(long ttvUserId) {
		Player player = null;
		for(Player p : playerCache.values()) {
			if(p.getTtvUser().getId() == ttvUserId) {
				if(player == null) player = p;
				else throw new RuntimeException("Found more than one player for Twitch user ID " + ttvUserId);
			}
		}
		return player;
	}
	
	//endregion
}
