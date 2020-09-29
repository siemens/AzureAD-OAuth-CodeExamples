# GraphSDKWithTokenCache Example (.NET Core 3.1 Application in C#)

## Overview

This is an example application that retrieves the last 10 mails of the logged
in user from the Graph API.

Log-in is performed via an Authorization Code Grant Flow with PKCE. The
details of this flow are handled by libraries.

In order to make authorized requests against the Graph API, an access token is
used. This example application handles local storage of access and refresh
tokens and automatically tries to acquire a new access token from a refresh
token if necessary. This minimizes the necessity for interactive user login,
even across multiple ru nsof the application. 

This application works cross-platform (Windows, Linux, macOS), but it has only
been tested on Windows 10.

## Application structure

### Configuration

All configuration settings are stored in variables at the beginning of the
`Program` class.

#### Redirect URI

This value must match one of the redirect URIs configured in the application
registration.

You must redirect to the localhost on HTTP. In the background, a web server is
started by the application. The authentication provider will redirect to this
web server, giving the application access to the required data.

In this example, `http://localhost` is used. As there is no port specified,
the used library will automatically use a free port ("any port").

However, you cannot use any port binding with B2C or ADFS 2019 accounts as of
2020-09-08. In this case, you must specify a free port to be used, e.g.,
`http://localhost:52021`.

#### Scopes

In the example, there are two scopes requested explicitly.

1. The `offline_access` scope must always be requested explicitly if you wish
   to obtain a refresh token (which is usually the case). Without the refresh
   token, the user will have to perform an interactive sign-in frequently.
2. The scope `https://graph.microsoft.com/mail.read` is application specific.
   In this example, it grants the application access to the users mails
   (`client.Me.Messages`).

In a different application, you should generally request 1. and replace 2.
by the scopes the application needs. These could be multiple scopes, but they
must be from the same API (MS Graph in this case).

The `openid` and `profile` scopes will be requested automatically, so they are
left out in this example, but they should be in the list of scopes to check
for if you decide to verify that you have obtained the desired scopes.

It is _not_ recommended to use the `https://graph.microsoft.com/.default`
scope because of its cumbersome behavior when permissions are changed (in the
AAD App Registration).

### Main method

The `Main` method consists of the following regions:

#### Initialize client application

An `IPublicClientApplication` instance `pca` is generated from the configured values.

#### Initialize cache

The `pca` has a `UserTokenCache` which stores tokens in memory. The
`Microsoft.Identity.Client.Extensions.Msal` package provides an encapsulated
mechanism to persist this cache to a file. In this region, the cache is hooked
up to the `UserTokenCache` of `pca`. Afterward, one does not need to take care
of the file cache -- all changes are synchronized to the file cache
automatically. Also, if the cache already exists on application startup, its
contents are read and stored in the `UserTokenCache`.

#### Retrieve cached accounts

The access and refresh tokens obtained from the used flow are bound to a user,
identified by its user principal name (UPN). In order to reuse tokens from the
cache, one must obtain the tokens belonging to the correct user.

The `GetAccountsAsync` call retrieves the accounts of the tokens in the
token cache. There are two possibilities:

1. There are no cached tokens (e.g., on first run of the application). The
   user is asked to enter a UPN, which will later be used as a login hint (see
   below). This will necessarily trigger an interactive authentication.
2. There are cached tokens. In this case, the first UPN found is used in the
   following. Usually, this allows for silent authorization (see below).

#### Construct GraphServiceClient

The `GraphServiceClient` instance `client` is the endpoint for all requests
against the Graph API.

Since `GraphServiceClient`s cannot perform authentication themselves, they
rely on an `IAuthenticationProvider` to handle authentication.

An `IAuthenticationProvider` has an `AuthenticateRequestAsync` method which is
called before the `GraphServiceClient` makes a request to the Graph API. The
`AuthenticateRequestAsync` method has the job of modifying the request in such
a way that it is authenticated.

The `DelegateAuthenticationProvider` is the most simple implementation of an
`IAuthenticationProvider`: It takes an `AuthenticateRequestAsyncDelegate` (via
its constructor) that handles the call to `AuthenticateRequestAsync`.

Since authentication requires a specific Header containing an access token, the
specific implementation here acquires an access token and adds it to the
headers of the HTTP request (`msg.Headers.Authorization = ...`) (see below for
details).

In summary, the control flow is as follows:

1. A request to the Graph API is made via the `GraphServiceClient` instance `client`.
2. `client` prepares a HTTP request to the Graph API.
3. In order to obtain an authenticated request, `client` relies on its
   `IAuthenticationProvider`, which is `authProvider` here.
   `client` calls the `AuthenticateRequestAsync` method of `authProvider` and
   passes the prepared HTTP request as a parameter.
