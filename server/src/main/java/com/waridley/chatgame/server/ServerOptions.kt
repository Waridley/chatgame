package com.waridley.chatgame.server

import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider

class ServerOptions {
	var socketCommandListenerOptions = SocketCommandListenerOptions()
	@JvmField var embeddedChatClientOptions = EmbeddedChatClientOptions()
	
	constructor(socketCommandListenerOptions: SocketCommandListenerOptions, embeddedChatClientOptions: EmbeddedChatClientOptions) {
		this.socketCommandListenerOptions = socketCommandListenerOptions
		this.embeddedChatClientOptions = embeddedChatClientOptions
	}
	
	constructor()
	
	fun withSocketCommandListenerOptions(socketCommandListenerOptions: SocketCommandListenerOptions): ServerOptions {
		return if (this.socketCommandListenerOptions === socketCommandListenerOptions) this else ServerOptions(socketCommandListenerOptions, embeddedChatClientOptions)
	}
	
	fun withEmbeddedChatClientOptions(embeddedChatClientOptions: EmbeddedChatClientOptions): ServerOptions {
		return if (this.embeddedChatClientOptions === embeddedChatClientOptions) this else ServerOptions(socketCommandListenerOptions, embeddedChatClientOptions)
	}
	
	override fun toString(): String {
		return "ServerOptions(socketCommandListenerOptions=$socketCommandListenerOptions, embeddedChatClientOptions=$embeddedChatClientOptions)"
	}
	
	class SocketCommandListenerOptions {
		var isEnabled = false
		var socket = 0
		var path = "/"
		
		constructor(enabled: Boolean, socket: Int, path: String) {
			isEnabled = enabled
			this.socket = socket
			this.path = path
		}
		
		constructor() {}
		
		fun withEnabled(enabled: Boolean): SocketCommandListenerOptions {
			return if (isEnabled == enabled) this else SocketCommandListenerOptions(enabled, socket, path)
		}
		
		fun withSocket(socket: Int): SocketCommandListenerOptions {
			return if (this.socket == socket) this else SocketCommandListenerOptions(isEnabled, socket, path)
		}
		
		fun withPath(path: String): SocketCommandListenerOptions {
			return if (this.path === path) this else SocketCommandListenerOptions(isEnabled, socket, path)
		}
		
		override fun toString(): String {
			return "ServerOptions.SocketCommandListenerOptions(enabled=$isEnabled, socket=$socket, path=$path)"
		}
	}
	
	class EmbeddedChatClientOptions {
		@JvmField var isEnabled = false
		@JvmField var channelName: String? = null
		@JvmField var identityProvider: OAuth2IdentityProvider? = null
		
		constructor(enabled: Boolean, channelName: String?, identityProvider: OAuth2IdentityProvider?) {
			isEnabled = enabled
			this.channelName = channelName
			this.identityProvider = identityProvider
		}
		
		constructor() {}
		
		fun withEnabled(enabled: Boolean): EmbeddedChatClientOptions {
			return if (isEnabled == enabled) this else EmbeddedChatClientOptions(enabled, channelName, identityProvider)
		}
		
		fun withChannelName(channelName: String): EmbeddedChatClientOptions {
			return if (this.channelName === channelName) this else EmbeddedChatClientOptions(isEnabled, channelName, identityProvider)
		}
		
		fun withIdentityProvider(identityProvider: OAuth2IdentityProvider): EmbeddedChatClientOptions {
			return if (this.identityProvider === identityProvider) this else EmbeddedChatClientOptions(isEnabled, channelName, identityProvider)
		}
		
		override fun toString(): String {
			return "ServerOptions.EmbeddedChatClientOptions(enabled=$isEnabled, channelName=$channelName, identityProvider=$identityProvider)"
		}
	}
	
	companion object {
		@JvmStatic
		fun fromArgs(args: Array<String>): ServerOptions {
			var eccOpts = EmbeddedChatClientOptions()
			var sclOpts = SocketCommandListenerOptions()
			for (arg in args) {
				if (arg.startsWith("--channel_name=")) eccOpts = eccOpts.withChannelName(arg.replaceFirst("--channel-name=".toRegex(), "")).withEnabled(true)
				if (arg.startsWith("--listen_socket=")) sclOpts = sclOpts.withSocket(arg.replaceFirst("--listen_socket=".toRegex(), "").toInt()).withEnabled(true)
				if (arg.startsWith("--listen_path=")) sclOpts = sclOpts.withPath(arg.replaceFirst("--listen_path=".toRegex(), "")).withEnabled(true)
			}
			return ServerOptions()
					.withEmbeddedChatClientOptions(eccOpts)
					.withSocketCommandListenerOptions(sclOpts)
		}
	}
}