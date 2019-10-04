package com.waridley.chatgame.server;

import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.chatgame.game.Player;
import com.waridley.ttv.TtvStorageInterface;
import com.waridley.ttv.TtvUser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmbeddedCommandMediator implements CommandMediator {
	
	private TtvStorageInterface ttvStorageInterface;
	
	private GameStorageInterface gameStorageInterface;
	
	
	@Override
	public Player getPlayerByTtvLogin(String login) {
		System.out.println("Getting TtvUser for login \"" + login + "\"");
		TtvUser ttvUser = ttvStorageInterface.findOrCreateTtvUser(login);
		System.out.println("Getting player for TtvUser \"" + ttvUser.getHelixUser().getDisplayName() + "\"");
		return gameStorageInterface.findOrCreatePlayer(ttvUser);
	}
	
	@Override
	public Player getPlayerByTtvUserId(String userId) {
		return gameStorageInterface.findOrCreatePlayer(userId);
	}
	
}
