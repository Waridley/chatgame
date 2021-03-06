/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.game.inventory

import com.waridley.chatgame.game.GameObject
import lombok.Data
import java.util.*

@Data
class ItemContainer {
	var items: List<GameObject>
	
	constructor() {
		items = emptyList()
	}
	
	constructor(size: Int) {
		items = Arrays.asList(*arrayOfNulls(size))
	}
}