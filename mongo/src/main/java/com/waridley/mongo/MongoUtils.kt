package com.waridley.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

fun connectToDatabase(connectionURI: String): MongoDatabase {
	val connStr = ConnectionString(connectionURI)
	val settings = MongoClientSettings.builder()
			.applyConnectionString(connStr)
			.retryWrites(true)
			.build()
	val mongoClient = MongoClients.create(settings)
	return mongoClient.getDatabase("chatgame")
}