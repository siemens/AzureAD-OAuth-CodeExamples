# GraphSDK SPA example JavaScript

## Overview

This is an example application that retrieves the following information in the
context of the logged in user

-   Mail address and business phone
-   Last 10 mails in the personal mailbox
-   Last 10 mails in a shared mailbox which the user has access to

It is based on the [Microsoft
VanillaJSTestApp2.0](https://github.com/AzureAD/microsoft-authentication-library-for-js/tree/dev/samples/msal-browser-samples/VanillaJSTestApp2.0)
example with some simplifications and improvements.

Log-in is performed via an Authorization Code Grant Flow with PKCE extension.

## App registration

For this example, an app registration with a `SPA` redirect URI is required. 
The redirect URI should be the base URI where the application is served. With
the standard configuration, this is `http://localhost:30662`. All other
redirect URI configurations can remain at their respective defaults, i.e., no
Logout URL, no implicit grants, not treated as public client. The registration
should be set up as a single tenant application (`Accounts in this
organzational directory only`).

## Application initalization and start

In the `JavascriptSPAGraphSDKExample`, use `npm install` once to install the
required NPM modules. Use `npm start` to start a local web server on the
default port `30662` to serve the application.

In order to change the port, you can either edit the `server.js` or you can
use `node server.js -p PORT`.

The application was tested succesfully with node.js v12.18.3 on Windows 10.

## Application structure

### Configuration

All configuration settings are stored in the object `msalConfig` in
`app/authConfig.js`. The most important ones are

-   `auth.clientId`: The client ID of your application registration
-   `auth.authority`: Built from the tenant ID of the tenant where your
	application is registered. The format is `https://login.microsoftonline.com/TENANTID`
-   `auth.redirectUri`: Not configured in this example because the default is
	fine
-   `tokenRequestParams.scopes`: The MS Graph scopes needed for this
	application

See [msal.js configuration
documentation](https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-browser/docs/configuration.md)
for more details.

### Server

The server (`server.js`) is a very basic node.js server which servers the
static files in `app/`. It depends on a few npm modules as documented in the
`package.json`.

### Frontend

The frontend is implemented in `index.html` and `ui.js`. Note that the
`index.html` includes the following libraries from CDNs

-   [msal-browser.js](https://www.npmjs.com/package/msal)
-   [microsoft-graph-client](https://github.com/microsoftgraph/msgraph-sdk-javascript)
-   Bootstrap 4

### Business logic

`authConfig.js` contains all the configuration options.

`auth.js` handles OAuth flows.

`graph.js` makes requests to a Graph client (after the frontend buttons have
been clicked) and handles their responses.

### Redirects vs popups

The `msal-browser.js` supports two UX flows for logging in: Either via a
redirect (in the same browser tab) or via a pop-up (in a new tab/window).

In this example, only redirects are used. See [msal.js initialization
documentation](https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/dev/lib/msal-browser/docs/initialization.md#choosing-an-interaction-type)
for more information.

### Browser support

The application was tested successfully with Chrome v85 on Windows 10. Support
for other browsers has deliberatly not been included to reduce the overall
complexity.

In order to support browsers with limited JavaScript support such as Internet
Explorer, please consider

-   using a transpiler such as Babel to support modern features like async
	functions
-   using polyfills for the script libraries `msal-browser.js` and
	`microsoft-graph-client`, see the documentation linked above.
-   not using pop-ups on certain browsers, see [sign-in logic in Microsoft example](https://github.com/AzureAD/microsoft-authentication-library-for-js/blob/15c4d90860d9eeb28ea110ed37d3041ff895838e/samples/msal-browser-samples/VanillaJSTestApp2.0/app/default/auth.js#L41)

