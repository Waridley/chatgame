package com.waridley.chatgame.api.frontend

import com.waridley.chatgame.api.CommandMediator
import com.waridley.chatgame.game.Player

class ClientSocketCommandMediator : CommandMediator {
	override fun getPlayerByTtvLogin(login: String): Player? {
		return null
	}
	
	override fun getPlayerByTtvUserId(userId: String): Player? {
		return null
	}
}