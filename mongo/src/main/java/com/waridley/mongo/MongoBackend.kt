package com.waridley.mongo

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase

interface MongoBackend {
	
	val db: MongoDatabase
	
	fun <TDocument> createCollectionIfNotExists(collectionName: String, documentClass: Class<TDocument>): MongoCollection<TDocument> {
		return createCollectionIfNotExists(db, collectionName, documentClass)
	}
	
	companion object {
		fun <TDocument> createCollectionIfNotExists(db: MongoDatabase, collectionName: String, documentClass: Class<TDocument>): MongoCollection<TDocument> {
			var collectionExists = false
			for (name in db.listCollectionNames()) {
				if (name == collectionName) {
					collectionExists = true
					break
				}
			}
			if (!collectionExists) {
				db.createCollection(collectionName)
			}
			return db.getCollection(collectionName, documentClass)
		}
	}
}