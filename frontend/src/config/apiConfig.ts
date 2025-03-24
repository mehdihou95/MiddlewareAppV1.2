import axios from 'axios';
import { tokenManager } from '../utils/tokenManager';

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_URL = `${API_BASE_URL}/api`;

interface AuthResponse {
    token: string;
    refreshToken: string;
    user: any;
}

// Create a single axios instance for the entire application
const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true // Enable credentials for CSRF
});

// Add request interceptor for both auth token and CSRF
api.interceptors.request.use(
    (config) => {
        // Add auth token if available
        const token = tokenManager.getToken();
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        // Add CSRF token if available
        const csrfToken = document.cookie
            .split('; ')
            .find(row => row.startsWith('XSRF-TOKEN='))
            ?.split('=')[1];
        
        if (csrfToken && config.headers) {
            config.headers['X-XSRF-TOKEN'] = csrfToken;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor with token refresh logic
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
            originalRequest._retry = true;
            const refreshToken = tokenManager.getRefreshToken();
            
            if (refreshToken) {
                try {
                    const response = await api.post<AuthResponse>('/auth/refresh', { refreshToken });
                    const { token, refreshToken: newRefreshToken } = response.data;
                    
                    tokenManager.setToken(token, newRefreshToken);
                    if (originalRequest.headers) {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    }
                    
                    return api(originalRequest);
                } catch (refreshError) {
                    tokenManager.clearToken();
                    // Only redirect to login if we're not already there
                    if (!window.location.pathname.includes('/login')) {
                        window.location.href = '/login';
                    }
                    throw refreshError;
                }
            }
        }
        
        return Promise.reject(error);
    }
);

// Export the configured axios instance
export default api;

// Export a function to reset axios defaults (useful for logout)
export const resetAxiosDefaults = () => {
    delete api.defaults.headers.common['Authorization'];
}; 