import { Role } from "./gen-types";

type Id = number;

export interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
}

// require manually sync to the payload from the backend for now
export interface OasipJwtPayload {
    iss: string;
    sub: string;
    role: Role;
    exp: number;
    iat: number;
}

export interface BaseSlotProps<T> {
    item: T;
}