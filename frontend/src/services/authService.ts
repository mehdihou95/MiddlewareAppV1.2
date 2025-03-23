import axios from 'axios';
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
    login: async (username: string, password: string): Promise<User> => {
        const response = await api.post<AuthResponse>('/auth/login', { username, password });
        const { token, user } = response.data;
        
        // Set authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        return user;
    },

    logout: async (): Promise<void> => {
        try {
            await api.post('/auth/logout');
        } finally {
            // Clear auth header and local storage
            delete api.defaults.headers.common['Authorization'];
            localStorage.removeItem('user');
        }
    },

    refreshToken: async (): Promise<string> => {
        const response = await api.post<RefreshResponse>('/auth/refresh');
        const { token } = response.data;
        
        // Update authorization header
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        return token;
    },

    getCurrentUser: async (): Promise<User> => {
        const response = await api.get<User>('/auth/user');
        return response.data;
    },

    isAuthenticated: (): boolean => {
        return !!api.defaults.headers.common['Authorization'];
    }
};

// Export logout function for use in interceptors
export const logout = () => {
    delete api.defaults.headers.common['Authorization'];
    localStorage.removeItem('user');
    window.location.href = '/login';
}; 