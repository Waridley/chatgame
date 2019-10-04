package com.waridley.chatgame.server;

import com.waridley.chatgame.api.frontend.CommandMediator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SocketCommandListener {
	
	private CommandMediator commandMediator;
	
	private ServerOptions.SocketCommandListenerOptions options;
	
	
	//TODO: Implement socket server
}
