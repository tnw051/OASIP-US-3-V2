import { setAccessToken } from "../../auth/providers/oasip";
import { MatchRequest, LoginResponse, LoginRequest } from "../../gen-types";
import { ErrorResponse } from "../../types";
import { dank } from "./client";

export const accessTokenKey = "accessToken";

interface RefreshTokenResponse {
  accessToken: string;
  error: Error | null;
}

export const refreshTokenURL = "/auth/refresh";
export async function refreshAccessToken(): Promise<RefreshTokenResponse> {
  const response = await dank.post<RefreshTokenResponse>(refreshTokenURL);
  const data = response?.data;
  if (data) {
    setAccessToken(data.accessToken);
  }
  return data;
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
  return (response as ErrorResponse)?.error !== undefined;
}

export const loginURL = "/auth/login";
export async function login(loginRequest: LoginRequest, options: LoginOptions = {
}): Promise<void> {
  try {
    const response = await dank.post<LoginResponse>(`${loginURL}`, loginRequest);
    options.onSuccess?.(response.data);
  } catch (error) {
    const data = error?.response?.data;
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

export async function authorizeAad(): Promise<boolean> {
  const response = await dank.post("/auth/aad");
  return response.status === 200;
}