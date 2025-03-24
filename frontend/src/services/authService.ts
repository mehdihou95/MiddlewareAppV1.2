import api, { resetAxiosDefaults } from '../config/apiConfig';
import { handleApiError } from '../utils/errorHandler';
import { tokenManager } from '../utils/tokenManager';
import { User } from '../types';

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

export const authService = {
    login: async (credentials: LoginCredentials): Promise<LoginResponse> => {
        try {
            const response = await api.post<LoginResponse>('/auth/login', credentials);
            const { token, refreshToken } = response.data;
            tokenManager.setToken(token, refreshToken);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    logout: async (): Promise<void> => {
        try {
            // Since there's no logout endpoint, we'll just clear tokens locally
            tokenManager.clearToken();
            resetAxiosDefaults();
            // Only redirect if not already on login page
            if (!window.location.pathname.includes('/login')) {
                window.location.href = '/login';
            }
        } catch (error) {
            console.error('Logout error:', error);
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

            const response = await api.post<RefreshTokenResponse>('/auth/refresh', {
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

            const response = await api.get('/auth/validate');
            return response.status === 200;
        } catch (error) {
            console.error('Token validation failed:', error);
            return false;
        }
    },

    getCurrentUser: async (): Promise<User> => {
        try {
            // Since there's no /auth/me endpoint, we'll use /user endpoint from UserController
            const response = await api.get<User>('/user');
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