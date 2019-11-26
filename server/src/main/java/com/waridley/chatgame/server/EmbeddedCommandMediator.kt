package com.waridley.chatgame.server

import com.waridley.chatgame.api.frontend.CommandMediator
import com.waridley.chatgame.game.Player

class EmbeddedCommandMediator(private val exec: CommandExecutive) : CommandMediator {
	override fun getPlayerByTtvLogin(login: String): Player? {
		println("Getting TtvUser for login \"$login\"")
		val ttvUser = exec.info.ttvUserFromLogin(login)
		println("Getting player for TtvUser \"" + ttvUser?.helixUser?.displayName + "\"")
		return ttvUser?.let{ exec.info.playerFromTtvUser(it) }
	}
	
	override fun getPlayerByTtvUserId(userId: String): Player? {
		return exec.info.playerFromTtvUserId(userId)
	}
	
}