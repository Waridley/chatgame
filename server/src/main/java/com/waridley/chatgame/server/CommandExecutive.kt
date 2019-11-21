package com.waridley.chatgame.server

import com.waridley.chatgame.api.backend.GameStorageInterface
import com.waridley.chatgame.game.Player
import com.waridley.ttv.TtvStorageInterface
import com.waridley.ttv.TtvUser

class CommandExecutive internal constructor(private val ttvStorageInterface: TtvStorageInterface, private val gameStorageInterface: GameStorageInterface) {
	@JvmField
	val info: InfoRelayer
	val check: PermissionChecker
	
	class InfoRelayer(private val ttvStorageInterface: TtvStorageInterface, private val gameStorageInterface: GameStorageInterface) {
		fun ttvUserFromLogin(login: String?): TtvUser {
			return ttvStorageInterface.findOrCreateTtvUserFromLogin(login)
		}
		
		fun playerFromTtvUser(ttvUser: TtvUser): Player? {
			return gameStorageInterface.findOrCreatePlayer(ttvUser)
		}
		
		fun playerFromTtvUserId(userId: String): Player? {
			return gameStorageInterface.findOrCreatePlayerByTtvId(userId)
		}
		
	}
	
	class PermissionChecker(private val ttvStorageInterface: TtvStorageInterface, private val gameStorageInterface: GameStorageInterface)
	
	init {
		info = InfoRelayer(ttvStorageInterface, gameStorageInterface)
		check = PermissionChecker(ttvStorageInterface, gameStorageInterface)
	}
}