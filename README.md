# OAuth Example Collection

## Overview

This repository contains multiple examples on how to implement different OAuth Flows in order to use the GraphAPI.
The examples are provided in different programming languages, which should represent common languages used for the different application types.

Currently, the following applications are provided as examples:

1.  Retrieve your Mails on native clients
2.  Login on a website to retrieve your profile information and send a mail containing that information

The first example uses the GraphSDK to retrieve mails from the personal mailbox (or a shared mailbox if minor adjustments are taken).
It uses the Authorization Code Grant flow with PKCE, so it does not require a secret. The example is provided in both C# (.NET core) and Java. 

The second example provides a website with a login. After login, the GraphSDK is used to query the GraphAPI for the user profile information and photo and is 
also used to optionally send an email with that information. This example uses the Authorization Code Grant flow, which requires a secret. The example is provided in
C# (ASP.NET core).

For a more detailed explanation on implementation details and how to (re)use the code, refer to the projects own documentation.
