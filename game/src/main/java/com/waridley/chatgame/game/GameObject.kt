package com.waridley.chatgame.game

import lombok.Data
import org.bson.codecs.pojo.annotations.BsonIgnore

@Data
open class GameObject {
	@BsonIgnore
	protected var parent: GameObject? = null
	protected var children: List<GameObject> = emptyList()
	@BsonIgnore
	@Throws(Exception::class)
	private fun addChild(child: GameObject) {
		if (child.parent == null) {
			children.add(child)
			child.parent = this
		} else {
			throw Exception("Cannot claim child -- child already has a parent!")
		}
	}
}