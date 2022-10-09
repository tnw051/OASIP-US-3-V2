import { decodeJwt } from "jose";
import { computed, ref } from "vue";
import { accessTokenKey, login, logout } from "../service/api";

const user = ref(null);
const isAuthenticated = computed(() => user.value !== null);
const isAuthLoading = ref(true);
const isAdmin = computed(() => {
  return user.value?.role === roles.ADMIN;
});
const isLecturer = computed(() => {
  return user.value?.role === roles.LECTURER;
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
    alert(errorResponse.message);
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
  const token = localStorage.getItem(accessTokenKey);
  const loadingDelay = import.meta.env.VITE_AUTH_LOADING_DELAY;
  if (loadingDelay) {
    setTimeout(work, import.meta.env.VITE_AUTH_LOADING_DELAY);
  } else {
    work();
  }

  function work() {
    setUserFromToken(token);
    isAuthLoading.value = false;
  }
})();

function setUserFromToken(token: string) {
  if (!token) {
    console.log("No access token found");
    return;
  }
  const claims = decodeJwt(token);
  user.value = claims;
  console.log("Loaded user", user.value);
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
  };
}

export type Role = "ADMIN" | "LECTURER" | "STUDENT";

export const roles = {
  ADMIN: "ADMIN" as Role,
  LECTURER: "LECTURER" as Role,
  STUDENT: "STUDENT" as Role,
};
