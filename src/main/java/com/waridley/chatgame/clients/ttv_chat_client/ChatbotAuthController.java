package com.waridley.chatgame.clients.ttv_chat_client;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.waridley.chatgame.backend.DesktopAuthController;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Deprecated
public class ChatbotAuthController extends DesktopAuthController {
	
	private OAuth2IdentityProvider provider;
	private TokenHandler tokenHandler;
	
	ChatbotAuthController(TokenHandler tokenHandler) {
		super();
		this.tokenHandler = tokenHandler;
	}
	
	ChatbotAuthController(TokenHandler tokenHandler, String infoURL) throws URISyntaxException {
		super(infoURL);
		this.tokenHandler = tokenHandler;
	}
	
	@Override
	public void startOAuth2AuthorizationCodeGrantType(OAuth2IdentityProvider provider, String redirectUrl, List<Object> scopes) {
		this.provider = provider;
		try {
//			String authURLString = URLEncoder.encode(this.provider.getAuthenticationUrl(redirectUrl, scopes, null), StandardCharsets.UTF_8.toString());
//			InfoPageHandler infoHandler = new InfoPageHandler(authURLString);
			
			String authURLString = provider.getAuthenticationUrl(redirectUrl, scopes, null);
			System.out.println("authURLString: " + authURLString);
			InfoPageHandler infoHandler = new InfoPageHandler();
			String infoString = redirectUrl.split("#")[0].split("\\?")[0] + "/info.html";
			this.infoURI = new URI(URLDecoder.decode(infoString, StandardCharsets.UTF_8.toString()));
			System.out.println("Info URI: " + this.infoURI);
			
			URI redirectURI = new URI(redirectUrl);
			int listenPort = redirectURI.getPort();
			if(listenPort < 0) listenPort = 80;
			String listenPath = redirectURI.getPath();
			if(listenPath == null || listenPath.equals("")) listenPath = "/";
			
			HttpServer server = HttpServer.create(new InetSocketAddress(listenPort), 0);
			server.createContext(listenPath, new RedirectHandler(this::onReceivedCode));
			server.createContext(redirectURI.getPath() + "/info.html", infoHandler);
			server.start();
			
			super.startOAuth2AuthorizationCodeGrantType(provider, redirectUrl, scopes);
		} catch(IOException e) {
			handle(e);
		} catch(URISyntaxException e) {
			handle(e);
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
	public void handle(HttpExchange exchange) throws IOException {
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
		exchange.sendResponseHeaders(200, response.length());
		exchange.getResponseBody().write(response.getBytes());
		exchange.getResponseBody().close();
		
		String query = uri.getQuery();
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

class InfoPageHandler implements HttpHandler {
	
	private String authUrl;
	
	public InfoPageHandler() { authUrl = null; }
	
	public InfoPageHandler(String authenticationUrl) {
		this.authUrl = authenticationUrl;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI reqURI = exchange.getRequestURI();
		String[] queryParams = reqURI.getQuery().split("&");
		for(String param : queryParams) {
			if(param.startsWith("authurl=")) {
				authUrl = param.replaceFirst("authurl=", "");
				System.out.println("Encoded authUrl: " + authUrl);
				authUrl = URLDecoder.decode(authUrl, StandardCharsets.UTF_8.toString()).replace(' ', '+');
			}
		}
		String response =
			"<html>" +
			"<head>" +
			"</head>" +
			"<body>" +
			"<h1>Log in to your desired chat bot account</h1>" +
			"The following link will take you to the Twitch authentication page to log in.<br>" +
			"If you do not want to use your main account for the chat bot, you can either:<br>" +
			"<p>    1) Click \"Not you?\" on that page, however, this will permanently change the account you are logged into on Twitch until you manually switch back.</p>" +
			"<p>    2) Right-click this link, and open it in a private/incognito window. This will allow you to stay logged in to Twitch on your main account in normal browser windows.</p>" +
			"<a href=" + authUrl + ">" + authUrl + "</a>" +
			"</body>" +
			"</html>";
		exchange.sendResponseHeaders(200, response.length());
		exchange.getResponseBody().write(response.getBytes());
		exchange.getResponseBody().close();
	}
}
