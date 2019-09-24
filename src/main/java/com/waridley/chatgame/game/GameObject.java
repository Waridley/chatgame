package com.waridley.chatgame.game;

import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.List;

public abstract class GameObject {
	
	@BsonIgnore
	protected GameObject parent = null;
	
	protected List<GameObject> children;
	
	private void addChild(GameObject child) throws Exception {
		if(child.parent == null) {
			children.add(child);
			child.parent = this;
		} else {
			throw new Exception("Cannot claim child -- child already has a parent!");
		}
	}
}
