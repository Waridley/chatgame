/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */

package com.waridley.chatgame.tests;


import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.auth.TwitchAuth;
import com.waridley.chatgame.ttv_integration.ReflexiveAuthenticationController;

public class MiscTester {
	
	
	
	public static void main(String[] args) {
		String clientId = args[0];
		String clientSecret = args[1];
		
		MiscTester tester = new MiscTester();
		
		ReflexiveAuthenticationController authController = new ReflexiveAuthenticationController(6464, tester::handleToken);
		
		CredentialManager credentialManager = CredentialManagerBuilder.builder()
				.withAuthenticationController(authController)
				.build();
		
		
		
		TwitchAuth twitchAuth = new TwitchAuth(credentialManager, clientId, clientSecret, "http://localhost:6464");
	}
	
	private void handleToken(OAuth2Credential credential) {
	
	}
	
}
