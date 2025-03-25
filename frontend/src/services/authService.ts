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

interface ValidateTokenResponse {
    valid: boolean;
    username: string;
    roles: string[];
}

export const authService = {
    login: async (username: string, password: string): Promise<LoginResponse> => {
        console.log('Attempting login for user:', username);
        try {
            const response = await api.post<LoginResponse>('/auth/login', { username, password });
            console.log('Login successful, storing tokens');
            tokenManager.setToken(response.data.token, response.data.refreshToken);
            return response.data;
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    },

    logout: async (): Promise<void> => {
        console.log('Logging out user');
        try {
            await api.post('/auth/logout');
            console.log('Logout successful, clearing tokens');
            tokenManager.clearToken();
        } catch (error) {
            console.error('Logout failed:', error);
            // Clear tokens anyway
            tokenManager.clearToken();
            throw error;
        }
    },

    isAuthenticated: (): boolean => {
        return tokenManager.isTokenValid();
    },

    refreshToken: async (): Promise<string> => {
        const refreshToken = tokenManager.getRefreshToken();
        console.log('Attempting to refresh token');
        
        if (!refreshToken) {
            console.error('No refresh token available');
            throw new Error('No refresh token available');
        }

        try {
            const response = await api.post<LoginResponse>('/auth/refresh', {
                refreshToken: refreshToken
            });
            console.log('Token refresh successful');
            tokenManager.setToken(response.data.token, response.data.refreshToken);
            return response.data.token;
        } catch (error) {
            console.error('Token refresh failed:', error);
            tokenManager.clearToken();
            throw error;
        }
    },

    validateToken: async (): Promise<ValidateTokenResponse> => {
        const token = tokenManager.getToken();
        console.log('Validating token:', token ? 'Token exists' : 'No token found');
        
        if (!token) {
            return {
                valid: false,
                username: '',
                roles: []
            };
        }
        
        try {
            const response = await api.post<ValidateTokenResponse>('/auth/validate', {
                token: token
            });
            console.log('Token validation response:', response.data);
            return {
                ...response.data,
                valid: true
            };
        } catch (error) {
            console.error('Token validation failed:', error);
            return {
                valid: false,
                username: '',
                roles: []
            };
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