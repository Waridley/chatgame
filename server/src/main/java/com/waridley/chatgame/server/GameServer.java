package com.waridley.chatgame.server;

import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.game.Game;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GameServer {
	
	private Game game;
	private GameStorageInterface gameBackend;
	
	
}
