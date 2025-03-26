import axios from 'axios';

export interface ApiError {
    message: string;
    status?: number;
    code?: string;
    details?: any;
}

export class ApiException extends Error implements ApiError {
    constructor(
        public message: string,
        public status?: number,
        public code?: string,
        public details?: any
    ) {
        super(message);
        this.name = 'ApiException';
    }
}

interface AxiosErrorResponse {
    message: string;
    code?: string;
    details?: any;
}

export const handleApiError = (error: unknown): ApiError => {
    if (error instanceof ApiException) {
        return error;
    }

    // Type guard for Axios error
    const isAxiosError = (error: unknown): error is any => {
        return typeof error === 'object' && error !== null && 'isAxiosError' in error;
    };

    if (isAxiosError(error)) {
        const { response, message } = error;
        
        if (response?.data) {
            const { status, data } = response;
            return {
                message: data.message || message,
                status,
                code: data.code,
                details: data.details
            };
        }
        
        return {
            message: message || 'Network error occurred',
            status: 500
        };
    }

    // Type guard for Error object
    const isError = (error: unknown): error is Error => {
        return error instanceof Error;
    };

    if (isError(error)) {
        const err = error as Error;
        return {
            message: err.message,
            status: 500
        };
    }

    return {
        message: 'An unexpected error occurred',
        status: 500
    };
};

export const isApiError = (error: unknown): error is ApiError => {
    return error instanceof ApiException || 
           (typeof error === 'object' && error !== null && 'message' in error);
};

export const getErrorMessage = (error: unknown): string => {
    if (error instanceof Error) {
        return error.message;
    }
    if (typeof error === 'string') {
        return error;
    }
    return 'An unexpected error occurred';
};

export const isAuthenticationError = (error: unknown): boolean => {
    if (error instanceof ApiException) {
        return error.status === 401 || error.status === 403;
    }

    // Type guard for Axios error
    const isAxiosError = (error: unknown): error is any => {
        return typeof error === 'object' && error !== null && 'isAxiosError' in error;
    };

    if (isAxiosError(error)) {
        return error.response?.status === 401 || error.response?.status === 403;
    }
    return false;
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

export const handleValidationErrors = (error: any, setFormErrors: (errors: any) => void) => {
  if (error.response?.status === 400 && error.response?.data?.message) {
    const errorMessage = error.response.data.message;
    const errors: Record<string, string> = {};
    
    // Parse the error message to identify error fields
    if (errorMessage.includes('name')) {
      errors.name = 'Name error: ' + errorMessage;
    }
    if (errorMessage.includes('code')) {
      errors.code = 'Code error: ' + errorMessage;
    }
    if (errorMessage.includes('status')) {
      errors.status = 'Status error: ' + errorMessage;
    }
    
    setFormErrors(errors);
    return true;
  }
  return false;
}; 