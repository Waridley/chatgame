/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.ttv

import com.github.twitch4j.helix.TwitchHelix
import com.github.twitch4j.helix.domain.User
import java.util.*

interface TtvStorageInterface {
	
	val helix: TwitchHelix
	
	fun helixAccessToken(): String?
	
	fun findOrCreateTtvUserFromId(userId: String): TtvUser? {
		return getHelixUsersFromIds(listOf(userId))[0]?.let { findOrCreateTtvUser(it) }
	}
	
	fun findOrCreateTtvUserFromLogin(login: String): TtvUser? {
		return getHelixUsersFromLogins(listOf(login))[0]?.let { findOrCreateTtvUser(it) }
	}
	
	fun findOrCreateTtvUser(user: User): TtvUser?
	
	fun findTtvUserById(userId: String): TtvUser? {
		return getHelixUsersFromIds(listOf(userId))[0]?.let { findTtvUserByHelixUser(it) }
	}
	
	fun findTtvUserByLogin(login: String): TtvUser? {
		return getHelixUsersFromLogins(listOf(login))[0]?.let { findTtvUserByHelixUser(it) }
	}
	
	fun findTtvUserByHelixUser(user: User): TtvUser?
	
	fun findTtvUsers(helixUsers: List<User>): List<TtvUser?>
	fun findTtvUsersByIds(userIds: List<String>): List<TtvUser?>
	
	fun findTtvUser(helixUser: User?, userId: String?, login: String?): TtvUser? {
		return findTtvUsers(
				helixUser?.let{ listOf(it) },
				userId?.let{ listOf(it) },
				login?.let{ listOf(it) }
		)[0]
	}
	fun findTtvUsers(helixUsers: List<User>?, userIds: List<String>?, logins: List<String>?): List<TtvUser?>
	
	fun getHelixUsersFromIds(ids: List<String>): List<User?> {
		val result: MutableList<User?> = ArrayList(ids.size)
		val divSize = 100
		val idLists: MutableList<List<String>> = ArrayList(ids.size / divSize + 1)
		var i = 0
		while (i < ids.size) {
			var to = i + divSize
			if (to >= ids.size) to = ids.size
			idLists.add(ids.subList(i, to))
			i += divSize
		}
		for (l in idLists) {
			val userList = helix.getUsers(
					helixAccessToken(),
					l,
					null
			).execute()
			result.addAll(userList.users)
		}
		return result
	}
	
	fun getHelixUsersFromLogins(logins: List<String>): List<User?> {
		val result: MutableList<User?> = Vector(logins.size)
		val divSize = 100
		val loginLists: MutableList<List<String>> = Vector(logins.size / divSize + 1)
		var i = 0
		while (i < logins.size) {
			var to = i + divSize
			if (to > logins.size) to = logins.size
			loginLists.add(logins.subList(i, to))
			i += divSize
		}
		for (l in loginLists) {
			val userList = helix.getUsers(
					helixAccessToken(),
					null,
					l
			).execute()
			result.addAll(userList.users)
		}
		return result
	}
	
	fun logMinutes(user: TtvUser, minutes: Long, online: Boolean): TtvUser? {
		return if(online) incrementProperty(user.id, "onlineMinutes", minutes)
		else incrementProperty(user.id, "offlineMinutes", minutes)
	}
	fun logGuestMinutes(user: TtvUser, minutes: Long, guestLogin: String): TtvUser?
	
	fun <T> getProperty(userId: String, propertyName: String): T? {
		return getProperties<T>(listOf(userId), propertyName)[0] as T
	}
	fun <T> getProperties(userIds: List<String>, propertyName: String): List<T?> {
		return userIds.map { getProperty<T>(it, propertyName) }
	}
	
	fun setProperty(userId: String, propertyName: String, value: Any?): TtvUser?
	fun setProperties(valuesPerUser: List<Pair<String, Any?>>, propertyName: String): List<TtvUser?> {
		return valuesPerUser.map { setProperty(it.first, propertyName, it.second) }
	}
	fun setProperties(userId: String, nameValuePairs: List<Pair<String, Any?>>): List<TtvUser?> {
		return nameValuePairs.map { setProperty(userId, it.first, it.second) }
	}
	
	fun incrementProperty(userId: String, propertyName: String, amount: Number): TtvUser? {
		return setProperty(
				userId,
				propertyName,
				getProperty<Number>(
						userId,
						propertyName
				)?.let{ it + amount }
		)
	}
}

private operator fun Number.plus(n: Number): Number {
	return when(this) {
		is Byte -> this + n.toByte()
		is Short -> this + n.toShort()
		is Int -> this + n.toInt()
		is Long -> this + n.toLong()
		is Float -> this + n.toFloat()
		else -> this as Double + n.toDouble()
	}
}
