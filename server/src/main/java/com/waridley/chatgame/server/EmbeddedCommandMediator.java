package com.waridley.chatgame.server;

import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.api.frontend.CommandMediator;
import com.waridley.chatgame.game.Player;
import com.waridley.ttv.TtvStorageInterface;
import com.waridley.ttv.TtvUser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmbeddedCommandMediator implements CommandMediator {
	
	private CommandExecutive exec;
	
	@Override
	public Player getPlayerByTtvLogin(String login) {
		System.out.println("Getting TtvUser for login \"" + login + "\"");
		TtvUser ttvUser = exec.info.ttvUserFromLogin(login);
		System.out.println("Getting player for TtvUser \"" + ttvUser.getHelixUser().getDisplayName() + "\"");
		return exec.info.playerFromTtvUser(ttvUser);
	}
	
	@Override
	public Player getPlayerByTtvUserId(String userId) {
		return exec.info.playerFromTtvUserId(userId);
	}
}
