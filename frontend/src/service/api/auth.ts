import { MatchRequest, LoginResponse, LoginRequest } from "../../gen-types";
import { ErrorResponse } from "../../types";
import { dank } from "./client";

export const accessTokenKey = "accessToken";

interface RefreshTokenResponse {
  accessToken: string;
  error: Error | null;
}

export async function refreshAccessToken(): Promise<RefreshTokenResponse> {
  const response = await dank.post<RefreshTokenResponse>("/auth/refresh");
  localStorage.setItem(accessTokenKey, response.data.accessToken);
  return response.data;
}


export async function match(matchRequest: MatchRequest) {
  const response = await dank.post("/match", matchRequest);
  return response.data;
}

interface LoginOptions {
  onSuccess?: (r: LoginResponse) => void;
  onUnauthorized?: (r: ErrorResponse) => void;
  onNotFound?: (r: ErrorResponse) => void;
}

function isErrorResponse(response: LoginResponse | ErrorResponse): response is ErrorResponse {
  return (response as ErrorResponse).error !== undefined;
}

export async function login(loginRequest: LoginRequest, options: LoginOptions = {
}): Promise<void> {
  try {
    const response = await dank.post<LoginResponse>("/auth/login", loginRequest);
    localStorage.setItem(accessTokenKey, response.data.accessToken);
    options.onSuccess?.(response.data);
  } catch (error) {
    const { data } = error.response;
    if (isErrorResponse(data)) {
      if (error.response.status === 401) {
        options.onUnauthorized?.(data);
      } else if (error.response.status === 404) {
        options.onNotFound?.(data);
      } 
    } else {
      throw error;
    }
  }
}

export async function logout(): Promise<boolean> {
  const response = await dank.post("/auth/logout");
  return response.status === 200;
}