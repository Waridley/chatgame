package com.waridley.chatgame.mongo

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.helix.TwitchHelix
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import com.waridley.chatgame.api.backend.GameStorageInterface
import com.waridley.chatgame.game.Player
import com.waridley.mongo.MongoBackend
import com.waridley.ttv.TtvUser
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.Convention
import org.bson.codecs.pojo.Conventions
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.ObjectId
import java.util.*

class MongoGameBackend(override val db: MongoDatabase, private val helix: TwitchHelix, private val helixCredential: OAuth2Credential) : GameStorageInterface, MongoBackend {
	private val playerCollection: MongoCollection<Document>
	private val playerView: MongoCollection<Player?>
	private val playerCache: MutableMap<ObjectId?, Player> = Collections.synchronizedSortedMap(TreeMap<ObjectId, Player>())
	private fun createPlayerViewIfNotExists(): MongoCollection<Player?> {
		var viewExists = false
		for (name in db.listCollectionNames()) {
			if (name == "player_view") {
				viewExists = true
				break
			}
		}
		if (!viewExists) db.createView(
				"player_view",
				"playerdata",
				ArrayList(listOf(Aggregates.lookup("ttv_users", "ttvUserId", "_id", "ttvUser"),
						Aggregates.unwind("\$ttvUser"),
						Aggregates.project(Document("ttvUserId", 0))))
		)
		return db.getCollection("player_view", Player::class.java)
	}
	
	//region findOrCreatePlayer()
	override fun findOrCreatePlayer(ttvUser: TtvUser): Player? {
		var player = checkCacheFor(ttvUser)
		if (player == null) {
			val playerDoc = playerCollection.findOneAndUpdate(
					Filters.eq("ttvUserId", ttvUser!!.id),
					Updates.setOnInsert(
							Document("ttvUserId", ttvUser.id)
									.append("username", ttvUser.helixUser.displayName)
					),
					FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
			)
			if (playerDoc != null) {
				player = playerView.find(Filters.eq("_id", playerDoc["_id"])).first()
			}
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	override fun findOrCreatePlayerByTtvId(ttvUserId: String): Player? {
		var player = checkCacheForUsername(ttvUserId)
		if (player == null) {
			var ttvUser: TtvUser? = null
			val userList = helix.getUsers(
					helixCredential.accessToken, listOf(ttvUserId),
					null
			).execute()
			for (user in userList.users) {
				ttvUser = TtvUser(user)
			}
			if (ttvUser != null) {
				val playerDoc = playerCollection.findOneAndUpdate(
						Filters.eq("ttvUserId", ttvUser.id),
						Updates.setOnInsert(
								Document("ttvUserId", ttvUser.id)
										.append("username", ttvUser.helixUser.displayName)
						),
						FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
				)
				if (playerDoc != null) {
					player = playerView.find(Filters.eq("_id", playerDoc["_id"])).first()
				}
			}
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	override fun findOrCreatePlayer(gameUsername: String): Player? {
		println("Looking for player $gameUsername")
		var player = checkCacheForUsername(gameUsername)
		if (player == null) {
			player = playerView.find(Filters.eq("username", gameUsername)).first()
			if (player == null) {
				var ttvUser: TtvUser? = null
				val userList = helix.getUsers(
						helixCredential.accessToken,
						null, listOf(gameUsername)).execute()
				for (user in userList.users) {
					ttvUser = TtvUser(user)
				}
				val playerDoc = playerCollection.findOneAndUpdate(
						Filters.eq("username", gameUsername),
						Updates.setOnInsert(
								Document("ttvUserId", ttvUser?.id)
										.append("username", gameUsername)
						),
						FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
				)
				if (playerDoc != null) {
					player = playerView.find(Filters.eq("_id", playerDoc["_id"])).first()
				}
			}
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	//endregion
	
	//region findPlayer()
	override fun findPlayer(ttvUser: TtvUser): Player? {
		var player = checkCacheFor(ttvUser)
		if (player == null) {
			player = playerView.find(Filters.eq("ttvUser._id", ttvUser!!.id)).first()
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	override fun findPlayer(gameUsername: String): Player? {
		var player = checkCacheForUsername(gameUsername)
		if (player == null) {
			player = playerView.find(Filters.eq("username", gameUsername)).first()
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	override fun findPlayer(id: ObjectId): Player? {
		var player = playerCache[id]
		if (player == null) {
			player = playerView.find(Filters.eq("_id", id)).first()
			if (player != null) playerCache[player.id] = player
		}
		return player
	}
	
	//endregion
	override fun logMinutes(player: Player, minutes: Long, online: Boolean): Player? { //TODO implement player watchtime logging
		return null
	}
	
	/**
	 * WARNING: This will overwrite any existing record and erase any missing fields. Be sure the player object is up-to-date before saving.
	 *
	 * @param player The player to save
	 * @return The saved player
	 */
	override fun savePlayer(player: Player): Player? {
		val result = playerView.findOneAndReplace(
				Filters.eq("_id", player!!.id),
				player,
				FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
		)
		if (result != null) playerCache[player.id] = player
		return result
	}
	
	//region checkCacheFor()
	private fun checkCacheFor(ttvUser: TtvUser): Player? {
		var player: Player? = null
		for (p in playerCache.values) {
			if (p.ttvUser == ttvUser) {
				player = if (player == null) p else throw RuntimeException("Found more than one player for Twitch user ID " + ttvUser!!.id)
			}
		}
		return player
	}
	
	private fun checkCacheForUsername(username: String): Player? {
		var player: Player? = null
		for (p in playerCache.values) {
			if (p.username == username) {
				player = if (player == null) p else throw RuntimeException("Found more than one player for username $username")
			}
		}
		return player
	}
	
	private fun checkCacheForId(ttvUserId: String): Player? {
		var player: Player? = null
		for (p in playerCache.values) {
			if (p.ttvUser?.id == ttvUserId) {
				player = if (player == null) p else throw RuntimeException("Found more than one player for Twitch user ID $ttvUserId")
			}
		}
		return player
	}
	//endregion
	
	init {
		val conventions: MutableList<Convention> = ArrayList(Conventions.DEFAULT_CONVENTIONS)
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION)
		val pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(true)
				.conventions(conventions)
				.build()
		val codecRegistry = CodecRegistries.fromRegistries(
				MongoClient.getDefaultCodecRegistry(),  //				CodecRegistries.fromCodecs(new PlayerCodec()),
				CodecRegistries.fromProviders(pojoCodecProvider)
		)
		playerCollection = createCollectionIfNotExists("playerdata", Document::class.java).withCodecRegistry(codecRegistry)
		playerView = createPlayerViewIfNotExists().withCodecRegistry(codecRegistry)
	}
}