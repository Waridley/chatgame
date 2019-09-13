package com.waridley.chatgame.tests;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import com.waridley.chatgame.ttv_integration.WatchtimeLogger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


public class MiscTester {
	
	/* TODO:
	 *  Migrate code to main module
	 */
	
	public static void main(String[] args) {
		String channelName = args[0];
		String oauthToken = args[1];
		String mongoUname = args[2];
		String mongoPwd = args[3];
		long intervalMinutes = 6;
		
		OAuth2Credential ttvCred = new OAuth2Credential("waridley_chatgame", oauthToken);
		
		TwitchClient twitchClient = TwitchClientBuilder.builder()
				.withEnableHelix(true)
				.withEnableChat(true)
				.withChatAccount(ttvCred)
				.withEnableTMI(true)
				.build();
		twitchClient.getClientHelper().enableStreamEventListener(channelName);
		
		ConnectionString connectionString = new ConnectionString(
				"mongodb+srv://" + mongoUname + ":" + mongoPwd + "@chatgame-jwz1u.gcp.mongodb.net/test?retryWrites=true&w=majority");
		
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		PojoCodecProvider codecProvider = PojoCodecProvider.builder().automatic(true).register(TwitchUser.class).build();
		CodecRegistry pojoCodecRegistry = fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				fromProviders(codecProvider));
		MongoCollection<TwitchUser> twitchUsers = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(pojoCodecRegistry);
		
		WatchtimeLogger logger = new WatchtimeLogger(twitchClient, twitchUsers, channelName, intervalMinutes);
		
	}
	
}
