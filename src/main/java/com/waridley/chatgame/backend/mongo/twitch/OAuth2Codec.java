package com.waridley.chatgame.backend.mongo.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class OAuth2Codec implements Codec<OAuth2Credential> {
	
	/**
	 * The identity provider key
	 */
	private String identityProvider;
	public String getIdentityProvider() { return identityProvider; }
	public void setIdentityProvider(String identityProvider) { this.identityProvider = identityProvider; }
	
	/**
	 * Unique User Id
	 */
	private String userId;
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	
	/**
	 * Access Token
	 */
	private String accessToken;
	public String getAccessToken() { return accessToken; }
	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
	
	/**
	 * Refresh Token
	 */
	private String refreshToken;
	public String getRefreshToken() { return refreshToken; }
	public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
	
	/**
	 * User Name
	 */
	private String userName;
	public String getUserName() { return userName; }
	public void setUserName(String userName) { this.userName = userName; }
	
	private Integer expiresIn;
	public Integer getExpiresIn() { return expiresIn; }
	public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
	
	/**
	 * OAuth Scopes
	 */
	private List<String> scopes;
	public List<String> getScopes() { return scopes; }
	public void setScopes(List<String> scopes) { this.scopes = scopes; }
	
	/**
	 * Constructor
	 *
	 * @param identityProvider Identity Provider
	 * @param accessToken      Authentication Token
	 */
	
	
	/**
	 * For delayed serialization only. Sets everything to null as placeholders to allow construction.
	 */
	public OAuth2Codec() {
		this(null, null);
	}
	
	/**
	 * For delayed serialization only. Sets everything to null as placeholders to allow construction.
	 */
	public OAuth2Codec(String identityProvider, String accessToken) {
		this(identityProvider, accessToken, null, null, null, null, null);
	}
	
	public OAuth2Codec(OAuth2Credential credential) {
		this(credential.getIdentityProvider(), credential.getAccessToken(), credential.getRefreshToken(), credential.getUserId(), credential.getUserName(), null, credential.getScopes());
	}
	/**
	 * Constructor
	 *
	 * @param identityProvider Identity Provider
	 * @param accessToken      Authentication Token
	 * @param refreshToken     Refresh Token
	 * @param userId           User Id
	 * @param userName         User Name
	 * @param expiresIn        Expires in x seconds
	 * @param scopes           Scopes
	 */
	public OAuth2Codec(String identityProvider, String accessToken, String refreshToken, String userId, String userName, Integer expiresIn, List<String> scopes) {
		this.identityProvider = identityProvider;
		this.userId = userId;
		this.accessToken = accessToken;
		if(accessToken != null && accessToken.startsWith("oauth:")) { accessToken.replace("oauth:", ""); }
		this.refreshToken = refreshToken;
		this.userName = userName;
		this.expiresIn = expiresIn;
		this.scopes = scopes;
	}
	
	public OAuth2Credential toOAuth2Credential() {
		return new OAuth2Credential(identityProvider, accessToken, refreshToken, userId, userName, expiresIn, scopes);
	}
	
	@Override
	public OAuth2Credential decode(BsonReader reader, DecoderContext decoderContext) {
		
		reader.readStartDocument();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			if(reader.getCurrentBsonType() != BsonType.NULL) {
				String name = reader.readName();
				switch (reader.getCurrentName()) {
					case("accessToken"):
						setAccessToken(reader.readString());
						break;
					case("expiresIn"):
						setExpiresIn(reader.readInt32());
						break;
					case("identityProvider"):
						setIdentityProvider(reader.readString());
						break;
					case("refreshToken"):
						setRefreshToken(reader.readString());
						break;
					case("scopes"):
						List<String> scopes = new ArrayList<>();
						reader.readStartArray();
							while(reader.readBsonType() == BsonType.STRING) scopes.add(reader.readString());
							setScopes(scopes);
						reader.readEndArray();
					case("userId"):
						setUserId(reader.readString());
						break;
					case("userName"):
						setUserName(reader.readString());
						break;
					default:
						System.err.println("ERROR: Unknown field name when reading OAuth2Credential: " + reader.getCurrentName());
				}
			}
		}
		reader.readEndDocument();
		
		return toOAuth2Credential();
	}
	
	@Override
	public void encode(BsonWriter writer, OAuth2Credential credential, EncoderContext encoderContext) {
		writer.writeStartDocument();
			writer.writeName("accessToken");
				writer.writeString(credential.getAccessToken());
			writer.writeName("identityProvider");
				writer.writeString(credential.getIdentityProvider());
			writer.writeName("refreshToken");
				writer.writeString(credential.getRefreshToken());
			writer.writeName("scopes");
				writer.writeStartArray();
					List<String> scopes = credential.getScopes();
					for(String scope : scopes) {
						writer.writeString(scope);
					}
				writer.writeEndArray();
			writer.writeName("userId");
				writer.writeString(credential.getUserId());
			writer.writeName("userName");
				writer.writeString(credential.getUserName());
		writer.writeEndDocument();
	}
	
	@Override
	public Class<OAuth2Credential> getEncoderClass() {
		return OAuth2Credential.class;
	}
}
