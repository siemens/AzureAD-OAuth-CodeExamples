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



// Config object to be passed to Msal on creation
const msalConfig = {
    auth: {
        clientId: "00000000-1111-0000-0000-111111111111", //replace with your ClientID
		authority: "https://login.microsoftonline.com/00000000-1111-2222-3333-444444444444", //replace with your TenantID
		// redirectUri
    },
    cache: {
		// This configures where your cache will be stored
		// Other option: localStorage
        cacheLocation: "sessionStorage",
		storeAuthStateInCookie: false, // Set this to "true" if you are having issues on IE11 or Edge
    },
    system: {
        loggerOptions: {
            loggerCallback: (level, message, containsPii) => {
                if (containsPii) {	
                    return;	
                }	
                switch (level) {	
                    case msal.LogLevel.Error:	
                        console.error(message);	
                        return;	
                    case msal.LogLevel.Info:	
                        console.info(message);	
                        return;	
                    case msal.LogLevel.Verbose:	
                        console.debug(message);	
                        return;	
                    case msal.LogLevel.Warning:	
                        console.warn(message);	
                        return;	
                }
            }
        },
		// If true, personally identifiable information (PII) is included in logs.	
		piiLoggingEnabled: false
	}
};

const tokenRequestParams = {
    scopes: ["openid", "profile", "User.Read", "Mail.Read", "Mail.Read.Shared"]
};
