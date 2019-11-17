/**
 * MIT License
 *
 * Copyright (c) 2019 Kevin day
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.waridley.credentials

import com.github.philippheuer.credentialmanager.domain.AuthenticationController
import com.github.philippheuer.credentialmanager.identityprovider.OAuth2IdentityProvider
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * An AuthenticationController which uses java.awt.Desktop.browse() to send the user to the identity provider's authentication URL.
 * If Desktop is not supported, it will print an error telling the user they may be able to paste the URL in a browser to continue authentication.
 */
class DesktopAuthController : AuthenticationController {
	/**
	 * The URI of an info page that redirects the user to the authentication URL after giving them some explanation.
	 */
	protected var infoURI: URI?
	
	/**
	 * Creates a DesktopAuthenticationController with no infoURL.
	 * The user will be directed straight to the identity provider's authentication URL.
	 */
	constructor() {
		infoURI = null
	}
	
	/**
	 * Creates a DesktopAuthController with an info page to direct the user to instead of sending them directly to the identity provider's authentication URL.
	 * The authentication URL will be added to the query of the infoURL, and the info page should contain some mechanism to redirect the user there.
	 *
	 * @param infoURL The URL of the page to send the user to first
	 * @throws URISyntaxException if a URI can't be created with infoURL
	 */
	constructor(infoURL: String?) {
		infoURI = URI(infoURL)
	}
	
	override fun startOAuth2AuthorizationCodeGrantType(provider: OAuth2IdentityProvider, redirectUrl: String, scopes: List<Any>) { //getAuthenticationUrl() does not URL-encode the scopes for some reason
		val authURL = provider.getAuthenticationUrl(redirectUrl, scopes, null)
		//				.replace(' ', '+'); //set scopeSeparator to "+" in RefreshingProvider for testing
		try {
			val browseURI: URI
			if (infoURI != null) { //infoURI is present, send user there instead of directly to auth page
//get parts of infoURI in order to add authURL to query
				val scheme = infoURI!!.scheme
				val userInfo = infoURI!!.userInfo
				val host = infoURI!!.host
				val port = infoURI!!.port
				val path = infoURI!!.path
				var query = infoURI!!.query
				val fragment = infoURI!!.fragment
				//add authURL to query
				if (query == null) query = "" else query += "&"
				query += "authurl=" + URLEncoder.encode(authURL, StandardCharsets.UTF_8.toString())
				browseURI = URI(scheme, userInfo, host, port, path, query, fragment)
			} else { //no infoURI present, send user to auth page
				browseURI = URI(authURL)
			}
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(browseURI)
			} else {
				handleDesktopUnsupported(browseURI)
			}
		} catch (e: URISyntaxException) {
			handle(e)
		} catch (e: IOException) {
			handle(e)
		}
	}
	
	/**
	 * Override this method to change the response when Desktop.Action.BROWSE is unsupported.
	 * By default it uses System.err.println() to print the URL and explain that it may be able to be pasted into a browser.
	 *
	 * @param browseURI The URI that would have been passed to Desktop.browse()
	 */
	protected fun handleDesktopUnsupported(browseURI: URI) {
		System.err.println(
				"Desktop is not supported! Cannot open browser for authentication.\n" +
						"You can paste the following URL into a web browser:\n\n" +
						"  " + browseURI.toString() + "\n\n" +
						"and if you are able to reach that page, then you may still be able to log in to your account."
		)
	}
	
	/**
	 * Override to implement your own URISyntaxException handler.
	 *
	 * @param e The IOException thrown by startOAuth2AuthorizationCodeGrantType()
	 */
	protected fun handle(e: URISyntaxException) {
		e.printStackTrace()
	}
	
	/**
	 * Override to implement your own IOException handler.
	 *
	 * @param e The IOException thrown by startOAuth2AuthorizationCodeGrantType()
	 */
	protected fun handle(e: IOException) {
		e.printStackTrace()
	}
}