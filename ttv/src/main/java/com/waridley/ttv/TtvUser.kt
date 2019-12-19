package com.waridley.ttv

import com.github.twitch4j.helix.domain.User
import java.util.*

class TtvUser(val helixUser: User) {
	val id: String = helixUser.id
	var offlineMinutes = 0L
	var onlineMinutes = 0L
	var guestMinutes = 0L
	val properties: MutableMap<String, Any> = HashMap()
	
	fun channelMinutes(): Long {
		return onlineMinutes + offlineMinutes
	}
	
	fun totalMinutes(): Long {
		return onlineMinutes + offlineMinutes + guestMinutes
	}
}