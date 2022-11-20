import { Role } from "./gen-types";

type Id = number;

interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
}

// require manually sync to the payload from the backend for now
interface OasipJwtPayload {
    iss: string;
    sub: string;
    role: Role;
    exp: number;
    iat: number;
    name: string;
    id: Id;
    email: string;
}
