package com.waridley.chatgame.game;

import com.waridley.chatgame.backend.GameStorageInterface;

public class Game {
	
	private GameStorageInterface storageInterface;
	public GameStorageInterface getStorageInterface() { return storageInterface; }
	
	public Game(GameStorageInterface storageInterface) {
		this.storageInterface = storageInterface;
	}
	
	
}
