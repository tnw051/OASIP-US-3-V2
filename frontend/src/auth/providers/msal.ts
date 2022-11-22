import { AuthenticationResult } from "@azure/msal-browser";
import { ref, watch } from "vue";
import { msalInstance, tokenRequest } from "../../configs/msalAuthConfig";
import { authorizeAad } from "../../service/api";
import { AuthState, AuthStore, getDefaultAuthState, setStore } from "../useAuthStore";

const state = ref<AuthState>(getDefaultAuthState());
const isLoadedByRedirectPromise = ref(false);
state.value.status = "loading";

const resultRef = ref<AuthenticationResult | null>(null);
watch(resultRef, (newValue) => {
  if (newValue) {
    setMsalAuthStateFromAuthenticationResult(newValue);
  } else {
    state.value.status = "unauthenticated";
  }
});

export const MsalAuthStore: AuthStore = {
  id: "msal",
  name: "MSAL",
  state,
  async logout() {
    try {
      await msalInstance.logoutRedirect({
        // logoutHint: result.value?.account?.idTokenClaims?.login_hint,
        onRedirectNavigate: (_url) => {
          // Skipping the server sign-out for the sake of ease of development
          return false;
        },
      });

      state.value = getDefaultAuthState();

      return true;
    } catch (error) {
      console.error(error);
      return false;
    }
  },
  async preload() {
    if (isLoadedByRedirectPromise.value) {
      return;
    }
    try {
      const isRedirect = await _handleRedirectPromise();
      if (isRedirect) {
        // every time the login is successful, create the AAD user in the backend if it doesn't exist
        await authorizeAad();
      }
      // silent login to get a token
      const silentResult = await msalInstance.acquireTokenSilent(tokenRequest);
      console.log("silent login result", silentResult);

      if (silentResult) {
        resultRef.value = silentResult;
        return;
      }
    } catch (error) {
      console.error(error);
    }
  },
  async getAccessToken() {
    return resultRef.value?.accessToken ?? null;
  },
};

// handle rediret from after previous login
export async function _handleRedirectPromise() {
  if (isLoadedByRedirectPromise.value) {
    return true;
  }
  isLoadedByRedirectPromise.value = true;

  const redirectResult = await msalInstance.handleRedirectPromise();
  if (redirectResult) {
    resultRef.value = redirectResult;
    return true;
  } else {
    return false;
  }
}

function setMsalAuthStateFromAuthenticationResult(result: AuthenticationResult | null) {
  const roles = result.account?.idTokenClaims?.roles;
  let roleCount = 0;

  if (roles) {
    if (roles.includes("Admin")) {
      roleCount++;
      state.value.isAdmin = true;
    }
    if (roles.includes("Lecturer")) {
      roleCount++;
      state.value.isLecturer = true;
    }
    if (roles.includes("Student")) {
      roleCount++;
      state.value.isStudent = true;
    }

    if (state.value.user) {
      state.value.user.roles = roles;
    }
  }

  if (roleCount === 0) {
    state.value.isGuest = true;
  }

  if (result.account?.name) {
    if (!state.value.user) {
      state.value.user = {
        name: result.account.name,
        email: result.account.username,
        role: roles?.[0],
        roles,
        id: result.account.homeAccountId,
      };
    } else {
      state.value.user.name = result.account.name;
    }
  }

  state.value.status = "authenticated";
}

async function login() {
  try {
    await _handleRedirectPromise();
    setStore(MsalAuthStore);
    await msalInstance.loginRedirect(tokenRequest);
  } catch (error) {
    console.error(error);
  }
}

export function useMsalAuth() {
  return {
    login,
  };
}