/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.game.inventory;

import lombok.Data;

@Data
public class BankAccount {
	ItemContainer itemVault = new ItemContainer();
	CurrencyContainer currencyVault = new CurrencyContainer();
}
