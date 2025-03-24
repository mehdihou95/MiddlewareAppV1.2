import axios from 'axios';
import { handleApiError } from '../utils/errorHandler';
import { tokenManager } from '../utils/tokenManager';
import { User } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface AuthResponse {
  token: string;
  user: User;
}

interface RefreshResponse {
  token: string;
}

interface ApiError {
  message: string;
}

interface LoginCredentials {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  refreshToken: string;
  username: string;
  roles: string[];
}

interface RefreshTokenResponse {
  token: string;
  refreshToken: string;
}

// Create axios instance with default config
const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add request interceptor to include CSRF token
api.interceptors.request.use((config) => {
    const token = document.cookie
        .split('; ')
        .find(row => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];
    
    if (token) {
        (config.headers as Record<string, string>)['X-XSRF-TOKEN'] = token;
    }
    
    return config;
});

// Add response interceptor to handle token refresh
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const response = await api.post<RefreshResponse>('/auth/refresh');
                const { token } = response.data;
                
                // Update authorization header
                api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                originalRequest.headers['Authorization'] = `Bearer ${token}`;
                
                // Retry the original request
                return api(originalRequest);
            } catch (refreshError) {
                // If refresh fails, logout user
                logout();
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export const authService = {
    login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
        try {
            const response = await axios.post<LoginResponse>(`${API_URL}/auth/login`, credentials);
            const { token, refreshToken } = response.data;
            tokenManager.setToken(token, refreshToken);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    logout: async (): Promise<void> => {
        try {
            await axios.post(`${API_URL}/auth/logout`);
            tokenManager.clearToken();
        } catch (error) {
            console.error('Logout error:', error);
            // Clear tokens even if the logout request fails
            tokenManager.clearToken();
        }
    },

    isAuthenticated: (): boolean => {
        return tokenManager.isTokenValid();
    },

    refreshToken: async (): Promise<boolean> => {
        try {
            const refreshToken = tokenManager.getRefreshToken();
            if (!refreshToken) {
                return false;
            }

            const response = await axios.post<RefreshTokenResponse>(`${API_URL}/auth/refresh`, {
                refreshToken
            });

            const { token, refreshToken: newRefreshToken } = response.data;
            tokenManager.setToken(token, newRefreshToken);
            return true;
        } catch (error) {
            console.error('Token refresh failed:', error);
            return false;
        }
    },

    validateToken: async (): Promise<boolean> => {
        try {
            const token = tokenManager.getToken();
            if (!token) {
                return false;
            }

            const response = await axios.get(`${API_URL}/auth/validate`);
            return response.status === 200;
        } catch (error) {
            console.error('Token validation failed:', error);
            return false;
        }
    },

    getCurrentUser: async () => {
        try {
            const response = await axios.get(`${API_URL}/auth/me`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    }
};

// Export logout function for use in interceptors
export const logout = () => {
    delete api.defaults.headers.common['Authorization'];
    localStorage.removeItem('user');
    window.location.href = '/login';
}; 