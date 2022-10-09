/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.37.1128 on 2022-10-09 11:20:13.

interface CategoryResponse {
    id: number;
    eventCategoryName: string;
    eventCategoryDescription: string;
    eventDuration: number;
}

interface CreateEventRequest {
    eventCategoryId: number;
    bookingName: string;
    bookingEmail: string;
    eventStartTime: DateAsString;
    eventNotes?: string;
}

interface CreateUserRequest {
    name: string;
    email: string;
    password: string;
    role: string;
}

interface EditCategoryRequest {
    eventCategoryName?: string;
    eventCategoryDescription?: string;
    eventDuration?: number;
}

interface EditEventRequest {
    eventStartTime?: DateAsString;
    eventNotes?: string;
}

interface EditUserRequest {
    name?: string;
    email?: string;
    role?: string;
}

interface EventCategoryIdAndNameResponse {
    id: number;
    eventCategoryName: string;
}

interface EventResponse {
    id: number;
    eventCategory: EventCategoryIdAndNameResponse;
    bookingName: string;
    bookingEmail: string;
    eventStartTime: DateAsString;
    eventDuration: number;
    eventNotes: string;
}

interface LoginRequest {
    email: string;
    password: string;
}

interface LoginResponse {
    type: string;
    accessToken: string;
}

interface MatchRequest {
    email: string;
    password: string;
}

interface UserResponse {
    id: number;
    name: string;
    email: string;
    role: Role;
    createdOn: DateAsString;
    updatedOn: DateAsString;
}

type DateAsString = string;

type Role = "ADMIN" | "STUDENT" | "LECTURER";
