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
    csrfToken?: string;
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
            
            // Store JWT tokens
            tokenManager.setToken(response.data.token, response.data.refreshToken);
            
            // Store CSRF token in cookie if it's not already set by the server
            if (response.data.csrfToken) {
                const cookies = document.cookie.split(';');
                const hasCsrfCookie = cookies.some(cookie => cookie.trim().startsWith('XSRF-TOKEN='));
                if (!hasCsrfCookie) {
                    document.cookie = `XSRF-TOKEN=${response.data.csrfToken}; path=/`;
                }
            }
            
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
            tokenManager.clearTokens();
        } catch (error) {
            console.error('Logout failed:', error);
            // Clear tokens anyway
            tokenManager.clearTokens();
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
            tokenManager.clearTokens();
            throw error;
        }
    },

    validateToken: async (): Promise<ValidateTokenResponse> => {
        try {
            // The token is automatically added to the header by the interceptor
            const response = await api.get<ValidateTokenResponse>('/auth/validate');
            return response.data;
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