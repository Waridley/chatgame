package com.waridley.chatgame.ttv_integration;

import com.github.philippheuer.credentialmanager.domain.AuthenticationController;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class LambdaAuthenticationController extends AuthenticationController  {
	
	private OAuth2IdentityProvider provider;
	private TokenHandler tokenHandler;
	
	public LambdaAuthenticationController(TokenHandler tokenHandler) {
		super();
		this.tokenHandler = tokenHandler;
	}
	
	@Override
	public void startOAuth2AuthorizationCodeGrantType(OAuth2IdentityProvider provider, String redirectUrl, List<Object> scopes) {
		this.provider = provider;
		String authenticationUrl = this.provider.getAuthenticationUrl(redirectUrl, scopes, null);
		
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(authenticationUrl.replace(' ', '+')));
				URI listenURI = new URI(redirectUrl);
				int port = listenURI.getPort();
				if(port < 0) port = 80;
				String path = listenURI.getPath();
				if(path == null || path.equals("")) path = "/";
				HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
				RedirectHandler handler = new RedirectHandler(this::onReceivedCode);
				server.createContext(path, handler);
				server.setExecutor(null);
				server.start();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Desktop is not supported! Cannot open browser for authentication");
		}
	}
	
	private void onReceivedCode(String code) {
		//System.out.println("Received code " + code + " -- Getting token");
		OAuth2Credential cred = provider.getCredentialByCode(code);
		//System.out.println("Token: " + cred.getAccessToken());
		super.getCredentialManager().addCredential("twitch", cred);
		tokenHandler.onReceivedToken(cred);
	}
	
	public interface TokenHandler {
		void onReceivedToken(OAuth2Credential token);
	}
	
	
}


class RedirectHandler implements HttpHandler {
	
	private String code;
	public String getCode() { return code; }
	
	private String[] scopes;
	public String[] getScopes() { return scopes; }
	
	private String state;
	public String getState() { return state; }
	
	private CodeHandler codeHandler;
	
	public RedirectHandler(CodeHandler codeHandler) {
		
		this.codeHandler = codeHandler;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		URI uri = exchange.getRequestURI();
		String response = new StringBuilder()
				.append("<html>")
				.append("<head>")
				.append("</head>")
				.append("<body>")
				.append("<h1>Success!</h1>")
				.append("Received authorization code. Getting token and joining chat.")
				.append("</body>")
				.append("</html>")
				.toString();
		try {
			exchange.sendResponseHeaders(200, response.length());
			exchange.getResponseBody().write(response.getBytes());
			exchange.getResponseBody().close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		String query = uri.getQuery();
		//System.out.println(query);
		String[] splitQuery = query.split("&");
		for(String s : splitQuery) {
			String[] splitField = s.split("=");
			String fieldName = splitField[0];
			String value = splitField[1];
			switch(fieldName) {
				case("code"):
					this.code = value;
					break;
				case("scope"):
					scopes = value.split(" ");
					break;
				case("state"):
					this.state = state;
					break;
				default:
					System.err.println("Unknown field in response");
			}
		}
		codeHandler.onReceivedCode(code);
	}
	
	interface CodeHandler {
		void onReceivedCode(String code);
	}
	
}
