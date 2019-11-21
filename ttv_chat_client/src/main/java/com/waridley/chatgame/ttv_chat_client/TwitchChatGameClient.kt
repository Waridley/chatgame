/**
 * Copyright (c) 2019 Kevin Day
 * Licensed under the EUPL
 */
package com.waridley.chatgame.ttv_chat_client

import com.github.philippheuer.credentialmanager.CredentialManager
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider
import com.github.twitch4j.auth.domain.TwitchScopes
import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.TwitchChatBuilder
import com.github.twitch4j.chat.events.channel.ChannelJoinEvent
import com.github.twitch4j.chat.events.channel.ChannelLeaveEvent
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.waridley.chatgame.api.frontend.CommandMediator
import com.waridley.chatgame.api.frontend.GameClient
import com.waridley.credentials.NamedCredentialStorageBackend
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*

class TwitchChatGameClient(
		provider: OAuth2IdentityProvider,
		private val channelName: String,
		private val commandMediator: CommandMediator) : GameClient {
	
	private val credentialStorage: NamedCredentialStorageBackend = provider.credentialManager.storageBackend as NamedCredentialStorageBackend
	private val credentialManager: CredentialManager = provider.credentialManager
	private val identityProvider: OAuth2IdentityProvider = provider
	override fun start() {
		log.info("Starting Twitch Chat Game Client")
		waitForCredential()
	}
	
	private fun waitForCredential() {
//		List<Credential> credentials = credentialStorage.loadCredentials();
//		Optional<NamedOAuth2Credential> botCredOpt = Optional.empty();
//		for(Credential c : credentials) {
//			if(c instanceof NamedOAuth2Credential) {
//				if(((NamedOAuth2Credential) c).getName().equals("botCredential")) botCredOpt = Optional.of((NamedOAuth2Credential) c);
//			}
//		}
		val botCredOpt = credentialStorage.getCredentialByName("botCredential")
		if (botCredOpt.isPresent) {
			log.info("Found bot credential.")
			var credential = botCredOpt.get() as OAuth2Credential
			val refreshedCredOpt = identityProvider.refreshCredential(credential)
			if (refreshedCredOpt.isPresent) {
				credential = refreshedCredOpt.get()
				log.info("Successfully refreshed token")
			} else {
				System.err.println("Failed to refresh token. Delete the credential from storage to generate a new one.")
			}
			buildChat(credential)
		} else {
			log.info("No saved bot credential found. Starting OAuth2 Authorization Code Flow.")
			try {
				newCredential
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}
	
	@get:Throws(IOException::class)
	private val newCredential: Unit
		private get() {
			val server = HttpServer.create(InetSocketAddress(6464), 0)
			server.createContext("/") { exchange: HttpExchange -> onReceivedCode(exchange) }
			server.createContext("/info.html") { exchange: HttpExchange -> handleInfoPage(exchange) }
			server.start()
			credentialManager.authenticationController.startOAuth2AuthorizationCodeGrantType(
					identityProvider,
					"http://localhost:6464",
					Arrays.asList<Any>(
							TwitchScopes.CHAT_CHANNEL_MODERATE,
							TwitchScopes.CHAT_EDIT,
							TwitchScopes.CHAT_READ,
							TwitchScopes.CHAT_WHISPERS_EDIT,
							TwitchScopes.CHAT_WHISPERS_READ
					))
		}
	
	@Throws(IOException::class)
	private fun handleInfoPage(exchange: HttpExchange) {
		var authUrl = ""
		val reqURI = exchange.requestURI
		val queryParams = reqURI.query.split("&").toTypedArray()
		for (param in queryParams) {
			if (param.startsWith("authurl=")) {
				authUrl = param.replaceFirst("authurl=".toRegex(), "")
				authUrl = URLDecoder.decode(authUrl, StandardCharsets.UTF_8.toString())
			}
		}
		val response = "<html>" +
				"<head>" +
				"</head>" +
				"<body>" +
				"<h1>Log in to your desired chat bot account</h1>" +
				"The following link will take you to the Twitch authentication page to log in.<br>" +
				"If you do not want to use your main account for the chat bot, you can either:<br>" +
				"<p style=\"margin-left: 40px\">1) Click \"Not you?\" on that page, however, this will permanently change the account you are logged into on Twitch until you manually switch back.</p>" +
				"<p style=\"margin-left: 40px\">2) Right-click this link, and open it in a private/incognito window. This will allow you to stay logged in to Twitch on your main account in normal browser windows.</p>" +
				"<a href=" + authUrl + ">" + authUrl + "</a>" +
				"</body>" +
				"</html>"
		exchange.sendResponseHeaders(200, response.length.toLong())
		exchange.responseBody.write(response.toByteArray())
		exchange.responseBody.close()
	}
	
	private fun onReceivedCode(exchange: HttpExchange) {
		var code: String? = null
		val uri = exchange.requestURI
		val response = "<html>" +
				"<head>" +
				"</head>" +
				"<body>" +
				"<h1>Success!</h1>" +
				"Received authorization code. Getting token and joining chat." +
				"</body>" +
				"</html>"
		try {
			exchange.sendResponseHeaders(200, response.length.toLong())
			exchange.responseBody.write(response.toByteArray())
			exchange.responseBody.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
		val query = uri.query
		val splitQuery = query.split("&").toTypedArray()
		for (s in splitQuery) {
			val splitField = s.split("=").toTypedArray()
			if (splitField[0] == "code") {
				code = splitField[1]
				break
			}
		}
		val cred = identityProvider.getCredentialByCode(code)
		identityProvider.credentialManager.addCredential("twitch", cred)
		buildChat(cred)
	}
	
	private fun buildChat(credential: OAuth2Credential) {
		var credential = credential
		val c = identityProvider.getAdditionalCredentialInformation(credential)
		if (c.isPresent) credential = c.get()
		//		NamedOAuth2Credential botCredential = new NamedOAuth2Credential("botCredential", credential);
		credentialStorage.saveCredential("botCredential", credential)
		log.info("Saved bot credential for " + credential.userName)
		val twitchChat = TwitchChatBuilder.builder()
				.withCredentialManager(credentialManager)
				.withChatAccount(credential)
				.build()
		twitchChat.joinChannel(channelName)
		val commandDispatcher = CommandDispatcher(twitchChat.eventManager)
		val commandParser = CommandParser(twitchChat.eventManager, commandMediator)
		twitchChat.eventManager.onEvent(ChannelJoinEvent::class.java).subscribe { event: ChannelJoinEvent -> userJoined(event) }
		twitchChat.eventManager.onEvent(ChannelLeaveEvent::class.java).subscribe { event: ChannelLeaveEvent -> userLeft(event) }
		log.info("Joined channel: $channelName")
		twitchChat.sendMessage(channelName, "/me The game has started! TwitchRPG")
	}
	
	private fun userJoined(event: ChannelJoinEvent) {
		//log.info(event.getUser().getName() + " just joined this channel!");
	}
	
	private fun userLeft(event: ChannelLeaveEvent) {
		//log.info("Someone just left this channel snowpoSOB");
	}
	
	companion object {
		private val log = LoggerFactory.getLogger(TwitchChatGameClient::class.java)
	}
	
}