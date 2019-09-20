package com.waridley.chatgame.tests;

import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.TwitchHelix;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.waridley.chatgame.backend.twitch.TwitchStorageInterface;
import com.waridley.chatgame.backend.mongo.twitch.MongoTwitchBackend;
import com.waridley.chatgame.ttv_integration.TwitchUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class PointsImporter {
	
	private ConnectionString connectionString;
	private TwitchHelix helix;
	private TwitchIdentityProvider identityProvider;
	
	public static void main(String[] args) throws IOException {
		/*ConnectionString connString = new ConnectionString(args[0]);
		TwitchHelix helix = TwitchClientBuilder.builder().withEnableHelix(true).build().getHelix();
		TwitchIdentityProvider provider = new TwitchIdentityProvider("id", "secret", "localhost");
		
		PointsImporter pi = new PointsImporter(connString, helix, provider);
		
		File file = pi.getFileFromResources("phazon.csv");
		
		pi.importPoints(file);*/
	}
	
	public PointsImporter(ConnectionString connectionString, TwitchHelix helix, TwitchIdentityProvider identityProvider) {
		this.connectionString = connectionString;
		this.helix = helix;
		this.identityProvider = identityProvider;
	}
	
	private File getFileFromResources(String fileName) {
		ClassLoader classLoader = getClass().getClassLoader();
		
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return new File(resource.getFile());
		}
		
	}
	
	private void importPoints(File file) throws IOException {
		if(file == null) return;
		
		FileReader reader = new FileReader(file);
		BufferedReader br = new BufferedReader(reader);
		
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		
		
		TwitchStorageInterface storageInterface = new MongoTwitchBackend(db, helix, identityProvider);
		
		String line;
		TwitchUser currentUser;
		while((line = br.readLine()) != null) {
			String[] splitLine = line.split(",");
			String username = splitLine[0];
			String pointsString = splitLine[1];
			long points = Long.parseLong(pointsString);
			
			
			
			try {
				currentUser = storageInterface.findOrCreateTwitchUser(username);
				System.out.println(currentUser.getLogin() +
						" currently has " + currentUser.getOnlineMinutes() + " minutes. " +
						"Adding " + String.valueOf(points));
				
				storageInterface.logMinutes(currentUser, points, true);
				
			} catch(TwitchUser.UserNotFoundException e) {
				System.err.println("Didn't find user " + username);
			}
			
		}
		
	}
	
	/*public TwitchUser logMinutes(TwitchUser user, long minutes, boolean online) {
		
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.retryWrites(true)
				.build();
		
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase db = mongoClient.getDatabase("chatgame");
		
		List<Convention> conventions = new ArrayList<>(Conventions.DEFAULT_CONVENTIONS);
		conventions.add(Conventions.SET_PRIVATE_FIELDS_CONVENTION);
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder()
				.automatic(false)
				.conventions(conventions)
				.register(User.class)
				.register(TwitchUser.class)
				.build();
		CodecRegistry codecRegistry = fromRegistries(
				com.mongodb.MongoClient.getDefaultCodecRegistry(),
				fromProviders(pojoCodecProvider)
		);
		
		MongoCollection<TwitchUser> twitchUsersCollection = db.getCollection("twitch_users", TwitchUser.class).withCodecRegistry(codecRegistry);
		
		String status;
		long currentMinutes;
		
		if(online) {
			status = "online";
			currentMinutes = user.getOnlineMinutes();
		} else {
			status = "offline";
			currentMinutes = user.getOfflineMinutes();
		}
		
		TwitchUser updatedUser = twitchUsersCollection.findOneAndUpdate(
				Filters.eq("userid", user.getUserId()),
				new Document("$set", new Document()
						.append(status + "Minutes", currentMinutes + minutes)),
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		);
		
		return updatedUser;
	}*/
	
}
