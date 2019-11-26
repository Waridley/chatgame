package com.waridley.ttv

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.helix.TwitchHelix
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

class MongoTtvOptions: TtvBackendOptions("MongoTtvOptions") {
	
	lateinit var helix: TwitchHelix
	
	val credential: OAuth2Credential?
			by option().convert { OAuth2Credential("twitch", it) }
	
	private val connectionUrl
			by option("-s", "--connection-string").required()
	
	val collectionName
			by option("-c", "--collection").default("ttv-users")
	
	val db = connectToDatabase(connectionUrl)
	
}

fun connectToDatabase(connectionURI: String): MongoDatabase {
	val connStr = ConnectionString(connectionURI)
	val settings = MongoClientSettings.builder()
			.applyConnectionString(connStr)
			.retryWrites(true)
			.build()
	val mongoClient = MongoClients.create(settings)
	return mongoClient.getDatabase("chatgame")
}