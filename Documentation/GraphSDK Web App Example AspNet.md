# GraphSDK web App example ASP.Net core

## Overview

This is an example application that retrieves the profile info of a
user and the profile picture and lets them send this information via E-Mail.

This example is based on the microsoft example found 
[here](https://github.com/microsoftgraph/aspnetcore-connect-sample).


Log-in is performed via an Authorization Code Grant Flow. The
details of this flow are handled by libraries (Microsoft GraphSDK).

## App registration

For this example, an app registration with a `web` redirect URI is required. 
In this example `https://localhost:44334/signin-oidc` was used.
Additionally, the following Logout URL is specified: `https://localhost:44334/Account/SignOut`
When using ASP.net core, you also have to check the box for `ID tokens`. 
 
The registration was set up as a single tenant application 
(`Accounts in this organzational directory only`)

As this example uses the Authorization Code Grant Flow (without PKCE), you also 
need an application secret. Remember to keep this secret confidental, and be 
reminded that it will only be shown once when creating it. In production environments
a certificate should be used instead of a client secret.

## Application structure

### Configuration

All configuration settings are stored in the `appsettings.json`. If the 
app registration was configured as stated below, there are no changes necessary
in this file. Additionally, you will need an `appsettings.<environment>.json` file
with the structure as given in the `appsettings.example.json`. Fill in your values
for those properties accordingly. Note, this file should not be covered in your
source control, as it contains the client secret (or certificate).

#### Redirect URI

This value must match one of the redirect URIs configured in the application
registration exactly.

You must redirect to the localhost on HTTP(s). In the background, a web server is
started by the application. The authentication provider will redirect to this
web server, giving the application access to the required data.

#### Scopes

In the example, there are two scopes requested explicitly.

1. `User.Read` to retrieve the profile information and profile picture.
2. `Mail.Send` to send mails with the logged in user as the sender.

It is _not_ recommended to use the `https://graph.microsoft.com/.default`
scope because of its cumbersome behavior when permissions are changed (in the
AAD App Registration).

### Authentication Logic

The relevant parts of the authentication logic are located in the `Services` folder.
The `GraphService` class encapsulates the GraphAPI calls, while the `GraphServiceClientFactory`
provides the `GraphClient`, which is using the `GraphAuthProvider` to authenticate requests.

All components are linked to the website via the `StartUp` class.