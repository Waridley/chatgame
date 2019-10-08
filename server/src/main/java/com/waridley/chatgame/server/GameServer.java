package com.waridley.chatgame.server;

import com.waridley.chatgame.api.backend.GameStorageInterface;
import com.waridley.chatgame.game.Game;
import com.waridley.chatgame.ttv_chat_client.TwitchChatGameClient;
import com.waridley.ttv.TtvStorageInterface;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameServer {
	
	private Game game;
	private GameStorageInterface gameBackend;
	private TtvStorageInterface ttvBackend;
	private ServerOptions options;
	private TwitchChatGameClient ttvChatClient = null;
	private SocketCommandListener socketCommandListener = null;
	
	@Getter
	private CommandExecutive commandExecutive;
	
	public GameServer(TtvStorageInterface ttvStorageInterface, GameStorageInterface gameStorageInterface, ServerOptions options) {
		this.gameBackend = gameStorageInterface;
		this.ttvBackend = ttvStorageInterface;
		this.options = options;
		commandExecutive = new CommandExecutive(ttvBackend, gameBackend);
	}
	
	
	public void start() {
		game = new Game();
		ServerOptions.EmbeddedChatClientOptions eccOpts = options.getEmbeddedChatClientOptions();
		if(eccOpts.isEnabled()) {
			ttvChatClient = new TwitchChatGameClient(eccOpts.getIdentityProvider(), eccOpts.getChannelName(), new EmbeddedCommandMediator(commandExecutive));
		}
		ServerOptions.SocketCommandListenerOptions sclOpts = options.getSocketCommandListenerOptions();
		if(sclOpts.isEnabled()) {
			socketCommandListener = new SocketCommandListener(new EmbeddedCommandMediator(commandExecutive), sclOpts);
		}
		log.info("Started game server with options: " + options.toString());
	}
	
}
