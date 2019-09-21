package com.waridley.chatgame.backend.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.backend.GameStorageInterface;
import com.waridley.chatgame.backend.mongo.codecs.PlayerCodec;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class MongoGameBackend implements GameStorageInterface {
	
	public MongoGameBackend(MongoDatabase db) {
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.conventions(conventions)
				.register(Player.class)
				.build();
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				//CodecRegistries.fromCodecs(new PlayerCodec()),
				CodecRegistries.fromProviders(pojoCodecProvider)
		);
		
		if(!collectionExists(db, "players")) db.createCollection("players");
		db.getCollection("players");
	}
	
	private boolean collectionExists(MongoDatabase db, String collectionName) {
		boolean collectionExists = false;
		for(String name : db.listCollectionNames()) {
			if(name.equals(collectionName)) collectionExists = true;
		}
		return collectionExists;
	}
	
	@Override
	public Player findOrCreatePlayer(TwitchUser twitchUser) {
		return null;
	}
	
	@Override
	public Player findOrCreatePlayer(String gameUsername) {
		return null;
	}
	
	@Override
	public Player findOrCreatePlayer(ObjectId id) {
		return null;
	}
	
	@Override
	public Player findPlayer(TwitchUser twitchUser) {
		return null;
	}
	
	@Override
	public Player findPlayer(String gameUsername) {
		return null;
	}
	
	@Override
	public Player findPlayer(long userId) {
		return null;
	}
}
