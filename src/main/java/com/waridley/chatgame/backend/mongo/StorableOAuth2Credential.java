package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.waridley.chatgame.backend.StorageInterface;
import sun.plugin2.uitoolkit.impl.awt.OldPluginAWTUtil;

import java.util.List;

public class StorableOAuth2Credential {
	
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
	 * Credential
	 *
	 * @param identityProvider Identity Provider
	 * @param userId           User Id
	 */
	
	/**
	 * Access Token
	 */
	private String accessToken;
	public String getAccessToken() { return accessToken; }
	public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Integer getExpiresIn() {
		return expiresIn;
	}
	
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}
	
	public List<String> getScopes() {
		return scopes;
	}
	
	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}
	
	/**
	 * Refresh Token
	 */
	private String refreshToken;
	
	/**
	 * User Name
	 */
	private String userName;
	
	
	private Integer expiresIn;
	
	
	/**
	 * OAuth Scopes
	 */
	private List<String> scopes;
	
	/**
	 * Constructor
	 *
	 * @param identityProvider Identity Provider
	 * @param accessToken      Authentication Token
	 */
	public StorableOAuth2Credential(String identityProvider, String accessToken) {
		this(identityProvider, accessToken, null, null, null, null, null);
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
	public StorableOAuth2Credential(String identityProvider, String accessToken, String refreshToken, String userId, String userName, Integer expiresIn, List<String> scopes) {
		this.identityProvider = identityProvider;
		this.userId = userId;
		this.accessToken = accessToken.startsWith("oauth:") ? accessToken.replace("oauth:", "") : accessToken;
		this.refreshToken = refreshToken;
		this.userName = userName;
		this.expiresIn = expiresIn;
		this.scopes = scopes;
	}
	
	public StorableOAuth2Credential() {
		this("twitch", "");
	}
	
	public StorableOAuth2Credential(OAuth2Credential credential) {
		this(credential.getIdentityProvider(), credential.getAccessToken(), credential.getRefreshToken(), credential.getUserId(), credential.getUserName(), Integer.valueOf(-1), credential.getScopes());
	}
	
	public OAuth2Credential toOAuth2Credential() {
		return new OAuth2Credential(identityProvider, accessToken, refreshToken, userId, userName, expiresIn, scopes);
	}
	
}
