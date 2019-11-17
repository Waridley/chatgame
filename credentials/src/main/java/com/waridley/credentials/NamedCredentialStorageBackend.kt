package com.waridley.credentials;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.api.IStorageBackend;
import com.github.philippheuer.credentialmanager.domain.Credential;

import java.util.*;

public class NamedCredentialStorageBackend implements IStorageBackend {
	
	protected Map<String, Credential> credentialMap;
	
	public NamedCredentialStorageBackend() {
		credentialMap = new HashMap<>();
	}
	
	public void saveCredential(String name, Credential credential) {
		credentialMap.put(name, credential);
	}
	
	public Optional<Credential> getCredentialByName(String name) {
		return Optional.ofNullable(credentialMap.get(name));
	}
	
	@Override
	public List<Credential> loadCredentials() {
		return new ArrayList<>(credentialMap.values());
	}
	
	@Override
	public void saveCredentials(List<Credential> credentials) {
		for(Credential credential : credentials) {
			credentialMap.put(credential.getUserId(), credential);
		}
	}
	
	@Override
	public Optional<Credential> getCredentialByUserId(String userId) {
		return Optional.ofNullable(credentialMap.get(userId));
	}
}
