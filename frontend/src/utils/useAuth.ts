import { decodeJwt, JWTPayload } from "jose";
import { computed, ref, watchEffect } from "vue";
import { LoginRequest, Role } from "../gen-types";
import { login, logout } from "../service/auth";
import { accessTokenKey } from "../service/common";
import { ErrorResponse, OasipJwtPayload } from "../types";

const user = ref<OasipJwtPayload | null>(null);
const isAuthenticated = computed(() => user.value !== null);
const isAuthLoading = ref(true);
const isAdmin = computed(() => {
  return user.value?.role === "ADMIN";
});
const isLecturer = computed(() => {
  return user.value?.role === "LECTURER";
});

// eslint-disable-next-line @typescript-eslint/no-empty-function
function _login(user: LoginRequest, onSuccess = () => { }) {
  try {
    login(user, {
      onSuccess: (response) => {
        console.log(response);
        alert("Login successful");
        const token = response.accessToken;
        setUserFromToken(token);
        onSuccess();
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
    const error = errorResponse as ErrorResponse;
    alert(error.message);
  }
}

async function _logout() {
  const success = await logout();
  if (success) {
    user.value = null;
    return true;
  }
  return false;
}

(function init() {
  const loadingDelay = import.meta.env.VITE_AUTH_LOADING_DELAY;
  if (loadingDelay) {
    setTimeout(work, import.meta.env.VITE_AUTH_LOADING_DELAY);
  } else {
    work();
  }

  function work() {
    const token = localStorage.getItem(accessTokenKey);
    if (!token) {
      console.log("No access token found");
    } else {
      console.log("Found access token");
      setUserFromToken(token);
    }

    isAuthLoading.value = false;
  }
})();

function setUserFromToken(token: string) {
  const claims = decodeJwt(token) as JWTPayload & OasipJwtPayload;
  user.value = claims;
  console.log("Loaded user", user.value);
}

function onAuthLoaded(callback: () => void) {
  watchEffect(() => {
    if (isAuthLoading.value) {
      return;
    }
    callback();
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
    onAuthLoaded,
  };
}

export const roles = {
  ADMIN: "ADMIN" as Role,
  LECTURER: "LECTURER" as Role,
  STUDENT: "STUDENT" as Role,
};
