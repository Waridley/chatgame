package com.waridley.chatgame.server;

import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.ttv.TtvStorageInterface;
import com.waridley.ttv.TtvUser;
import lombok.AllArgsConstructor;

public class CommandExecutive {
	
	private final TtvStorageInterface ttvStorageInterface;
	private final GameStorageInterface gameStorageInterface;
	
	final InfoRelayer info;
	final PermissionChecker check;
	
	CommandExecutive(TtvStorageInterface ttvStorageInterface, GameStorageInterface gameStorageInterface) {
		this.ttvStorageInterface = ttvStorageInterface;
		this.gameStorageInterface = gameStorageInterface;
		this.info = new InfoRelayer(ttvStorageInterface, gameStorageInterface);
		this.check = new PermissionChecker(ttvStorageInterface, gameStorageInterface);
	}
	
	
	@AllArgsConstructor
	static class InfoRelayer {
		
		private TtvStorageInterface ttvStorageInterface;
		private GameStorageInterface gameStorageInterface;
		
		TtvUser ttvUserFromLogin(String login) {
			return ttvStorageInterface.findOrCreateTtvUser(login);
		}
		
		Player playerFromTtvUser(TtvUser ttvUser) {
			return gameStorageInterface.findOrCreatePlayer(ttvUser);
		}
		
		Player playerFromTtvUserId(String userId) {
			return gameStorageInterface.findOrCreatePlayer(Long.parseLong(userId));
		}
	}
	
	@AllArgsConstructor
	static class PermissionChecker {
		
		private TtvStorageInterface ttvStorageInterface;
		private GameStorageInterface gameStorageInterface;
		
		
		
	}
	
	
	
}
