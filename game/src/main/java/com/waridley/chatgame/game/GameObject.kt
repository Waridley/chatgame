package com.waridley.chatgame.game

import org.bson.codecs.pojo.annotations.BsonIgnore

open class GameObject {
	
	@BsonIgnore
	protected var parent: GameObject? = null
	protected val children: MutableList<GameObject> = ArrayList()
	
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