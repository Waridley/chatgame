package com.waridley.ttv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.twitch4j.auth.providers.TwitchIdentityProvider

class TtvClientOptions: CliktCommand() {
	
	val channelName
			by option("-c", "--channel", help = "The name of the channel to join.").prompt("Channel name:")
	val clientId
			by option("-i", "--client-id", help = "Your application's client ID").prompt("Client ID:")
	val clientSecret
			by option("-s", "--client-secret", help = "Your application's client secret.").prompt("Client secret:", hideInput = true)
	val redirectUrl
			by option("-r", "--redirect-url", help = "The redirect URL for OAuth2 code flow. Must match the URL registered with your client ID.").default("http://localhost")
	val infoPath
			by option("-p", "--info-path", help = "The path to the page explaining the credential code flow.")
	
	
	lateinit var idProvider: TwitchIdentityProvider
	
	lateinit var ttvBackend: TtvStorageInterface
	
	
	
	override fun run() {
		
		idProvider = TwitchIdentityProvider(clientId, clientSecret, redirectUrl)
		
		
	}
	
}

open class TtvBackendOptions(name: String): OptionGroup(name)