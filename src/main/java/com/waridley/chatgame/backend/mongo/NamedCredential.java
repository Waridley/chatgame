/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.backend.mongo;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;

public class NamedCredential {
	
	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	private OAuth2Credential credential;
	public OAuth2Credential getCredential() { return credential; }
	public void setCredential(OAuth2Credential credential) { this.credential = credential; }
	/*
	
	public AdminCredential(String name, OAuth2Credential credential) {
		this.name = name;
		this.credential = credential;
	}*/
}
