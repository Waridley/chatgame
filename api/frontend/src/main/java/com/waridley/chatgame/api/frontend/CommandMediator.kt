package com.waridley.chatgame.api.frontend;

import com.waridley.chatgame.game.Player;

public interface CommandMediator {
	
	Player getPlayerByTtvLogin(String login);
	
	Player getPlayerByTtvUserId(String userId);
	
}
