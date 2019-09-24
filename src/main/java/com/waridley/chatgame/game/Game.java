package com.waridley.chatgame.game;

import com.waridley.chatgame.backend.GameStorageInterface;
public class Game {
	
	
	private GameObject root = new RootObject(this);
	
	private GameStorageInterface storageInterface;
	public GameStorageInterface getStorageInterface() { return storageInterface; }
	
	public Game(GameStorageInterface storageInterface) {
		this.storageInterface = storageInterface;
	}
	
	
	public void addPlayer(Player player) {
		//storageInterface.savePlayer(player);
		//playerCache.put(player.getId(), player);
	}
	
}
