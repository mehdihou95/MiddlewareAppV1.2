import api from '../config/apiConfig';
import axios from 'axios';

const TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

interface AuthResponse {
    token: string;
    refreshToken: string;
}

export const tokenManager = {
    setToken: (token: string, refreshToken?: string): void => {
        localStorage.setItem(TOKEN_KEY, token);
        if (refreshToken) {
            localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
        }
    },
    getToken: (): string | null => localStorage.getItem(TOKEN_KEY),
    removeToken: (): void => localStorage.removeItem(TOKEN_KEY),
    getRefreshToken: (): string | null => localStorage.getItem(REFRESH_TOKEN_KEY),
    setRefreshToken: (token: string): void => localStorage.setItem(REFRESH_TOKEN_KEY, token),
    removeRefreshToken: (): void => localStorage.removeItem(REFRESH_TOKEN_KEY),
    hasToken: (): boolean => !!localStorage.getItem(TOKEN_KEY),
    hasRefreshToken: (): boolean => !!localStorage.getItem(REFRESH_TOKEN_KEY),
    clearTokens: (): void => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
    },
    isTokenValid: (): boolean => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (!token) return false;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch {
            return false;
        }
    },
    refreshToken: async (): Promise<boolean> => {
        try {
            const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
            if (!refreshToken) return false;

            const response = await api.post<AuthResponse>('/auth/refresh', { refreshToken });

            if (response.data.token) {
                tokenManager.setToken(response.data.token, response.data.refreshToken);
                return true;
            }
        } catch (error) {
            console.error('Token refresh failed:', error);
        }
        return false;
    }
};

export const setupAxiosInterceptors = (axiosInstance: typeof axios) => {
    axiosInstance.interceptors.response.use(
        (response) => response,
        async (error: any) => {
            const originalRequest = error.config;
            
            if (error.response?.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;
                
                try {
                    const refreshToken = tokenManager.getRefreshToken();
                    if (!refreshToken) {
                        throw new Error('No refresh token available');
                    }

                    const response = await api.post<AuthResponse>('/auth/refresh', { refreshToken });
                    const { token } = response.data;
                    
                    tokenManager.setToken(token);
                    originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    
                    return axiosInstance(originalRequest);
                } catch (refreshError) {
                    tokenManager.clearTokens();
                    window.location.href = '/login';
                    return Promise.reject(refreshError);
                }
            }
            
            return Promise.reject(error);
        }
    );
}; 