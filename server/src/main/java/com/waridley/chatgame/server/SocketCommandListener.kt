package com.waridley.chatgame.server

import com.waridley.chatgame.api.CommandMediator
import com.waridley.chatgame.server.ServerOptions.SocketCommandListenerOptions

class SocketCommandListener (
		private val commandMediator: CommandMediator? = null,
		private val options: SocketCommandListenerOptions? = null
)