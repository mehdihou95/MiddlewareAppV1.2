import api from '../config/apiConfig';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

interface TokenData {
    token: string;
    refreshToken: string;
    expiresIn: number;
}

interface RefreshResponse {
    token: string;
    refreshToken: string;
    expiresIn: number;
}

export const tokenManager = {
    setToken: (token: string, refreshToken: string) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, token);
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    },

    getToken: (): string | null => {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    },

    getRefreshToken: (): string | null => {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    },

    isTokenValid: (): boolean => {
        const token = localStorage.getItem(ACCESS_TOKEN_KEY);
        if (!token) return false;
        
        try {
            // Decode JWT to check expiration
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch (e) {
            console.error('Error validating token:', e);
            return false;
        }
    },

    clearToken: () => {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
    },

    refreshToken: async (): Promise<boolean> => {
        try {
            const refreshToken = tokenManager.getRefreshToken();
            if (!refreshToken) {
                return false;
            }

            const response = await api.post<RefreshResponse>('/auth/refresh', {
                refreshToken
            });

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