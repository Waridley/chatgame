/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.game.inventory

import com.waridley.chatgame.game.currency.Coins
import com.waridley.chatgame.game.currency.Gems
import lombok.Data

@Data
class CurrencyContainer {
	val coins = Coins()
	private val gems = Gems()
}