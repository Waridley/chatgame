package com.waridley.chatgame.server;

import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@With
@Getter
@Setter
@ToString
class ServerOptions {
	
	private SocketCommandListenerOptions socketCommandListenerOptions = new SocketCommandListenerOptions();
	private EmbeddedChatClientOptions embeddedChatClientOptions = new EmbeddedChatClientOptions();
	
	public static ServerOptions fromArgs(String[] args) {
		ServerOptions.EmbeddedChatClientOptions eccOpts = new ServerOptions.EmbeddedChatClientOptions();
		
		ServerOptions.SocketCommandListenerOptions sclOpts = new ServerOptions.SocketCommandListenerOptions();
		
		for(String arg : args) {
			if(arg.startsWith("--channel_name="))
				eccOpts = eccOpts.withChannelName(arg.replaceFirst("--channel-name=", "")).withEnabled(true);
			if(arg.startsWith("--listen_socket="))
				sclOpts = sclOpts.withSocket(Integer.parseInt(arg.replaceFirst("--listen_socket=", ""))).withEnabled(true);
			if(arg.startsWith("--listen_path="))
				sclOpts = sclOpts.withPath(arg.replaceFirst("--listen_path=", "")).withEnabled(true);
		}
		
		return new ServerOptions()
				.withEmbeddedChatClientOptions(eccOpts)
				.withSocketCommandListenerOptions(sclOpts);
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@With
	@Getter
	@Setter
	@ToString
	static class SocketCommandListenerOptions {
		private boolean enabled;
		private int socket = 0;
		private String path = "/";
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@With
	@Getter
	@Setter
	@ToString
	static class EmbeddedChatClientOptions {
		private boolean enabled;
		private String channelName;
		private OAuth2IdentityProvider identityProvider;
	}
}
