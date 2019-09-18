package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.IdentityProvider;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.helix.domain.User;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.waridley.chatgame.game.Player;
import com.waridley.chatgame.ttv_integration.TwitchUser;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import sun.plugin2.uitoolkit.impl.awt.OldPluginAWTUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoCredStorageBackend implements IStorageBackend {
	
	private MongoCollection<OAuth2Credential> credCollection;
	private TwitchIdentityProvider provider;
	
	public MongoCredStorageBackend(MongoCollection<OAuth2Credential> credCollection, TwitchIdentityProvider provider) {
		this.credCollection = credCollection;
		this.provider = provider;
	}
	
	@Override
	public List<Credential> loadCredentials() {
		ArrayList<Credential> credentials = new ArrayList<>();
		for(OAuth2Credential cred : credCollection.find()) {
			credentials.add(cred);
		}
		return credentials;
	}
	
	@Override
	public void saveCredentials(List<Credential> credentials) {
		for(Credential credential : credentials) {
			if (credential instanceof OAuth2Credential) {
				CredentialWrapper credentialWrapper;
				Optional<OAuth2Credential> enrichedCredential = provider.getAdditionalCredentialInformation((OAuth2Credential) credential);
				if (enrichedCredential.isPresent()) {
					credentialWrapper = new CredentialWrapper(enrichedCredential.get());
					credCollection.findOneAndUpdate(
							Filters.eq("userId", credential.getUserId()),
							Updates.combine(
									new Document(
											"$setOnInsert",
											new Document("userid", credential.getUserId())
									),
									new Document(
											"$set",
											new Document("credential", credentialWrapper.getCredential())
									)
							),
							new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
					);
				} else {
					throw new RuntimeException("Can't get enriched credential");
				}
			} else {
					throw new RuntimeException("Credential is not an OAuth2Credential");
			}
		}
	}
	
	@Override
	public Optional<Credential> getCredentialByUserId(String userId) {
		return Optional.empty();
	}
	
	class CredentialWrapper {
		
		private CredentialWrapper(OAuth2Credential credential) {
			this.setCredential(credential);
			this.setUserId(credential.getUserId());
		}
		
		private String userId;
		public String getUserId() { return userId; }
		private void setUserId(String userId) { this.userId = userId; }
		
		private OAuth2Credential credential;
		public OAuth2Credential getCredential() { return credential; }
		private void setCredential(OAuth2Credential credential) { this.credential = credential; }
	}
}
