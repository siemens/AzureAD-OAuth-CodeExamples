/*
* Authentication module example, based on OAuth Authorization Code grant using refresh token
*
* Copyright (c) Siemens AG, 2020-2022
*
* Authors:
*  OneAD team in cooperation with Comma Soft AG 
*
* This work is licensed under the terms of MIT.  See
* the LICENSE file in the top-level directory.
*/


package commaSoft.oAuth;

import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.authentication.*;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.graph.requests.extensions.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class OAuthExample {

	// Directory (tenant) ID from Azure => java wants an "authority" property, which includes the whole URI
	private static final String authority = "https://login.microsoftonline.com/00000000-1111-2222-3333-444444444444"; // Enter tenantID
	// Application (client) ID from Azure
	private static final String clientID = "00000000-1111-0000-0000-111111111111"; // Enter ClientID
	// One of the configured redirect URIs for the "Auth Code Grant with PKCE" flow
	private static final String redirectUri = "http://localhost";

    // Scopes to be requested.
    // offline_access is necessary to obtain a refresh token and must be explicitly requested here.
    // It is not recommended to use the ".default" scope, see the docs.
	private static final Set<String> scopes = new HashSet<>
	(Arrays.asList(
            // The used library seems to always request openid and profile to ensure that you get the UPN
            // "openid", "profile",
            // Always request this scope if you need a refresh token
			"offline_access",
            // App specific scopes 
			"https://graph.microsoft.com/mail.read"));

	public static void main(String[] args) throws Exception {
		// Loads cache from file
		ITokenCacheAccessAspect tokenCache = new SimpleTokenCache(Paths.get("C:\\temp\\cache.json"));

		// By setting *TokenPersistence* on the PublicClientApplication,
		// MSAL will call *beforeCacheAccess()* before accessing the cache and
		// *afterCacheAccess()* after accessing the cache.
		IPublicClientApplication pca = PublicClientApplication.builder(clientID)
				.setTokenCacheAccessAspect(tokenCache)
				.authority(authority)
				.build();

		// get first account from cache, if no account is found, null will be given to the
		// AuthenticationProvider which has an extra method to handle the case when no account is present.
		IAccount account;
		try {
			account = pca.getAccounts().join().iterator().next();
		} catch (Exception ex) {
			account = null;
		}

		// Instanciate our own AuthenticationProvider
		IAuthenticationProvider authProv = new CodeGrantAuthenticator(pca, account, scopes, redirectUri);
		IGraphServiceClient client = GraphServiceClient.builder()
				.authenticationProvider(authProv)
				.buildClient();
		
		// Use Graph Client to access EMails
		client.me().messages().buildRequest().get(new ICallback<IMessageCollectionPage>() {
			@Override
			public void success(IMessageCollectionPage iMessageCollectionPage) {
				// Successful response, use it in your application
				for (Message message : iMessageCollectionPage.getCurrentPage()) {
					System.out.println("Found mail from " + message.from.emailAddress.address + " with subject: " + message.subject);
				}
			}
				//If call fails, handle the exception accordingly
			@Override
			public void failure(ClientException ex) {
			}
		});
	}

	/*
	 *         Very simple token cache. Saves the Access and Refresh token
	 *         unencrypted into a json file. DO NOT USE IN PRODUCTION Use encryption,
	 *         NTFS ACLs and/or other means to protect the access and refresh tokens. Refresh
	 *         tokens might be valid for a long time, they must not be leaked and 
	 *         should not be easily accessible by everyone!
	 */
	static class SimpleTokenCache implements ITokenCacheAccessAspect {
		private String data;
		private Path fileName;

		SimpleTokenCache(Path fileName) throws IOException {
			if (!Files.exists(fileName, LinkOption.NOFOLLOW_LINKS)) {
				Files.createFile(fileName);
			}
			this.data = Files.readString(fileName);
			this.fileName = fileName;
		}

		@Override
		public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
			iTokenCacheAccessContext.tokenCache().deserialize(data);
		}

		@Override
		public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
			data = iTokenCacheAccessContext.tokenCache().serialize();
			try {
				Files.write(fileName, data.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException ex) {
				System.out.println("Could not write cache to disk.");
			}
		}
	}

}
