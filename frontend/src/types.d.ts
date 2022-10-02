type Id = number;
type Role = string;

interface ErrorResponse {
    timestamp: string;
    status: number;
    error: string;
    message: string;
}