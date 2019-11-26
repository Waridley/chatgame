/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.ttv.mongo

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.domain.User
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.waridley.mongo.MongoBackend
import com.waridley.ttv.MongoTtvOptions
import com.waridley.ttv.TtvStorageInterface
import com.waridley.ttv.TtvUser
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.Convention
import org.bson.codecs.pojo.Conventions
import org.bson.codecs.pojo.PojoCodecProvider
import java.util.*

class MongoTtvBackend(override val db: MongoDatabase, override val helix: TwitchHelix) : TtvStorageInterface, MongoBackend {
	
	private lateinit var ttvUserCollection: MongoCollection<TtvUser>
	
	private var helixCredential: OAuth2Credential? = null
	override fun helixAccessToken(): String? {
		return if (helixCredential != null) helixCredential!!.accessToken else null
	}
	
	private val ttvUserCache: MutableMap<String, TtvUser> = Collections.synchronizedSortedMap(TreeMap())
	
	constructor(opts: MongoTtvOptions): this(opts.db, opts.helix, opts.credential)
	
	constructor(db: MongoDatabase, helix: TwitchHelix, helixCredential: OAuth2Credential?) : this(db, helix) {
		this.helixCredential = helixCredential
		val conventions: MutableList<Convention> = ArrayList(Conventions.DEFAULT_CONVENTIONS)
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION)
		val pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(false)
				.conventions(conventions)
				.register(User::class.java)
				.register(TtvUser::class.java)
				.build()
		val codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(pojoCodecProvider)
		)
		ttvUserCollection = createCollectionIfNotExists("ttv_users", TtvUser::class.java).withCodecRegistry(codecRegistry)
	}
	
	//region New TtvUser methods
	//region findOrCreateTtvUser()
	override fun findOrCreateTtvUserFromId(ttvUserId: String): TtvUser {
		var ttvUser = ttvUserCache[ttvUserId]
		if (ttvUser == null) {
			val chatters = helix.getUsers(
					helixAccessToken(), listOf(ttvUserId),
					null
			).execute()
			for (u in chatters.users) {
				ttvUser = findOrCreateTtvUser(u)
			}
		}
		return ttvUser!!
	}
	
	override fun findOrCreateTtvUserFromLogin(ttvLogin: String): TtvUser {
		var ttvUser: TtvUser? = null
		for (u in ttvUserCache.values) {
			if (u.helixUser.login.equals(ttvLogin, ignoreCase = true)) {
				ttvUser = u
				break
			}
		}
		if (ttvUser == null) {
			val chatters = helix.getUsers(
					null,
					null, listOf(ttvLogin)).execute()
			for (u in chatters.users) {
				ttvUser = findOrCreateTtvUser(u)
			}
		}
		return ttvUser!!
	}
	
	override fun findOrCreateTtvUser(user: User): TtvUser? {
		val ttvUser = ttvUserCollection.findOneAndUpdate(
				Filters.eq("_id", user.id),
				Updates.combine(
						Updates.setOnInsert(
								Document("_id", user.id)
										.append("onlineMinutes", 0L)
										.append("offlineMinutes", 0L)
										.append("guestMinutes", 0L)
						),
						Document(
								"\$set",
								Document("helixUser", user)
						)
				),
				FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		)
		if (ttvUser != null) ttvUserCache[ttvUser.id] = ttvUser
		return ttvUser
	}
	
	//endregion

	override fun findTtvUserByLogin(login: String): TtvUser? {
		val result = ttvUserCollection.find(Filters.eq("login", login)).first()
		if (result != null) ttvUserCache[result.id] = result
		return result
	}
	
	override fun findTtvUserByHelixUser(user: User): TtvUser? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun findTtvUsers(helixUsers: List<User>): List<TtvUser> {
		val userIds: MutableList<String> = ArrayList(helixUsers.size)
		for (u in helixUsers) {
			userIds.add(u.id)
		}
		return findTtvUsersByIds(userIds)
	}
	
	override fun findTtvUsers(helixUsers: List<User>?, userIds: List<String>?, logins: List<String>?): List<TtvUser?> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun findTtvUsersByIds(userIds: List<String>): List<TtvUser> {
		val result: MutableList<TtvUser> = ArrayList(userIds.size)
		for (u in ttvUserCollection.find(Filters.`in`("_id", userIds))) {
			result.add(u)
			ttvUserCache[u.id] = u
		}
		return result
	}
	
	fun saveTtvUser(user: TtvUser) {
		ttvUserCollection.findOneAndUpdate(
				Filters.eq("_id", user.id),
				Document("\$set",
						Document("offlineMinutes", user.offlineMinutes)
								.append("onlineMinutes", user.onlineMinutes)
								.append("guestMinutes", user.guestMinutes)
								.append("helixUser", user.helixUser)
				),
				FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		)
	}
	
	override fun logMinutes(user: TtvUser, minutes: Long, online: Boolean): TtvUser? {
		val status: String
		val currentMinutes: Long
		if (online) {
			status = "online"
			currentMinutes = user.onlineMinutes
		} else {
			status = "offline"
			currentMinutes = user.offlineMinutes
		}
		val updatedUser = ttvUserCollection.findOneAndUpdate(
				Filters.eq("_id", user.id),
				Document("\$set", Document()
						.append(status + "Minutes", currentMinutes + minutes)),
				FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		)
		updatedUser?.let { ttvUserCache[updatedUser.id] = updatedUser }
		return updatedUser
	}
	
	override fun logGuestMinutes(user: TtvUser, minutes: Long, guestLogin: String): TtvUser? {
		val currentMinutes = user.guestMinutes
		val updatedUser = ttvUserCollection.findOneAndUpdate(
				Filters.eq("_id", user.id),
				Document("\$set", Document()
						.append("guestMinutes", currentMinutes + minutes)),
				FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		)
		updatedUser?.let { ttvUserCache[updatedUser.id] = updatedUser }
		return updatedUser
	}
	
	override fun setProperty(userId: String, propertyName: String, value: Any?): TtvUser? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	
	//endregion
}