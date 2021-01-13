/*
* Copyright (c) Siemens AG, 2020-2022
*
* This work is licensed under the terms of MIT.  See
* the LICENSE file in the top-level directory.
* Authors:
*  OneAD team in cooperation with Comma Soft AG 
*/


let accountId = "";

// Create the main myMSALObj instance
// configuration parameters are located at authConfig.js
const myMSALObj = new msal.PublicClientApplication(msalConfig);

// Register handleResponse to be called on OAuth redirects
myMSALObj.handleRedirectPromise().then(handleResponse).catch(err => {
    console.error(err);
});

function handleResponse(resp) {
    if (resp !== null) {
		// resp present -> this is a redirect from login
        accountId = resp.account.homeAccountId;
        showWelcomeMessage(resp.account);
    } else {
		// no resp present -> no redirect
		// check if there are any cached accounts
        const currentAccounts = myMSALObj.getAllAccounts();
        if (!currentAccounts || currentAccounts.length < 1) {
            return;
        } else if (currentAccounts.length >= 1) {
			// Choose first available account
            accountId = currentAccounts[0].homeAccountId;
            showWelcomeMessage(currentAccounts[0]);
        }
    }
}

/*
async function signInPopup()
{
	return myMSALObj.loginPopup(tokenRequestParams).
		then(handleResponse).
		catch(console.log);
}
*/

async function signInRedirect()
{
	return myMSALObj.loginRedirect(tokenRequestParams);
}


function signOut() {
    const logoutRequest = {
        account: myMSALObj.getAccountByHomeId(accountId)
    };

    myMSALObj.logout(logoutRequest);
}

/*
async function getTokenPopup(request, account) {
    request.account = account;
    return await myMSALObj.acquireTokenSilent(request).catch(async (error) => {
        console.log("silent token acquisition fails.");
        if (error instanceof msal.InteractionRequiredAuthError) {
            console.log("acquiring token using popup");
            return myMSALObj.acquireTokenPopup(request).catch(error => {
                console.error(error);
            });
        } else {
            console.error(error);
        }
    });
}
*/

async function getTokenRedirect(request, account) {
    request.account = account;
    return await myMSALObj.acquireTokenSilent(request).catch(async (error) => {
        console.log("silent token acquisition fails.");
        if (error instanceof msal.InteractionRequiredAuthError) {
            // fallback to interaction when silent call fails
            console.log("acquiring token using redirect");
            myMSALObj.acquireTokenRedirect(request);
        } else {
            console.error(error);
        }
    });
}
