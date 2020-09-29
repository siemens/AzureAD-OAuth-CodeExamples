using System;
using System.Threading.Tasks; // async Main()
using System.Linq;

using Microsoft.Graph;
// From NuGet package Microsoft.Graph v3.10.0, which depends on
// Microsoft.Graph.Core which was used in v1.21.0

using Microsoft.Identity.Client; // From NuGet package Microsoft.Identity.Client v4.17.1
using Microsoft.Identity.Client.Extensions.Msal; // From NuGet package Microsoft.Identity.Client.Extensions.Msal v2.13.0

using System.Net.Http;
using System.Net.Http.Headers;
using System.Runtime.InteropServices;
using System.Collections.Generic;

namespace GraphSDKWithTokenCache
{
    class Program
    {
        #region Client application configuration
        // Directory (tenant) ID from Azure
        private const string tenantID = "27db00a0-427e-4f65-acc7-191ca3302345"; //alexcomma
        // Application (client) ID from Azure
        private const string clientID = "11044352-a17d-481d-9226-11bd836e4828"; //mw-wia-appreg
        // One of the configured redirect URIs for the "Auth Code Grant with PKCE" flow
        private const string redirectUri = "http://localhost";

        // Scopes to be requested.
        // offline_access is necessary to obtain a refresh token and must be explicitly requested here.
        // It is not recommended to use the ".default" scope, see the docs.
        private static readonly string[] scopes = new string[]
        {
            // The used library seems to always request openid and profile to ensure that you get the UPN
            // "openid", "profile",

            // Always request this scope if you need a refresh token
            "offline_access",
            
            // App specific scopes 
            "https://graph.microsoft.com/mail.read",            //For access to only own Mailbox
            "https://graph.microsoft.com/mail.read.shared",     //For access to shared/foreign + own Mailboxes
        };
        #endregion

        #region Token cache configuration
        // Name of the token cache file
        private const string tokenCacheFileName = "cache.bin";
        // Path of the directory for the token cache
        private const string tokenCacheDirectory = ".";
        #endregion

        #region Internal consts
        private const string AUTH_HEADER_PREFIX = "Bearer";
        #endregion

        static async Task Main()
        {
            #region Initialize client application
            var pca = PublicClientApplicationBuilder
                .Create(clientID)
                .WithTenantId(tenantID)
                .WithRedirectUri(redirectUri)
                .Build();
            #endregion

            #region Initialize cache
            Console.WriteLine("Initializing token cache ...");
            var storageCreationProperties = GetStorageCreationProperties(tokenCacheFileName,tokenCacheDirectory, clientID);
            var cacheHelper = await MsalCacheHelper.CreateAsync(storageCreationProperties);
            cacheHelper.RegisterCache(pca.UserTokenCache);
            #endregion

            #region Retrieve cached accounts
            Console.WriteLine("Getting accounts from token cache ...");
            var cachedAccounts = await pca.GetAccountsAsync();
            string userPrincipalName;

            if (!cachedAccounts.Any())
            {
                Console.WriteLine("No account present in token cache.");
                Console.WriteLine("Enter user UPN:");
                userPrincipalName = Console.ReadLine();
            }
            else
            {
                userPrincipalName = cachedAccounts.First().Username;

                Console.WriteLine("Accounts present in token cache.");
                Console.WriteLine($"The first one ({userPrincipalName}) will be used for authentication.");
            }
            #endregion

            #region Construct GraphServiceClient which uses the token cache for authentication
            var authProvider = new DelegateAuthenticationProvider(GetAuthDelegate(pca, userPrincipalName));
            var client = new GraphServiceClient(authProvider);
            #endregion

            Console.WriteLine($"All set up!");

            #region Sample graph request
            Console.WriteLine($"Fetching last 10 mails from Graph API ...");
            //var mails = await client.Users["graph@alexcomma.onmicrosoft.com"].Messages.Request()        // Accessing shared Mailbox
            var mails = await client.Me.Messages.Request()                                            // Accessing own Mailbox
              .Select(m => new { m.Subject, m.From })
              .Top(10)
              .GetAsync();

            foreach (var mail in mails)
                Console.WriteLine($"Found mail from {mail.From.EmailAddress.Address} with subject: {mail.Subject}");
            #endregion

            Console.WriteLine("Press any key to exit.");
            Console.Read();
        }

        private static AuthenticateRequestAsyncDelegate GetAuthDelegate(IPublicClientApplication pca, string userPrincipalName)
        {
            /* This delegate is called every time the GraphServiceClient makes a request. */
            return async (HttpRequestMessage msg) =>
            {
                Console.WriteLine($"Performing authentication ...");

                // Acquire an access token
                AuthenticationResult accessToken;
                try
                {   
                    accessToken = await pca.AcquireTokenSilent(scopes, userPrincipalName).ExecuteAsync();
                    Console.WriteLine($"Cache hit. Using a token which expires on {accessToken.ExpiresOn}.");
                }
                catch (MsalUiRequiredException)
                {
                    Console.WriteLine("Cache miss. Performing interactive authentication.");
                    // Display authentication page in browser
                    accessToken = await pca.AcquireTokenInteractive(scopes).WithLoginHint(userPrincipalName).ExecuteAsync();
                }

                // Add the access token to the header of the HTTP request.
                msg.Headers.Authorization = new AuthenticationHeaderValue(AUTH_HEADER_PREFIX, accessToken.AccessToken);
            };
        }

        private static StorageCreationProperties GetStorageCreationProperties(string tokenCacheFileName, string tokenCacheDirectory, string clientID)
        {
            StorageCreationProperties storageCreationProperties = null;
            if (RuntimeInformation.IsOSPlatform(OSPlatform.Linux))
            {
                storageCreationProperties = new StorageCreationPropertiesBuilder(tokenCacheFileName, tokenCacheDirectory, clientID)
                    .WithLinuxKeyring(
                        schemaName: "com.microsoft.quantum.iqsharp",
                        collection: "default",
                        secretLabel: "Credentials used by Microsoft IQ# kernel",
                        attribute1: new KeyValuePair<string, string>("Version", "0.1"),
                        attribute2: new KeyValuePair<string, string>("ProductGroup", "QDK"))
                    .Build();
            }
            else if (RuntimeInformation.IsOSPlatform(OSPlatform.OSX))
            {
                storageCreationProperties = new StorageCreationPropertiesBuilder(tokenCacheFileName, tokenCacheDirectory, clientID)
                  .WithMacKeyChain(
                     serviceName: "Microsoft.Quantum.IQSharp",
                     accountName: "MSALCache")
                  .Build();
            }
            else //assume Windows
            {
                storageCreationProperties = new StorageCreationPropertiesBuilder(tokenCacheFileName, tokenCacheDirectory, clientID).Build();
            }
            return storageCreationProperties;
        }


    }
}
