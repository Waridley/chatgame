package com.waridley.chatgame.server

import com.waridley.chatgame.api.frontend.CommandMediator
import com.waridley.chatgame.server.ServerOptions.SocketCommandListenerOptions
import lombok.AllArgsConstructor

@AllArgsConstructor
class SocketCommandListener (
	private val commandMediator: CommandMediator? = null,
	private val options: SocketCommandListenerOptions? = null
)