import api from '../config/apiConfig';

const TOKEN_KEY = 'auth_token';
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
        console.log('Setting new tokens');
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    },

    getToken: (): string | null => {
        const token = localStorage.getItem(TOKEN_KEY);
        console.log('Getting token:', token ? 'Token exists' : 'No token found');
        return token;
    },

    getRefreshToken: (): string | null => {
        const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
        console.log('Getting refresh token:', refreshToken ? 'Refresh token exists' : 'No refresh token found');
        return refreshToken;
    },

    isTokenValid: (): boolean => {
        const token = localStorage.getItem(TOKEN_KEY);
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
        console.log('Clearing all tokens');
        localStorage.removeItem(TOKEN_KEY);
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
    },

    hasToken: (): boolean => {
        const hasToken = !!localStorage.getItem(TOKEN_KEY);
        console.log('Checking if token exists:', hasToken);
        return hasToken;
    }
}; 