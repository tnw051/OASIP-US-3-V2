/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.37.1128 on 2022-12-19 17:22:05.

export interface ApiError {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    errors: { [index: string]: string[] };
}

export interface CategoryResponse {
    id: number;
    eventCategoryName: string;
    eventCategoryDescription: string;
    eventDuration: number;
}

export interface CreateEventRequest {
    eventCategoryId: number;
    bookingName: string;
    bookingEmail: string;
    eventStartTime: DateAsString;
    eventNotes?: string;
}

export interface CreateUserRequest {
    name: string;
    email: string;
    password: string;
    role: string;
}

export interface EditCategoryRequest {
    eventCategoryName?: string;
    eventCategoryDescription?: string;
    eventDuration?: number;
}

export interface EditEventRequest {
    eventStartTime?: DateAsString;
    eventNotes?: string;
}

export interface EditUserRequest {
    name?: string;
    email?: string;
    role?: string;
}

export interface EventCategoryIdAndNameResponse {
    id: number;
    eventCategoryName: string;
}

export interface EventResponse {
    id: number;
    eventCategory: EventCategoryIdAndNameResponse;
    bookingName: string;
    bookingEmail: string;
    eventStartTime: DateAsString;
    eventDuration: number;
    eventNotes: string;
    files?: FileInfoResponse[];
}

export interface EventTimeSlotResponse {
    eventStartTime: DateAsString;
    eventEndTime: DateAsString;
    eventDuration: number;
    eventCategoryId: number;
}

export interface FileInfoResponse {
    name: string;
    type: string;
    bucketId: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    type: string;
    accessToken: string;
}

export interface MatchRequest {
    email: string;
    password: string;
}

export interface UserResponse {
    id: number;
    name: string;
    email: string;
    role: Role;
    createdOn: DateAsString;
    updatedOn: DateAsString;
}

export type DateAsString = string;

export type Role = "ADMIN" | "STUDENT" | "LECTURER";
