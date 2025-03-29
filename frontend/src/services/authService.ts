import { api, apiService } from './apiService';
import { tokenService, TokenResponse } from './tokenService';
import { handleApiError } from '../utils/errorHandler';
import { ENDPOINTS } from '../config/apiConfig';

export interface LoginCredentials {
    username: string;
    password: string;
}

export interface LoginResponse extends TokenResponse {
    username: string;
    roles: string[];
    csrfToken?: string;
}

export interface RefreshTokenResponse {
    token: string;
    refreshToken: string;
}

export interface ValidateTokenResponse {
    valid: boolean;
    username: string;
    roles: string[];
}

export interface User {
    id: number;
    username: string;
    email: string;
    roles: string[];
}

export const authService = {
    login: async (username: string, password: string): Promise<LoginResponse> => {
        console.log('Attempting login for user:', username);
        try {
            const response = await api.post<LoginResponse>(ENDPOINTS.AUTH.LOGIN, {
                username: username.trim(),
                password: password.trim()
            });
            
            console.log('Login response received:', response.data);
            
            // Store tokens and user info
            if (response.data.token) {
                console.log('Storing access token and user info');
                tokenService.setAccessToken(response.data.token);
                // Store user info in localStorage for persistence
                localStorage.setItem('user', JSON.stringify({
                    username: response.data.username,
                    roles: response.data.roles
                }));
            } else {
                console.error('No access token in response');
            }
            
            if (response.data.refreshToken) {
                console.log('Storing refresh token');
                tokenService.setRefreshToken(response.data.refreshToken);
            } else {
                console.error('No refresh token in response');
            }
            
            // Set Authorization header for future requests
            api.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
            console.log('Default Authorization header set');
            
            // Initialize API instance with stored token
            const userInfo = tokenService.getUserInfo();
            console.log('User info from token:', userInfo);
            
            return response.data;
        } catch (error) {
            console.error('Login failed:', error);
            throw apiService.handleError(error);
        }
    },

    logout: async (): Promise<void> => {
        console.log('Logging out user');
        try {
            // Call logout endpoint
            await api.post(ENDPOINTS.AUTH.LOGOUT);
        } catch (error) {
            console.error('Logout API call failed:', error);
            // Continue with logout process regardless of API call result
        } finally {
            // Clear tokens and API configuration
            tokenService.clearTokens();
            delete api.defaults.headers.common['Authorization'];
            delete api.defaults.headers.common['X-XSRF-TOKEN'];
            // Reset API instance
            api.interceptors.request.clear();
            api.interceptors.response.clear();
            // Redirect to login
            window.location.href = '/login';
        }
    },

    isAuthenticated: (): boolean => {
        return tokenService.isTokenValid();
    },

    refreshToken: async (): Promise<string> => {
        const refreshToken = tokenService.getRefreshToken();
        console.log('Attempting to refresh token');
        
        if (!refreshToken) {
            console.error('No refresh token available');
            throw new Error('No refresh token available');
        }

        try {
            const response = await api.post<LoginResponse>(ENDPOINTS.AUTH.REFRESH, {
                refreshToken: refreshToken
            });
            console.log('Token refresh successful');
            tokenService.setTokens(response.data.token, response.data.refreshToken);
            return response.data.token;
        } catch (error) {
            console.error('Token refresh failed:', error);
            tokenService.clearTokens();
            throw error;
        }
    },

    validateToken: async (): Promise<ValidateTokenResponse> => {
        try {
            const response = await api.get<ValidateTokenResponse>(ENDPOINTS.AUTH.VALIDATE);
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
            return await apiService.get<User>(ENDPOINTS.USER.CURRENT);
        } catch (error) {
            throw apiService.handleError(error);
        }
    },

    hasRole: (role: string): boolean => {
        const userInfo = tokenService.getUserInfo();
        return userInfo?.roles.includes(role) || false;
    }
};

// Export logout function for use in interceptors
export const logout = () => {
    delete api.defaults.headers.common['Authorization'];
    localStorage.removeItem('user');
    window.location.href = '/login';
}; 