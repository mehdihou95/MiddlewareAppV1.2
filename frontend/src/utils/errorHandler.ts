import axios from 'axios';

export interface ApiError {
    message: string;
    code?: string;
    details?: any;
}

interface AxiosErrorResponse {
    response?: {
        data?: {
            message?: string;
            [key: string]: any;
        };
        status?: number;
    };
    message: string;
}

export const handleApiError = (error: unknown): ApiError => {
    if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as AxiosErrorResponse;
        return {
            message: axiosError.response?.data?.message || axiosError.message,
            code: axiosError.response?.status?.toString(),
            details: axiosError.response?.data
        };
    }

    if (error instanceof Error) {
        return {
            message: error.message,
            code: 'UNKNOWN_ERROR'
        };
    }

    return {
        message: 'An unexpected error occurred',
        code: 'UNKNOWN_ERROR'
    };
};

export const isApiError = (error: unknown): error is ApiError => {
    return (
        typeof error === 'object' &&
        error !== null &&
        'message' in error &&
        typeof (error as ApiError).message === 'string'
    );
};

export interface ErrorResponse {
    status: number;
    message: string;
    validationErrors?: string[];
}

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