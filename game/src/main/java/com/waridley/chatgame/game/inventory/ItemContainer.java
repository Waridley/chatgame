/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.game.inventory;

import com.waridley.chatgame.game.GameObject;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class ItemContainer {
	List<GameObject> items;
	
	public ItemContainer() {
		items = Collections.emptyList();
	}
	
	public ItemContainer(int size) {
		items = Arrays.asList(new GameObject[size]);
	}
	
}
