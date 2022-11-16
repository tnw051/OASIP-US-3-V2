import { CreateEventRequest, EditEventRequest, EventResponse } from "../../gen-types";
import { Id } from "../../types";
import { dank } from "./client";

export async function getEvents(): Promise<EventResponse[]> {
  const response = await dank.get<EventResponse[]>("/events");
  return response.data;
}


export async function createEvent(newEvent: CreateEventRequest, file: File): Promise<EventResponse> {
  const formData = new FormData();
  for (const [key, value] of Object.entries(newEvent)) {
    formData.append(key, value);
  }
  if (file) {
    formData.append("file", file);
  }

  const response = await dank.post<EventResponse>("/events", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function deleteEvent(id: Id): Promise<boolean> {
  const response = await dank.delete(`/events/${id}`);
  return response.status === 200;
}

/**
 * @param file null means delete file, undefined means no change
 */
export async function updateEvent(id: Id, editEvent: EditEventRequest, file?: File | null): Promise<EventResponse> {
  const formData = new FormData();
  for (const [key, value] of Object.entries(editEvent)) {
    formData.append(key, value);
  }
  if (file) {
    formData.append("file", file);
  } else if (file === null) {
    formData.append("file", new Blob());
  }

  const response = await dank.patch<EventResponse>(`/events/${id}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function getEventsByCategoryIdOnDate(categoryId: Id, startAt: string): Promise<EventResponse[]> {
  const response = await dank.get<EventResponse[]>(`/events?categoryId=${categoryId}&startAt=${startAt}`);
  return response.data;
}

export async function getEventsByCategoryId(categoryId: Id): Promise<EventResponse[]> {
  const response = await dank.get<EventResponse[]>(`/events?categoryId=${categoryId}`);
  return response.data;
}


interface GetEventsFilter {
  categoryId?: Id;
  type?: string;
  startAt?: string;
}

export async function getEventsByFilter(filter: GetEventsFilter): Promise<EventResponse[]> {
  const urlSearchParams = new URLSearchParams();
  Object.entries(filter).forEach(([key, value]) => {
    if (value) {
      urlSearchParams.append(key, value);
    }
  });

  const response = await dank.get<EventResponse[]>("/events", {
    params: urlSearchParams,
  });
  return response.data;
}
