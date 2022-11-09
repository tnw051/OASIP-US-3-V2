import { LogLevel, PublicClientApplication } from "@azure/msal-browser";

const msalConfig = {
  auth: {
    clientId: "9914859c-bcd1-42f1-ab4e-b28306d466b3",
    authority: "https://login.microsoftonline.com/6f4432dc-20d2-441d-b1db-ac3380ba633d",
    redirectUri: "/",
    postLogoutRedirectUri: "/",
  },
  cache: {
    cacheLocation: "localStorage", // This configures where your cache will be stored
    storeAuthStateInCookie: false, // Set this to "true" if you are having issues on IE11 or Edge
  },
  system: {
    loggerOptions: {
      logLevel: LogLevel.Trace,
    },
  },
};

export const msalInstance = new PublicClientApplication(msalConfig);

export const apiConfig = {
  apiEndpoint: "http://localhost:3000/api",
  scopes: ["api://9914859c-bcd1-42f1-ab4e-b28306d466b3/access_as_user"],
};

export const loginRequest = {
  scopes: ["openid", "profile"],
};

export const tokenRequest = {
  scopes: [...apiConfig.scopes],
};
