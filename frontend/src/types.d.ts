import { Role } from "./gen-types";

type Id = number;

export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    errors?: Record<string, string[]>;
}

// require manually sync to the payload from the backend for now
export interface OasipJwtPayload {
    iss: string;
    sub: string;
    role: Role;
    exp: number;
    iat: number;
    name: string;
    id: Id;
    email: string;
}

export interface BaseSlotProps<T> {
    item: T;
    dClass?: string;
}

export interface EventTimeSlot {
    eventStartTime: Date;
    eventEndTime: Date;
    eventDuration: number;
    eventCategoryId: number;
}
