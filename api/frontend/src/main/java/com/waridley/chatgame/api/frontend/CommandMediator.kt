package com.waridley.chatgame.api.frontend

import com.waridley.chatgame.game.Player

interface CommandMediator {
	fun getPlayerByTtvLogin(login: String?): Player?
	fun getPlayerByTtvUserId(userId: String?): Player?
}