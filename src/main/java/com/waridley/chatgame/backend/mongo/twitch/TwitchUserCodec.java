package com.waridley.chatgame.backend.mongo.twitch;

import com.waridley.chatgame.backend.game.GameStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.Optional;

public class TwitchUserCodec implements Codec<TwitchUser> {
	
	private GameStorageInterface storageInterface;
	
	public TwitchUserCodec(GameStorageInterface storageInterface) {
		this.storageInterface = storageInterface;
	}
	
	@Override
	public TwitchUser decode(BsonReader reader, DecoderContext decoderContext) {
		return null;
	}
	
	@Override
	public void encode(BsonWriter writer, TwitchUser user, EncoderContext encoderContext) {
		Optional<ObjectId> playerOpt = Optional.ofNullable(user.getPlayerId());
		writer.writeStartDocument();
			writer.writeName("userid");
				writer.writeInt64(user.getUserId());
			writer.writeName("login");
				writer.writeString(user.getLogin());
			writer.writeName("offlineMinutes");
				writer.writeInt64(user.getOfflineMinutes());
			writer.writeName("onlineMinutes");
				writer.writeInt64(user.getOnlineMinutes());
			if(playerOpt.isPresent()) {
				writer.writeName("playerId");
					writer.writeObjectId(playerOpt.get());
			}
			writer.writeName("helixUser");
				writer.writeStartDocument();
					//TODO write helixUser object
				writer.writeEndDocument();
			writer.writeEndDocument();
	}
	
	@Override
	public Class<TwitchUser> getEncoderClass() {
		return TwitchUser.class;
	}
}
