package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;

public class AdminCredential {
	
	private String name;
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	private StorableOAuth2Credential credential;
	public StorableOAuth2Credential getCredential() { return credential; }
	public void setCredential(StorableOAuth2Credential credential) { this.credential = credential; }
	/*
	
	public AdminCredential(String name, OAuth2Credential credential) {
		this.name = name;
		this.credential = credential;
	}*/
}
