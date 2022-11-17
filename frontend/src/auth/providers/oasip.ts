import { decodeJwt, JWTPayload } from "jose";
import { ref, watch } from "vue";
import { LoginRequest } from "../../gen-types";
import { accessTokenKey, login, logout } from "../../service/api";
import { OasipJwtPayload } from "../../types";
import { AuthState, AuthStore, getDefaultAuthState, setStore } from "../useAuthStore";

const state = ref<AuthState>(getDefaultAuthState());
state.value.status = "loading";

const user = ref<OasipJwtPayload>(null);


watch(user, (u) => {
  if (u === null) {
    state.value = getDefaultAuthState();
    return;
  }

  state.value = {
    user: {
      // TODO: update oasip jwt payload
      id: u?.sub,
      name: u?.sub,
      email: u?.sub,
      role: u?.role,
      roles: [u?.role],
    },
    isAdmin: u?.role === "ADMIN",
    isLecturer: u?.role === "LECTURER",
    isStudent: u?.role === "STUDENT",
    isGuest: u === null,
    status: "authenticated",
  };
});

export const OasipAuthStore: AuthStore = {
  id: "oasip",
  name: "OASIP",
  state,
  logout: _logout,
  async preload() {
    const token = _getAccessToken();
    if (token) {
      const loadingDelay = import.meta.env.VITE_AUTH_LOADING_DELAY;
      if (loadingDelay) {
        await new Promise((resolve) => setTimeout(resolve, loadingDelay));
      } 
    }

    setUserFromToken(token);
    setStore(OasipAuthStore);
  },
  async getAccessToken() {
    return _getAccessToken();
  },
  async onRefreshTokenFailed() {
    _logout();
  },
};

function _getAccessToken() {
  return localStorage.getItem(accessTokenKey);
}

export function setAccessToken(token: string) {
  localStorage.setItem(accessTokenKey, token);
}

function removeAccessToken() {
  localStorage.removeItem(accessTokenKey);
}

async function _logout() {
  user.value = null;
  removeAccessToken();
  const success = await logout();
  if (success) {
    return true;
  }
  return false;
}


// eslint-disable-next-line @typescript-eslint/no-empty-function
function _login(user: LoginRequest, onSuccess?: () => void) {
  try {
    login(user, {
      onSuccess: (response) => {
        console.log(response);
        alert("Login successful");
        const token = response.accessToken;
        setAccessToken(token);
        setUserFromToken(token);
        setStore(OasipAuthStore);
        onSuccess?.();
      },
      onUnauthorized: (error) => {
        console.log(error);
        alert("Password is incorrect");
      },
      onNotFound: (error) => {
        console.log(error);
        alert("A user with the specified email DOES NOT exist");
      },
    });
  } catch (errorResponse) {
    alert(errorResponse.message);
  }
}

function setUserFromToken(token: string) {
  if (!token) {
    console.log("No access token found");
    return;
  }
  const claims = decodeJwt(token) as JWTPayload & OasipJwtPayload;
  user.value = claims;
}

export function useOasipAuth() {
  return {
    login: _login,
  };
}


