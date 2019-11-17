/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.game.inventory;

import lombok.Data;

@Data
public class Backpack {
	
	private ItemContainer mainPouch = new ItemContainer();
	
	private CurrencyContainer currencyPouch = new CurrencyContainer();
}
