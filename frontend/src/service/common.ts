import { ApiError, LoginResponse } from "../gen-types";
import { reactiveToken, setReactiveToken } from "../utils/useAuth";

const baseUrl = import.meta.env.PROD ? import.meta.env.VITE_API_URL : "/api";

export function makeUrl(path: string) {
  return `${baseUrl}${path}`;
}

export type NullablePromise<T> = Promise<T | null>

export class ApiErrorError extends Error {
  public content: ApiError;

  constructor(error: ApiError) {
    super(error.message);
    this.name = "ApiError";
    this.content = error;
  }
}

export class ApiUnexpectedError extends Error {
  constructor(public path: string, public status: number) {
    super(`Unexpected error from '${path}': ${status}`);
    this.name = "ApiUnexpectedError";
  }
}

interface FetchOptions {
  noAuth?: boolean;
  noRefresh?: boolean;
}

type RefreshTokenResult = {
  accessToken: string;
  error: null;
} | {
  accessToken: null;
  error: ApiErrorError | ApiUnexpectedError;
};

export async function dankFetcher<T = unknown>(url: string, options: RequestInit = {}, customOptions: FetchOptions = {}): NullablePromise<T> {
  const finalOptions = {
    ...options,
  };

  finalOptions.headers = {
    ...finalOptions.headers,
    ...(customOptions.noAuth ? {} : makeAuthHeaders()),
  };

  const response = await fetch(url, finalOptions);
  if (response.status === 401 && !customOptions.noRefresh) {
    const { error } = await refreshAccessToken();
    if (error instanceof ApiErrorError) {
      throw error;
    }

    return dankFetcher(url, options);
  }

  if (!response.ok) {
    let error: ApiError | null = null;
    try {
      error = await response.json();
    } catch (_error) {
      console.error(`Failed to parse error response: ${_error}`);
    }

    if (error) {
      throw new ApiErrorError(error);
    }

    throw new ApiUnexpectedError(url, response.status);
  }

  try {
    return await response.json() as Promise<T>;
  } catch (error) {
    console.log("Could not parse response as JSON");
    return null;
  }
}

export function makeAuthHeaders() {
  const accessToken = reactiveToken.value;
  return {
    ...(accessToken && {
      Authorization: `Bearer ${accessToken}`, // add token to headers if it exists
    }),
  };
}

export async function refreshAccessToken(): Promise<RefreshTokenResult> {
  const url = makeUrl("/auth/refresh");
  try {
    const response = await dankFetcher<LoginResponse>(url, {
      method: "POST",
    }, {
      noAuth: true,
      noRefresh: true,
    });

    console.log("Refreshed access token");
    if (response) {
      setReactiveToken(response.accessToken);
      return { accessToken: response.accessToken, error: null };
    }
  } catch (error) {
    setReactiveToken(null);
    console.log("Failed to refresh access token");

    if (error instanceof ApiErrorError) {
      return { accessToken: null, error };
    }
  }

  return { accessToken: null, error: new ApiUnexpectedError(url, 500) };
}
