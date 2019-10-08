package com.waridley.chatgame.game;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.Collections;
import java.util.List;

@Data
public class GameObject {
	
	@BsonIgnore
	protected GameObject parent = null;
	
	protected List<GameObject> children = Collections.emptyList();
	
	@BsonIgnore
	private void addChild(GameObject child) throws Exception {
		if(child.parent == null) {
			children.add(child);
			child.parent = this;
		} else {
			throw new Exception("Cannot claim child -- child already has a parent!");
		}
	}
}
