- Based on https://github.com/AzureAD/microsoft-authentication-library-for-js/tree/dev/samples/msal-browser-samples/VanillaJSTestApp2.0
Does not really work though

- Tested on Chrome v85 on Win 10

- Does not work in IE11 the sample repo has some IE-specific features, but
  they do not appear to work. I guess the culprit is the insufficient JS
  support
// Browser check variables
// If you support IE, our recommendation is that you sign-in using Redirect APIs
// If you as a developer are testing using Edge InPrivate mode, please add "isEdge" to the if check
const ua = window.navigator.userAgent;
const msie = ua.indexOf("MSIE ");
const msie11 = ua.indexOf("Trident/");
const msedge = ua.indexOf("Edge/");
const isIE = msie > 0 || msie11 > 0;
const isEdge = msedge > 0;


- npm install, npm start

- node 12.18.3 used

- App reg: SPA endpoint with redirect URI http://localhost:30662

- Frontend: Uses CDN for msal.js and bootstrap
msal cdn info: https://www.npmjs.com/package/msal

- token cache in session (????)

- maybe polyfills needed for graph

- only redirect option presented, one could also use popups
