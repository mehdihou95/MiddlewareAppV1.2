import axios from 'axios';

export interface ErrorResponse {
    status: number;
    message: string;
    validationErrors?: string[];
}

export const handleApiError = (error: unknown, setError: (message: string) => void) => {
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