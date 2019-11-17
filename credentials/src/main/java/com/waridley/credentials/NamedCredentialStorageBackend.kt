package com.waridley.credentials

import com.github.philippheuer.credentialmanager.api.IStorageBackend
import com.github.philippheuer.credentialmanager.domain.Credential
import java.util.*

open class NamedCredentialStorageBackend : IStorageBackend {
	@JvmField
	protected var credentialMap: MutableMap<String, Credential>
	fun saveCredential(name: String, credential: Credential) {
		credentialMap[name] = credential
	}
	
	fun getCredentialByName(name: String?): Optional<Credential> {
		return Optional.ofNullable(credentialMap[name])
	}
	
	override fun loadCredentials(): List<Credential> {
		return ArrayList(credentialMap.values)
	}
	
	override fun saveCredentials(credentials: List<Credential>) {
		for (credential in credentials) {
			credentialMap[credential.userId] = credential
		}
	}
	
	override fun getCredentialByUserId(userId: String): Optional<Credential> {
		return Optional.ofNullable(credentialMap[userId])
	}
	
	init {
		credentialMap = HashMap()
	}
}