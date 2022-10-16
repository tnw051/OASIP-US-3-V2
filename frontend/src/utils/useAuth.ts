import { decodeJwt, JWTPayload } from "jose";
import { computed, ref } from "vue";
import { LoginRequest, Role } from "../gen-types";
import { login, logout } from "../service/auth";
import { ApiErrorError } from "../service/common";
import { OasipJwtPayload } from "../types";

const accessTokenKey = "accessToken";
export const reactiveToken = ref<string | null>(null);

export function setReactiveToken(token: string | null) {
  reactiveToken.value = token;
  if (token) {
    localStorage.setItem(accessTokenKey, token);
  } else {
    localStorage.removeItem(accessTokenKey);
  }
}

(function initReactiveToken() {
  const loadingDelay = import.meta.env.VITE_AUTH_LOADING_DELAY;
  if (loadingDelay) {
    setTimeout(work, import.meta.env.VITE_AUTH_LOADING_DELAY);
  } else {
    work();
  }

  function work() {
    const token = localStorage.getItem(accessTokenKey);
    if (token) {
      setReactiveToken(token);
    }
    isAuthLoading.value = false;
  }
})();

const user = computed<OasipJwtPayload | null>(() => {
  const token = reactiveToken.value;
  if (!token) {
    return null;
  }
  return decodeJwt(token) as JWTPayload & OasipJwtPayload;
});

const isAuthenticated = computed(() => user.value !== null);
const isAuthLoading = ref(true);
const isAdmin = computed(() => {
  return user.value?.role === "ADMIN";
});
const isLecturer = computed(() => {
  return user.value?.role === "LECTURER";
});

type LoginResult = {
  success: true;
  error: null;
} | {
  success: false;
  error: string;
}

async function _login(user: LoginRequest): Promise<LoginResult> {
  const { accessToken, error } = await login(user);
  if (accessToken) {
    setReactiveToken(accessToken);
    return { success: true, error: null };
  }

  if (error instanceof ApiErrorError) {
    if (error.content.status === 401) {
      return { error: "Password is incorrect", success: false };
    } else if (error.content.status === 404) {
      return { error: "A user with the specified email DOES NOT exist", success: false };
    }
  }

  return { error: "An unexpected error occurred", success: false };
}

async function _logout() {
  setReactiveToken(null);
  return await logout();
}

function waitForAuth() {
  return new Promise<void>(resolve => {
    function check() {
      if (isAuthLoading.value) {
        setTimeout(check, 100);
        return;
      }
      resolve();
    }

    check();
  });
}

export function useAuth() {
  return {
    user,
    isAuthenticated,
    login: _login,
    logout: _logout,
    isAdmin,
    isLecturer,
    isAuthLoading,
    waitForAuth,
  };
}

export const roles = {
  ADMIN: "ADMIN" as Role,
  LECTURER: "LECTURER" as Role,
  STUDENT: "STUDENT" as Role,
};
