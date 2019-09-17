package com.waridley.chatgame.ttv_integration;

import com.github.philippheuer.credentialmanager.domain.AuthenticationController;
import com.github.philippheuer.credentialmanager.domain.Credential;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.List;

public class LocalAuthenticationController extends AuthenticationController {
	
	private OAuth2IdentityProvider provider;
	private int port;
	private TokenHandler tokenHandler;
	
	public LocalAuthenticationController(int port, TokenHandler tokenHandler) {
		super();
		this.port = port;
		this.tokenHandler = tokenHandler;
	}
	
	@Override
	public void startOAuth2AuthorizationCodeGrantType(OAuth2IdentityProvider oAuth2IdentityProvider, String redirectUrl, List<Object> scopes) {
		this.provider = oAuth2IdentityProvider;
		String authenticationUrl = provider.getAuthenticationUrl(redirectUrl, scopes, null).replace(' ', '+');
		
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(authenticationUrl));
				HttpServer server = HttpServer.create(new InetSocketAddress(6464), 0);
				RedirectHandler handler = new RedirectHandler(this::getOAuth2Token);
				server.createContext("/", handler);
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
	
	public void getOAuth2Token(String code) {
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
	
	private TokenRetriever tokenRetriever;
	
	public RedirectHandler(TokenRetriever tokenRetriever) {
		this.tokenRetriever = tokenRetriever;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		URI uri = exchange.getRequestURI();
		String response = "Handling redirect";
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
		tokenRetriever.getToken(code);
	}
	
	interface TokenRetriever {
		void getToken(String code);
	}
}