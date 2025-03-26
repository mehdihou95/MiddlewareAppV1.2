import axios, { AxiosRequestConfig } from 'axios';
import { tokenManager } from '../utils/tokenManager';

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_URL = `${API_BASE_URL}/api`;

interface AuthResponse {
    token: string;
    refreshToken: string;
    user: any;
}

interface CsrfResponse {
    csrfToken: string;
}

interface ExtendedRequestConfig extends AxiosRequestConfig {
    _csrfRetry?: boolean;
    _jwtRetry?: boolean;
}

// Create a single axios instance for the entire application
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true // Enable credentials for CSRF
});

// Function to get CSRF token from cookies
const getCsrfToken = () => {
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return value;
        }
    }
    return null;
};

// Function to handle token validation
const validateToken = async () => {
    try {
        await api.get('/api/auth/validate');
        return true;
    } catch (error) {
        console.error('Token validation failed:', error);
        return false;
    }
};

// Add request interceptor for both auth token and CSRF
api.interceptors.request.use(
    (config) => {
        if (!config.headers) {
            config.headers = {};
        }

        // Skip token check for auth endpoints
        if (config.url?.includes('/auth/login') || config.url?.includes('/auth/refresh')) {
            return config;
        }

        // Add auth token if available
        const token = tokenManager.getToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        } else if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
        }

        // Add CSRF token for non-GET requests
        if (config.method?.toUpperCase() !== 'GET') {
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                config.headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        return config;
    },
    (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
    }
);

// Function to refresh CSRF token
const refreshCsrfToken = async (): Promise<string | null> => {
    try {
        const response = await api.post<CsrfResponse>('/auth/refresh-csrf', {}, {
            headers: {
                'Authorization': `Bearer ${tokenManager.getToken()}`
            }
        });
        return response.data.csrfToken;
    } catch (error) {
        console.error('Failed to refresh CSRF token:', error);
        return null;
    }
};

// Response interceptor with token refresh logic and better error handling
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config as ExtendedRequestConfig;
        if (!originalRequest) {
            return Promise.reject(error);
        }

        // Handle CSRF token expiration (403 Forbidden)
        if (error.response?.status === 403 && !originalRequest._csrfRetry) {
            originalRequest._csrfRetry = true;
            try {
                const response = await api.post<CsrfResponse>('/api/auth/refresh-csrf');
                if (response.data.csrfToken) {
                    if (!originalRequest.headers) {
                        originalRequest.headers = {};
                    }
                    originalRequest.headers['X-XSRF-TOKEN'] = response.data.csrfToken;
                    return api(originalRequest);
                }
            } catch (csrfError) {
                console.error('CSRF refresh failed:', csrfError);
            }
        }

        // Handle JWT token expiration (401 Unauthorized)
        if (error.response?.status === 401 && !originalRequest._jwtRetry) {
            originalRequest._jwtRetry = true;
            try {
                const refreshToken = tokenManager.getRefreshToken();
                if (!refreshToken) {
                    throw new Error('No refresh token available');
                }

                const response = await api.post<AuthResponse>('/api/auth/refresh', { refreshToken });
                const { token, refreshToken: newRefreshToken } = response.data;

                tokenManager.setToken(token);
                tokenManager.setRefreshToken(newRefreshToken);
                if (!originalRequest.headers) {
                    originalRequest.headers = {};
                }
                originalRequest.headers['Authorization'] = `Bearer ${token}`;

                // Validate the new token
                const isValid = await validateToken();
                if (!isValid) {
                    tokenManager.clearTokens();
                    window.location.href = '/login';
                    return Promise.reject('Invalid token after refresh');
                }

                return api(originalRequest);
            } catch (refreshError) {
                console.error('Token refresh failed:', refreshError);
                tokenManager.clearTokens();
                if (!window.location.pathname.includes('/login')) {
                    window.location.href = '/login';
                }
            }
        }

        return Promise.reject(error);
    }
);

// Export the configured axios instance
export default api;

// Export a function to reset axios defaults
export const resetAxiosDefaults = () => {
    delete api.defaults.headers.common['Authorization'];
}; 