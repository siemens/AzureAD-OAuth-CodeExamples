# GraphSDK native App Example

## Overview

This is an example application that retrieves the mails of the logged
in user from the Graph API.

Log-in is performed via an Authorization Code Grant Flow with PKCE. The
details of this flow are handled by libraries (Microsoft GraphSDK).

In order to make authorized requests against the Graph API, an access token is
used. This example application handles local storage of access and refresh
tokens in a cache and automatically tries to acquire a new access token from a 
refresh token if necessary. This minimizes the necessity for interactive user 
login, even across multiple runs of the application. 

## Application structure

### Configuration

All configuration settings are stored in variables at the beginning of the
class containing the 'Main'-Method of the program.

#### Redirect URI

This value must match one of the redirect URIs configured in the application
registration exactly.

You must redirect to the localhost on HTTP(s). In the background, a web server is
started by the application. The authentication provider will redirect to this
web server, giving the application access to the required data.

In this example, `http://localhost` is used. As there is no port specified,
the used library will automatically use a free port ("any port").

However, you cannot use any port binding with B2C or ADFS 2019 accounts as of
2020-09-08. In this case, you must specify a free port to be used, e.g.,
`http://localhost:52021` in both code and app registration.

#### Scopes

In the example, there are two scopes requested explicitly.

1. The `offline_access` scope must always be requested explicitly if you wish
   to obtain a refresh token (which is usually the case). Without the refresh
   token, the user will have to perform an interactive sign-in frequently.
2. The scope `https://graph.microsoft.com/mail.read` is application specific.
   In this example, it grants the application access to the users mails
   (`client.Me.Messages`). If you want to access a shared mailbox instead, you 
   need to specify the 'mail.read.shared' scope instead and use 
   'client.Users[upn].Messages' instead. 

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

The `Main` method consists of the following steps:

1. An `IPublicClientApplication` instance `pca` is generated from the configured values.
2. The token cache is connected to the pca
3. An Authentication Provider is instanciated
4. The Graph Client is initialized with the Authentication Provider
5. A GraphAPI Call is done via the Graph Client

As there are some differences on the specifics of each step depending on the programming 
language, there are more detailed descriptions of each step in the respective documentation.



