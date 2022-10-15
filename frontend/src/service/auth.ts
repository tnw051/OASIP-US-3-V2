import { accessTokenKey, makeUrl } from "./common";
import { ApiError, LoginRequest, LoginResponse } from "../gen-types";

interface LoginOptions {
  onSuccess?: (r: LoginResponse) => void;
  onUnauthorized?: (r: ApiError) => void;
  onNotFound?: (r: ApiError) => void;
}

export async function login(loginRequest: LoginRequest, options: LoginOptions = {
}): Promise<void> {
  const { onSuccess, onUnauthorized, onNotFound } = options;
  const response = await fetch(makeUrl("/auth/login"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(loginRequest),
  });

  if (response.status === 200) {
    const data = await response.json() as LoginResponse;
    localStorage.setItem(accessTokenKey, data.accessToken);
    onSuccess?.(data);
  } else if (response.status === 401) {
    const data = await response.json() as ApiError;
    onUnauthorized?.(data);
  } else if (response.status === 404) {
    const data = await response.json() as ApiError;
    onNotFound?.(data);
  } else {
    console.log("Cannot login");
  }
}

export async function logout() {
  const response = await fetch(makeUrl("/auth/logout"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
  });

  if (response.status === 200) {
    localStorage.removeItem(accessTokenKey);
    return true;
  } else {
    console.log("Cannot logout");
    return false;
  }
}
