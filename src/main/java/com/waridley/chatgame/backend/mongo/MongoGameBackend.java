package com.waridley.chatgame.backend.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TtvUser;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.*;

public class MongoGameBackend extends MongoBackend implements GameStorageInterface {
	
	private MongoCollection<Player> playerCollection;
	private Map<ObjectId, Player> playerCache = Collections.synchronizedSortedMap(new TreeMap<>());
	
	public MongoGameBackend(MongoDatabase db) {
		super(db);
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
	
	@Override
	public Player findOrCreatePlayer(TtvUser twitchUser) {
		//TODO implement
		return null;
	}
	@Override
	public Player findOrCreatePlayer(long TtvUserId) {
		//TODO implement
		return null;
	}
	
	@Override
	public Player findOrCreatePlayer(String username) {
		//TODO implement
		return null;
	}
	
	@Override
	public Optional<Player> findPlayer(TtvUser ttvUser) {
		Player player = null;
		for(Player p : playerCache.values()) {
			if(p.getTtvUser().equals(ttvUser)) {
				if(player == null) player = p;
				else throw new RuntimeException("Found more than one player for Twitch user ID " + ttvUser.getId());
			}
		}
		if(player == null) {
			player = playerCollection.find(Filters.eq("ttvUser._id", ttvUser.getId())).first();
			if(player != null) playerCache.put(player.getId(), player);
		}
		return Optional.ofNullable(player);
	}
	
	@Override
	public Optional<Player> findPlayer(String username) {
		Player player = null;
		for(Player p : playerCache.values()) {
			if(p.getUsername().equals(username)) {
				if(player == null) player = p;
				else throw new RuntimeException("Found more than one player for username " + username);
			}
		}
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
	
	@Override
	public void savePlayer(Player player) {
		playerCollection.replaceOne(
				Filters.eq("_id", player.getId()),
				player,
				new ReplaceOptions().upsert(true)
				);
	}
	
	
}
