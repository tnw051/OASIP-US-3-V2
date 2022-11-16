import { AuthenticationResult, EventType } from "@azure/msal-browser";
import { createApp } from "vue";
import App from "./App.vue";
import { MsalAuthStore } from "./auth/providers/msal";
import { OasipAuthStore } from "./auth/providers/oasip";
import { registerAuthStore, initAuthStore } from "./auth/useAuthStore";
import { msalInstance } from "./configs/msalAuthConfig";
import "./index.css";
import { msalPlugin } from "./plugins/msalPlugin";
import router from "./router/index";
import { CustomNavigationClient } from "./router/NavigationClient";

registerAuthStore(OasipAuthStore);
registerAuthStore(MsalAuthStore);
initAuthStore();

const navigationClient = new CustomNavigationClient(router);
msalInstance.setNavigationClient(navigationClient);

const accounts = msalInstance.getAllAccounts();
if (accounts.length > 0) {
  msalInstance.setActiveAccount(accounts[0]);
}
msalInstance.addEventCallback((event) => {
  if (event.eventType === EventType.LOGIN_SUCCESS && event.payload) {
    const payload = event.payload as AuthenticationResult;
    const account = payload.account;
    msalInstance.setActiveAccount(account);
  }
});

const app = createApp(App);

app.use(router);
app.use(msalPlugin, msalInstance);

router.isReady().then(() => {
  // Waiting for the router to be ready prevents race conditions when returning from a loginRedirect or acquireTokenRedirect
  app.mount("#app");
});
