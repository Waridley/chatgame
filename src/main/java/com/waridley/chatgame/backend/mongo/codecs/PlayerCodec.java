package com.waridley.chatgame.backend.mongo.codecs;

import com.waridley.chatgame.backend.TtvStorageInterface;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TtvUser;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.util.Optional;

public class PlayerCodec implements Codec<Player> {
	
	private TtvStorageInterface ttvStorageInterface;
	
	private PlayerCodec(TtvStorageInterface ttvUserSource) {
		this.ttvStorageInterface = ttvUserSource;
	}
	
	@Override
	public Player decode(BsonReader reader, DecoderContext decoderContext) {
		ObjectId id = new ObjectId();
		String username = null;
		Optional<TtvUser> ttvUser = Optional.empty();
		
		reader.readStartDocument();
		while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			switch(reader.readName()) {
				case("_id"):
					id = reader.readObjectId();
				case("username"):
					username = reader.readString();
					break;
				case("ttvUserId"):
					ttvUser = ttvStorageInterface.findTtvUser(reader.readInt64());
					break;
				default:
					System.err.println("ERROR: Unknown field name when reading Player: " + reader.getCurrentName());
			}
		}
		reader.readEndDocument();
		
		return new Player(id, username, ttvUser.orElse(null));
	}
	
	@Override
	public void encode(BsonWriter writer, Player player, EncoderContext encoderContext) {
		Optional<TtvUser> ttvUsrOpt = Optional.ofNullable(player.getTtvUser());
		writer.writeStartDocument();
			writer.writeName("_id");
				writer.writeObjectId(player.getId());
			writer.writeName("username");
				writer.writeString(player.getUsername());
			if(ttvUsrOpt.isPresent()) {
				writer.writeName("ttvUserId");
					writer.writeInt64(ttvUsrOpt.get().getId());
			}
		writer.writeEndDocument();
	}
	
	@Override
	public Class<Player> getEncoderClass() {
		return Player.class;
	}
}
