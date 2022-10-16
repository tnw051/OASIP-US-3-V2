import {
  CategoryResponse,
  CreateEventRequest,
  CreateUserRequest,
  EditCategoryRequest,
  EditEventRequest,
  EditUserRequest,
  EventResponse,
  EventTimeSlotResponse,
  MatchRequest,
  Role,
  UserResponse,
} from "../gen-types";
import { Id } from "../types";
import {
  ApiErrorError,
  dankFetcher,
  makeAuthHeaders,
  makeUrl,
  NullablePromise,
} from "./common";

export async function getEvents(): Promise<EventResponse[]> {
  return dankFetcher(makeUrl("/events"));
}

export async function getCategories(): Promise<CategoryResponse[]> {
  return dankFetcher(makeUrl("/categories"));
}

export async function getLecturerCategories(): Promise<CategoryResponse[]> {
  return dankFetcher(makeUrl("/categories/lecturer"));
}

export async function createEvent(newEvent: CreateEventRequest, file: File): Promise<EventResponse> {
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
    }, {
      noContent: true,
    });

    return true;
  } catch (error) {
    return false;
  }
}

export async function updateEvent(id: Id, editEvent: EditEventRequest): Promise<EventResponse> {
  return dankFetcher(makeUrl(`/events/${id}`), {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(editEvent),
  });
}

export async function getEventsByCategoryIdOnDate(categoryId: Id, startAt: string): Promise<EventResponse[]> {
  return dankFetcher(makeUrl(`/events?categoryId=${categoryId}&startAt=${startAt}`));
}

export async function getEventsByCategoryId(categoryId: Id): Promise<EventResponse[]> {
  return dankFetcher(makeUrl(`/events?categoryId=${categoryId}`));
}

interface GetEventsFilter {
  categoryId?: Id;
  type?: string;
  startAt?: string;
}

export async function getEventsByFilter(filter: GetEventsFilter): Promise<EventResponse[]> {
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

export async function updateCategory(id: Id, editCategory: EditCategoryRequest): Promise<CategoryResponse> {
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

export async function getUsers(options: GetUsersOptions = {}): NullablePromise<UserResponse[]> {
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


export async function getRoles(): Promise<Role[]> {
  return dankFetcher(makeUrl("/users/roles"));
}

export async function createUser(newUser: CreateUserRequest): Promise<UserResponse> {
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
    }, {
      noContent: true,
    });
    return true;
  } catch (error) {
    return false;
  }
}

export async function updateUser(id: Id, changes: EditUserRequest): Promise<UserResponse> {
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
): Promise<EventTimeSlotResponse[]> {
  let url = makeUrl(`/events/allocatedTimeSlots?categoryId=${categoryId}&startAt=${startAt.toISOString()}`);
  if (excludeId) {
    url += `&excludeId=${excludeId}`;
  }

  return dankFetcher(url);
}

