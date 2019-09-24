package com.waridley.chatgame.backend.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public abstract class MongoBackend {
	
	protected final MongoDatabase db;
	
	protected MongoBackend(MongoDatabase db) {
		this.db = db;
	}
	
	/**
	 * Creates a collection with the given name if it doesn't already exist
	 *
	 * @param collectionName The name of the collection to create
	 * @return The found or created collection
	 */
	protected <TDocument> MongoCollection<TDocument> createCollectionIfNotExists(String collectionName, Class<TDocument> type) {
		boolean collectionExists = false;
		for(String name : db.listCollectionNames()) {
			if(name.equals(collectionName)) {
				collectionExists = true;
				break;
			}
		}
		if(!collectionExists)  {
			db.createCollection(collectionName);
		}
		return db.getCollection(collectionName, type);
	}
}
