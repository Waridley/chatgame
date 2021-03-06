/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.game.inventory

import lombok.Data

@Data
class Chest {
	var inside: ItemContainer? = null
}