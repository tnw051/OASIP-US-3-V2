import { decodeJwt } from 'jose';
import { computed, ref } from "vue";
import { accessTokenKey, login, logout } from "../service/api";

const user = ref(null);
const isAuthenticated = computed(() => user.value !== null);

function _login(user) {
  try {
    login(user, {
      onSuccess: (response) => {
        console.log(response);
        alert("Login successful");
        const token = response.accessToken;
        localStorage.setItem(accessTokenKey, token);
        setUserFromToken(token);
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
  setUserFromToken(token);
})()

function setUserFromToken(token) {
  if (!token) {
    console.log("No access token found");
    return;
  }
  const claims = decodeJwt(token);
  user.value = claims;
  console.log("Loaded user", user.value);
  return
}

export function useAuth() {
  return {
    user,
    isAuthenticated,
    login: _login,
    logout: _logout,
  };
}