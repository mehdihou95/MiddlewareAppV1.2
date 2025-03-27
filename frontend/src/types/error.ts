export interface ErrorResponse {
    code: string;
    message: string;
    details?: string | string[];
    timestamp?: string;
    path?: string;
    status?: number;
}

export interface ValidationError {
    field: string;
    message: string;
}

export interface ApiError extends ErrorResponse {
    validationErrors?: ValidationError[];
} 