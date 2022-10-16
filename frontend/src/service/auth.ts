import { ApiErrorError, ApiUnexpectedError, dankFetcher, makeUrl } from "./common";
import { LoginRequest, LoginResponse } from "../gen-types";
import { reactiveToken } from "../utils/useAuth";

type LoginResult = {
  accessToken: null;
  error: ApiErrorError | ApiUnexpectedError;
} | {
  accessToken: string;
  error: null;
};

export async function login(loginRequest: LoginRequest): Promise<LoginResult> {
  const url = makeUrl("/auth/login");
  try {
    const response = await dankFetcher<LoginResponse>(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(loginRequest),
    }, {
      noAuth: true,
    });

    if (response) {
      return { accessToken: response.accessToken, error: null };
    }
  } catch (error) {
    if (error instanceof ApiErrorError) {
      return { accessToken: null, error };
    }
  }

  return { accessToken: null, error: new ApiUnexpectedError(url, 500) };
}

export async function logout() {
  try {
    await dankFetcher(makeUrl("/auth/logout"), {
      method: "POST",
    }, {
      noAuth: true,
    });

    return true;
  } catch (error) {
    return false;
  }
}

