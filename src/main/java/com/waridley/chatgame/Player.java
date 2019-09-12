package com.waridley.chatgame;

import com.waridley.chatgame.ttv_integration.TwitchUser;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity("Players")
@Indexes(
		@Index(value = "twitchAcct", fields = @Field("twitchAcct"))
)
public class Player {
	@Id
	private ObjectId id;
	private String username;
	@Reference
	private TwitchUser twitchAcct;
	
}
