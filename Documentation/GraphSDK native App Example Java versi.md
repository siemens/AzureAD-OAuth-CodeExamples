# GraphSDK native App Example Java version 

## Re-using the code

If you wish to re-use the code for another application, please take note of
the following:

### Dependencies

The code was developed in Eclipse with Java-SE 14 on OpenJDK 1.8. 
Dependencies were imported using Maven, in particular:
-Microsoft Graph 2.0.0
-Msal4j 1.7.0

The details can be seen in the pom.xml file in the source code

This application works cross-platform (Windows, Linux, macOS), but it has only
been tested on Windows 10. There might be adjustments necessary to the file 
that the token cache creates, but as that part of the code has to be redone 
anyways, it can implemented platform dependent or independent, however it is deemed
necessary. 

### Configuration

The strings in the beginning of the `OAuthExample` class  
region must be set to the correct values as documented in the code.

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

### Token Cache

In the `OAuthExample` class exists an inner static class `SimpleTokenCache` which
demonstrates on how a token cache functions. It should however _not_ be used in 
productive code, as it is saving the tokens in plain text on disc, which is a 
security risk. The requested refresh tokens have a long life time and should be 
handled similar to credentials. If you want to implement a token cache in productive
code, use encryption, file access rules or other means to protect this data accordingly.

The cache is used to check for access/refresh tokens whenever a request is made towards 
the Graph API. Before the request is done, the `beforeCacheAccess` is called, which is used
to deserialize the cache and populate the token cache of the `PublicClientApplication`.

After a request is done the `afterCacheAccess` is called, which can be used to persist the
tokens present in the token cache.

### User account management

In order to acquire tokens silently from the cache, you need to specify an IAccount.
There are multiple ways to handle this.

The example program has a quite simple implementation which uses the first IAccount 
in the cache if available. This will be the first user account giving consent to 
use the application.

If your program is used with multiple Accounts, you should
probably implement a more sophisticated way of choosing a UPN and looking for
matching accounts in the cache (as exposed by `pca.GetAccounts`).


## Code Analysis

For a general introduction to the sample, refer to the overview document which is
shared for multiple programming languages. This part will only analyze the specific
implementation in Java. 

### OAuthExample class

In the `Main`-Method of the class, the token cache is initialized. 
Afterwars a `PublicClientApplication` instance is initialized with the configured `Authority`
and the token cache.

#### Retrieve cached accounts

The `GetAccounts` call retrieves the accounts of the tokens in the
token cache. There are two possibilities:

1. There are no cached tokens (e.g., on first run of the application). The IAccount is 
   set to `null`. This will necessarily trigger an interactive authentication.
2. There are cached tokens. In this case, the first IAccount found is used in the
   following. Usually, this allows for silent authorization (see below).

#### Construct GraphServiceClient

The `GraphServiceClient` instance `client` is the endpoint for all requests
against the Graph API.

Since `GraphServiceClient`s cannot perform authentication themselves, they
rely on an `IAuthenticationProvider` to handle authentication.

In this example, the `IAuthenticationProvider` interface is implemented in 
the `CodeGrantAuthenticator` class (see below), which is subsequently used as an Authentication 
Provider for the Graph client.

#### Sample graph request

This is a simple example which retrieves some mails of the user from the Graph
API and lists them on the standard out channel. It uses an ICallback to process 
the result of the request.

### CodeGrantAuthenticator class

The `CodeGrantAuthenticator` is a simple implementation of the `IAuthenticationProvider`
interface, which implements the method `authenticateRequest` to add a bearer token to an
http request. 

The aim of this method is to acquire a valid access token for the given `IAccount`.
It relies on the token cache of the `IPublicClientApplication` instance
`pca`. (Recall that this token cache is synchronized to a file.)

First, the code tries to acquire a token silently: `AcquireTokenSilent`. If
there is an access token for the given `IAccount` granting the required `scopes`, it
will be returned without user interaction.

If the access token is expired (or close to expiring), the
`acquireTokenSilently` call will use a refresh token from the token cache (if
present) to acquire a new access token.

If silent token acquisition fails, a `MsalException` is thrown. This
could happen for example in the following cases:
- No tokens for the given `IAccount` are present in the cache.
- There are tokens for the given `IAccount`, but none of them grant the desired scopes.
- There is an access token with the desired `IAccount` and claims, but it is expired
  and there is no refresh token.
- There is an access token with the desired `IAccount` and claims, but it is expired
  and trying to use the refresh token to get a new access token failed (e.g.,
  because the refresh token is expired, too).

If silent token acquisition was successful, the access token is used to
authenticate the HTTP request. Otherwise, the aforementioned exception is thrown and
`acquireToken` is called. This opens a browser window where the
user is asked to authenticate themselves.

If the user has not given consent to the desired scopes, they will be asked to
grant this consent to your application after they have successfully
authenticated.

In the end, the variable `accessToken` represents the token which has been
retrieved (in case that retrieval was successful). At this point, it would be
a good idea to verify that its `Scopes` property contains the desired scopes.
Note that the token could always contain more scopes than the application has
requested. This verification has not been implemented in this application to
keep the code brief.

Finally, the access token is injected into the HTTP request generated by the
GraphServiceClient which ensures that the request is authenticated.


