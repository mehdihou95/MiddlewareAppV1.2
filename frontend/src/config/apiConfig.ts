import axios from 'axios';
import { tokenManager, TokenResponse } from '../services/tokenManager';

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_URL = `${API_BASE_URL}/api`;

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

// Request interceptor for API calls
api.interceptors.request.use(
    (config: any) => {
        const token = tokenManager.getAccessToken();
        if (token) {
            config.headers = config.headers || {};
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        // Add CSRF token if present
        const csrfToken = document.cookie
            .split('; ')
            .find(row => row.startsWith('XSRF-TOKEN='))
            ?.split('=')[1];
        
        if (csrfToken) {
            config.headers = config.headers || {};
            config.headers['X-XSRF-TOKEN'] = csrfToken;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for API calls
api.interceptors.response.use(
    (response) => response,
    async (error: any) => {
        const originalRequest = error.config as any;

        // If the error is not 401 or it's already a retry, reject the promise
        if (error.response?.status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            const refreshToken = tokenManager.getRefreshToken();
            if (!refreshToken) {
                throw new Error('No refresh token available');
            }

            const response = await axios.post<TokenResponse>(`${API_URL}/auth/refresh`, {
                refreshToken,
            });

            const { token, refreshToken: newRefreshToken } = response.data;

            // Update tokens in storage
            tokenManager.setAccessToken(token);
            tokenManager.setRefreshToken(newRefreshToken);

            // Update the Authorization header
            if (originalRequest.headers) {
                originalRequest.headers['Authorization'] = `Bearer ${token}`;
            }

            // Retry the original request
            return api(originalRequest);
        } catch (refreshError) {
            // If refresh token is invalid, clear tokens and redirect to login
            tokenManager.clearTokens();
            window.location.href = '/login';
            return Promise.reject(refreshError);
        }
    }
);

export default api; 