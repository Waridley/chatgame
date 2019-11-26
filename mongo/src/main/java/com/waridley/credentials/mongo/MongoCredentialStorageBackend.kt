/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.credentials.mongo

import com.github.philippheuer.credentialmanager.domain.Credential
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.waridley.credentials.NamedCredentialStorageBackend
import com.waridley.credentials.mongo.codecs.CredentialCodecProvider
import com.waridley.mongo.MongoBackend
import com.waridley.mongo.MongoMap
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.jetbrains.annotations.NotNull

class MongoCredentialStorageBackend(override val db: MongoDatabase, collectionName: String = "credentials") : NamedCredentialStorageBackend(), MongoBackend {
	private val credCollection: MongoCollection<Document>
	
	init {
		val codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				//CodecRegistries.fromCodecs(new OAuth2Codec()),
				CodecRegistries.fromProviders(CredentialCodecProvider())
		)
		credCollection = createCollectionIfNotExists(collectionName, Document::class.java).withCodecRegistry(codecRegistry)
		credentialMap = MongoMap(credCollection, Credential::class.java)
	}
}