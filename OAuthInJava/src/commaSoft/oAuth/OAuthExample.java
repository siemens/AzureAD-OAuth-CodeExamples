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
	private static final String authority = "https://login.microsoftonline.com/27db00a0-427e-4f65-acc7-191ca3302345"; // alexcomma#
	// Application (client) ID from Azure
	private static final String clientID = "11044352-a17d-481d-9226-11bd836e4828"; // mw-wia-appreg
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
		PublicClientApplication pca = PublicClientApplication.builder(clientID)
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