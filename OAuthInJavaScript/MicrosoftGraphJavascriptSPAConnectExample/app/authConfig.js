// Config object to be passed to Msal on creation
const msalConfig = {
    auth: {
        clientId: "387383dc-1e3c-4b60-bcb7-0d89983bd902",
		authority: "https://login.microsoftonline.com/27db00a0-427e-4f65-acc7-191ca3302345",
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

// Add here the endpoints for MS Graph API services you would like to use.
const graphConfig = {
    graphMeEndpoint: "https://graph.microsoft.com/v1.0/me",
    graphMailEndpoint: "https://graph.microsoft.com/v1.0/me/messages"
};

const tokenRequestParams = {
    scopes: ["openid", "profile", "User.Read", "Mail.Read"]
};
