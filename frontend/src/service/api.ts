import {
  ApiError,
  CategoryResponse,
  CreateEventRequest,
  CreateUserRequest,
  EditCategoryRequest,
  EditEventRequest,
  EditUserRequest,
  EventResponse,
  EventTimeSlotResponse,
  LoginResponse,
  MatchRequest,
  Role,
  UserResponse,
} from "../gen-types";
import router from "../router";
import { Id } from "../types";
import { accessTokenKey, makeUrl } from "./common";

type NullablePromise<T> = Promise<T | null>

export async function getEvents(): NullablePromise<EventResponse[]> {
  return dankFetcher(makeUrl("/events"));
}

export async function getCategories(): NullablePromise<CategoryResponse[]> {
  return dankFetcher(makeUrl("/categories"));
}

export async function getLecturerCategories(): NullablePromise<CategoryResponse[]> {
  return dankFetcher(makeUrl("/categories/lecturer"));
}

export async function createEvent(newEvent: CreateEventRequest, file: File): NullablePromise<EventResponse> {
  const formData = new FormData();
  for (const [key, value] of Object.entries(newEvent)) {
    formData.append(key, value);
  }
  if (file) {
    formData.append("file", file);
  }

  return dankFetcher(makeUrl("/events"), {
    method: "POST",
    body: formData,
  });
}

export async function deleteEvent(id: Id): Promise<boolean> {
  try {
    await dankFetcher(makeUrl(`/events/${id}`), {
      method: "DELETE",
    });

    return true;
  } catch (error) {
    return false;
  }
}

export async function updateEvent(id: Id, editEvent: EditEventRequest): NullablePromise<EventResponse> {
  return dankFetcher(makeUrl(`/events/${id}`), {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(editEvent),
  });
}

export async function getEventsByCategoryIdOnDate(categoryId: Id, startAt: string): NullablePromise<EventResponse[]> {
  return dankFetcher(makeUrl(`/events?categoryId=${categoryId}&startAt=${startAt}`));
}

export async function getEventsByCategoryId(categoryId: Id): NullablePromise<EventResponse[]> {
  return dankFetcher(makeUrl(`/events?categoryId=${categoryId}`));
}

interface GetEventsFilter {
  categoryId?: Id;
  type?: string;
  startAt?: string;
}

export async function getEventsByFilter(filter: GetEventsFilter): NullablePromise<EventResponse[]> {
  const { categoryId, type, startAt } = filter;

  let uri = "/events?";
  const filters: string[] = [];

  if (categoryId) {
    filters.push(`categoryId=${categoryId}`);
  }

  if (type) {
    filters.push(`type=${type}`);
  }

  if (startAt) {
    filters.push(`startAt=${startAt}`);
  }

  if (filters.length > 0) {
    uri += filters.join("&");
  }

  return dankFetcher(makeUrl(uri));
}

export async function updateCategory(id: Id, editCategory: EditCategoryRequest): NullablePromise<CategoryResponse> {
  return dankFetcher(makeUrl(`/categories/${id}`), {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(editCategory),
  });
}

interface GetUsersOptions {
  onUnauthorized?: () => void;
}
export async function getUsers(options: GetUsersOptions = {
}): NullablePromise<UserResponse[]> {
  const { onUnauthorized } = options;

  try {
    return await dankFetcher(makeUrl("/users"));
  } catch (error) {
    if (error instanceof ApiErrorError && error.content.status === 401) {
      onUnauthorized?.();
    }
  }

  return null;
}


export async function getRoles(): NullablePromise<Role[]> {
  return dankFetcher(makeUrl("/users/roles"));
}

export async function createUser(newUser: CreateUserRequest): NullablePromise<UserResponse> {
  const trimmedUser = {
    ...newUser,
    name: newUser.name.trim(),
    email: newUser.email.trim(),
  };

  return dankFetcher(makeUrl("/users"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(trimmedUser),
  });
}

export async function deleteUser(id: Id) {
  try {
    await dankFetcher(makeUrl(`/users/${id}`), {
      method: "DELETE",
    });
    return true;
  } catch (error) {
    return false;
  }
}

export async function updateUser(id: Id, changes: EditUserRequest): NullablePromise<UserResponse> {
  return dankFetcher(makeUrl(`/users/${id}`), {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(changes),
  });
}

export async function match(matchRequest: MatchRequest) {
  return dankFetcher(makeUrl("/auth/match"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(matchRequest),
  });
}

export async function getFilenameByBucketUuid(uuid: string): NullablePromise<string> {
  const response = await fetch(makeUrl(`/events/files/${uuid}?noContent=true`), {
    headers: makeAuthHeaders(),
  });

  if (response.status === 200) {
    // get filename from content-disposition header
    const filename = response.headers.get("content-disposition")?.split("filename=")[1];
    if (filename) {
      return filename;
    }
  }

  return null;
}

export function getBucketURL(uuid: string) {
  return makeUrl(`/events/files/${uuid}`);
}

export async function getAllocatedTimeSlotsInCategoryOnDate(
  categoryId: Id,
  startAt: Date,
  excludeId?: Id,
): NullablePromise<EventTimeSlotResponse[]> {
  let url = makeUrl(`/events/allocatedTimeSlots?categoryId=${categoryId}&startAt=${startAt.toISOString()}`);
  if (excludeId) {
    url += `&excludeId=${excludeId}`;
  }

  return dankFetcher(url);
}

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

async function dankFetcher<T = unknown>(url: string, options: RequestInit = {}): NullablePromise<T> {
  const finalOptions = {
    ...options,
  };

  finalOptions.headers = {
    ...finalOptions.headers,
    ...makeAuthHeaders(),
  };

  const response = await fetch(url, finalOptions);
  if (response.status === 401) {
    await router.isReady();
    router.push("/login");

    const { error } = await refreshAccessToken();
    if (error) {
      throw new ApiErrorError(error);
    }

    return dankFetcher(url, options);
  }

  if (!response.ok) {
    try {
      const error = await response.json();
      if (error.message) {
        console.log(error.message);

        throw new ApiErrorError(error);
      }
    } catch (error) {
      throw new ApiUnexpectedError(url, response.status);
    }
  }

  try {
    return await response.json() as Promise<T>;
  } catch (error) {
    console.log("Could not parse response as JSON");
    return null;
  }
}

function makeAuthHeaders() {
  const accessToken = localStorage.getItem(accessTokenKey);
  return {
    ...(accessToken && {
      Authorization: `Bearer ${accessToken}`, // add token to headers if it exists
    }),
  };
}

type RefreshTokenResult = {
  accessToken: string;
  error: null;
} | {
  accessToken: null;
  error: ApiError;
};

async function refreshAccessToken(): Promise<RefreshTokenResult> {
  const response = await fetch(makeUrl("/auth/refresh"), {
    method: "POST",
  });

  if (!response.ok) {
    return { accessToken: null, error: await response.json() };
  }

  const data = await response.json() as LoginResponse;
  localStorage.setItem(accessTokenKey, data.accessToken);

  return { accessToken: data.accessToken, error: null };
}