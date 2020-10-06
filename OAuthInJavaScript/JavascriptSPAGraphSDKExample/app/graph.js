/* Graph calls via Graph Client SDK */

const sharedMailBox = 'graph@alexcomma.onmicrosoft.com';

const authProvider =
{
	getAccessToken: async () => 
	{
		console.log('Graph API Auth middleware called.');

		const currentAcc = myMSALObj.getAccountByHomeId(accountId);

		if (currentAcc)
		{
			const response = await getTokenRedirect(tokenRequestParams, currentAcc).
				catch(console.log);
			return response.accessToken;
		}
	}
};

const graphClient = MicrosoftGraph.Client.initWithMiddleware({authProvider});

async function seeProfileRedirect()
{
	let profileData = await graphClient.api('/me').
		select('id,mail,businessPhones').get();
	updateUIWithProfileInfo(profileData);

}

async function readMailRedirect()
{
	let mailData = await graphClient.api('/me/messages').
		select('subject,from,bodyPreview').top(10).get();
	updateUIWithMessages(mailData, "personal mailbox");
}

async function readMailSharedRedirect()
{
	let sharedMailData = await graphClient.
		api(`/users/${sharedMailBox}/messages`).
		select('subject,from,bodyPreview').top(10).get();
	updateUIWithMessages(sharedMailData, sharedMailBox);
}
