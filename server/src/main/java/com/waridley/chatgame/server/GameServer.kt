package com.waridley.chatgame.server

import com.github.ajalt.clikt.core.CliktCommand
import com.waridley.chatgame.api.backend.GameStorageInterface
import com.waridley.chatgame.game.Game
import com.waridley.chatgame.ttv_chat_client.TwitchChatGameClient
import com.waridley.ttv.TtvStorageInterface
import org.slf4j.LoggerFactory

class GameServer(private val ttvBackend: TtvStorageInterface, private val gameBackend: GameStorageInterface, private val options: ServerOptions): CliktCommand("game-server") {
	private var game: Game? = null
	private var ttvChatClient: TwitchChatGameClient? = null
	private var socketCommandListener: SocketCommandListener? = null
	@JvmField val commandExecutive: CommandExecutive = CommandExecutive(ttvBackend, gameBackend)
	fun start() {
		game = Game()
		val eccOpts = options.embeddedChatClientOptions
		if (eccOpts.isEnabled) {
			ttvChatClient = TwitchChatGameClient(eccOpts.identityProvider, eccOpts.channelName, EmbeddedCommandMediator(commandExecutive))
		}
		val sclOpts = options.socketCommandListenerOptions
		if (sclOpts.isEnabled) {
			socketCommandListener = SocketCommandListener(EmbeddedCommandMediator(commandExecutive), sclOpts)
		}
		log.info("Started game server with options: $options")
	}
	
	companion object {
		private val log = LoggerFactory.getLogger(GameServer::class.java)
	}
	
	override fun run() {
		start()
	}
	
}