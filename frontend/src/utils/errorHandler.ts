import axios from 'axios';
import { ErrorResponse } from '../types';

export class ApiError extends Error {
    status: number;
    details?: string[];

    constructor(message: string, status: number, details?: string[]) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.details = details;
    }
}

const isAxiosError = (error: unknown): boolean => {
    return (
        typeof error === 'object' &&
        error !== null &&
        'isAxiosError' in error &&
        (error as { isAxiosError: boolean }).isAxiosError === true
    );
};

export const handleApiError = (error: unknown): ApiError => {
    if (error instanceof ApiError) {
        return error;
    }

    if (isAxiosError(error)) {
        const axiosError = error as any;
        const response = axiosError.response?.data as ErrorResponse;
        return new ApiError(
            response?.message || axiosError.message || 'An unexpected error occurred',
            axiosError.response?.status || 500,
            response?.details
        );
    }

    if (error instanceof Error) {
        return new ApiError(error.message, 500);
    }

    return new ApiError('An unexpected error occurred', 500);
};

export const isAuthenticationError = (error: unknown): boolean => {
    if (error instanceof ApiError) {
        return error.status === 401 || error.status === 403;
    }
    if (isAxiosError(error)) {
        const axiosError = error as any;
        return axiosError.response?.status === 401 || axiosError.response?.status === 403;
    }
    return false;
};

export const isApiError = (error: unknown): error is ApiError => {
    return (
        typeof error === 'object' &&
        error !== null &&
        'message' in error &&
        typeof (error as ApiError).message === 'string'
    );
};

export const handleApiErrorOld = (error: unknown, setError: (message: string) => void) => {
    if (error && typeof error === 'object' && 'response' in error) {
        const response = (error as any).response?.data;

        if (response?.validationErrors) {
            // Handle validation errors
            const errorMessages = response.validationErrors.join(', ');
            setError(errorMessages);
        } else if (response?.message) {
            // Handle specific error messages from the server
            setError(response.message);
        } else {
            // Handle generic error messages
            switch ((error as any).response?.status) {
                case 400:
                    setError('Invalid request. Please check your input.');
                    break;
                case 401:
                    setError('Unauthorized. Please log in again.');
                    break;
                case 403:
                    setError('Access denied. You do not have permission to perform this action.');
                    break;
                case 404:
                    setError('Resource not found.');
                    break;
                case 500:
                    setError('Server error. Please try again later.');
                    break;
                default:
                    setError('An unexpected error occurred.');
            }
        }
    } else {
        setError('An unexpected error occurred.');
    }
};

export const isValidationError = (error: unknown): boolean => {
    if (error && typeof error === 'object' && 'response' in error) {
        const response = (error as any).response?.data;
        return response?.validationErrors !== undefined;
    }
    return false;
};

export const getValidationErrors = (error: unknown): string[] => {
    if (error && typeof error === 'object' && 'response' in error) {
        const response = (error as any).response?.data;
        return response?.validationErrors || [];
    }
    return [];
}; 