4. `authProvider` is in fact a `DelegateAuthenticationProvider`. In this
   implementation of `IAuthenticationProvider`, the call to
   `IAuthenticationProvider.AuthenticateRequestAsync` is handled by a
   `AuthenticateRequestAsyncDelegate`. This delegate adds the access token to
   the original HTTP request and returns. Then `AuthenticateRequestAsync`
   returns, too.
5. Control is passed back to `client`. The original HTTP request has been
   altered in such a way that it is authenticated. `client` will now perform
   this request.

#### Sample graph request

This is a simple example which retrieves some mails of the user from the Graph
API and lists them in the console.

### Token acquisition

Here, we describe the `AuthenticationRequestAsyncDelegate` used above. The aim
of this delegate is to acquire a valid access token for the given `userPrincipalName`.
It relies on the `UserTokenCache` of the `IPublicClientApplication` instance
`pca`. (Recall that this token cache is synchronized to a file.)

First, the code tries to acquire a token silently: `AcquireTokenSilent`. If
there is an access token for the given UPN granting the required `scopes`, it
will be returned without user interaction.

If the access token is expired (or close to expiring), the
`AcquireTokenSilent` call will use a refresh token from the token cache (if
present) to acquire a new access token.

If silent token acquisition fails, a `MsalUiRequiredExcpetion` is thrown. This
could happen for example in the following cases:
- No tokens for the given UPN are present in the cache.
- There are tokens for the given UPN, but none of them grant the desired scopes.
- There is an access token with the desired UPN and claims, but it is expired
  and there is no refresh token.
- There is an access token with the desired UPN and claims, but it is expired
  and trying to use the refresh token to get a new access token failed (e.g.,
  because the refresh token is expired, too).

If silent token acquisition was successful, the access token is used to
authenticate the HTTP request. Otherwise, the aforementioned exception is thrown and
`AcquireTokenInteractive` is called. This opens a browser window where the
user is asked to authenticate themselves.

If the user has not given consent to the desired scopes, they will be asked to
grant this consent to your application after they have successfully
authenticated.

The `WithLoginHint(userPrincipalName)` ensures that the specified UPN is
displayed in the browser. The user can choose a different account, however.

In the end, the variable `accessToken` represents the token which has been
retrieved (in case that retrieval was successful). At this point, it would be
a good idea to verify that its `Scopes` property contains the desired scopes.
Note that the token could always contain more scopes than the application has
requested. This verification has not been implemented in this application to
keep the code brief.

Finally, the access token is injected into the HTTP request generated by the
GraphServiceClient which ensures that the request is authenticated.

## Re-using the code

If you wish to re-use the code for another application, please take note of
the following:

### Dependencies

The code requires the NuGet packages mentioned at the beginning of the code,
next to their respective `using` declarations.

In particular, the
[Microsoft.Identity.Client.Extensions.Msal](https://github.com/AzureAD/microsoft-authentication-extensions-for-dotnet)
package is used to provide serialization of the token cache to a file.

### Configuration

The strings in the `Client application configuration` and `Token cache
configuration` region must be set to the correct values as documented in the
code.

### Exceptions

There is almost no exception handling in the code to keep it concise.
Depending on your requirements and the environment where your code runs, you
should particularly keep in mind the following potential problems:

- Token cache file I/O problems
- Argument validation if some of the configuration variables are exposed
- Network problems
- Interactive authentication problems (user denies consent, user cancels
  authentication, ...)

In general, all the calls that rely on external factors (I/O, network, user
interaction, ...) are async in this example.

### User account management

In order to acquire tokens silently from the cache, you need to specify a UPN.
There are multiple ways to handle this UPN.

The example program has a quite simple implementation which uses the first UPN
that comes along if available. Obviously, there is no control of the concrete
UPN which is used.

On the one hand, if your program is always to be used with the same UPN, you
could store the UPN in a configuration file.

On the other hand, if your program is used with different UPNs, you should
probably implement a more sophisticated way of choosing a UPN and looking for
matching accounts in the cache (as exposed by `pca.GetAccountsAsync`).

### `AuthenticateRequestAsyncDelegate`

This delegate is defined as 
`public delegate Task AuthenticateRequestAsyncDelegate(HttpRequestMessage request);`

In this example, an instance is created by a static private method.
One could also pass the async lambda to the `DelegateAuthenticationProvider`
constructor inline, or one could have a instance method with the signature
`Task MyMethod (HttpRequestMessage request)` somewhere and pass `MyMethod` to
the mentioned constructor. This does _not_ work here because we need to inject
the `pca` and `userPrincipalName` variables.
