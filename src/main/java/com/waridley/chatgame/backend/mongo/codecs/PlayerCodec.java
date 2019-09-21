package com.waridley.chatgame.backend.mongo.codecs;

import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Optional;

public class PlayerCodec implements Codec<Player> {
	
	@Override
	public Player decode(BsonReader reader, DecoderContext decoderContext) {
		String username = null;
		Long userid = null;
		TwitchUser twitchUser = null;
		
		reader.readStartDocument();
		while(reader.getCurrentBsonType() != BsonType.END_OF_DOCUMENT) {
			switch(reader.readName()) {
				case("username"):
					username = reader.readString();
					break;
				case("twitchUserId"):
					userid = reader.readInt64();
					break;
				default:
					System.err.println("ERROR: Unknown field name when reading Player: " + reader.getCurrentName());
			}
		}
		reader.readEndDocument();
		
		return new Player(username, userid);
	}
	
	@Override
	public void encode(BsonWriter writer, Player player, EncoderContext encoderContext) {
		Optional<Long> ttvUsrOpt = Optional.ofNullable(player.getTwitchUserId());
		writer.writeStartDocument();
			writer.writeName("username");
				writer.writeString(player.getUsername());
			if(ttvUsrOpt.isPresent()) {
				writer.writeName("twitchUser");
					writer.writeInt64(ttvUsrOpt.get());
			}
		writer.writeEndDocument();
	}
	
	@Override
	public Class<Player> getEncoderClass() {
		return Player.class;
	}
}
