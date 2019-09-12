package com.waridley.chatgame.ttv_integration;


import com.waridley.chatgame.Player;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity("TwitchUsers")

public class TwitchUser {
	@Id
	private ObjectId id;
	private String username;
	@Reference
	private Player gameAcct;
	
	
}
