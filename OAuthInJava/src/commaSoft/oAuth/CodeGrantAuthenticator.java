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

import java.net.*;
import java.util.*;

import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class CodeGrantAuthenticator implements IAuthenticationProvider {

	private IPublicClientApplication pca;
	private IAccount account;
	private Set<String> scopes;
	private String redirectUri;

	public CodeGrantAuthenticator(IPublicClientApplication pca, IAccount account, Set<String> scopes, String redirectUri) {
		this.pca = pca;
		this.account = account;
		this.scopes = scopes;
		this.redirectUri = redirectUri;
	}

	@Override
	public void authenticateRequest(IHttpRequest request) {
		String accessToken;
		accessToken = acquireToken(pca, account).accessToken();
		request.addHeader("Authorization", "Bearer " + accessToken);
	}


	public IAuthenticationResult acquireToken(IPublicClientApplication pca, IAccount account) {
		IAuthenticationResult result = null;
		boolean doInteractively = account == null;

		// if an account is supplied, try silent first.
		if (account != null) {
			try {
				SilentParameters silentParameters = SilentParameters.builder(scopes, account).build();

				// try to acquire token silently. This call will fail if the account is not present in the cache.
				result = pca.acquireTokenSilently(silentParameters).join();
			} catch (Exception ex) {
				// If the user is not cached, or has not given consent or the tokens are expired, the exception will be raised and interactive token acquisition will be started.
				if (ex.getCause() instanceof MsalException) {
					doInteractively = true;
				}
			}
		}
		//if silent acquisition fails or is not possible, do interactive acquisition
		if (doInteractively) {
			InteractiveRequestParameters parameters;
			try {
				parameters = InteractiveRequestParameters.builder(new URI(redirectUri)).scopes(scopes).build();
				result = pca.acquireToken(parameters).join();
			} catch (URISyntaxException e) {
			}
		}
		return result;
	}

}
