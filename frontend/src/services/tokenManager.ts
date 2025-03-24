const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

export interface TokenResponse {
    token: string;
    refreshToken: string;
    username: string;
    roles: string[];
}

export const tokenManager = {
    setAccessToken: (token: string) => {
        localStorage.setItem(ACCESS_TOKEN_KEY, token);
    },

    setRefreshToken: (token: string) => {
        localStorage.setItem(REFRESH_TOKEN_KEY, token);
    },

    getAccessToken: (): string | null => {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    },

    getRefreshToken: (): string | null => {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    },

    clearTokens: () => {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
    },

    isTokenValid: (token: string): boolean => {
        if (!token) return false;
        
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch (e) {
            return false;
        }
    },

    hasValidAccessToken: (): boolean => {
        const token = tokenManager.getAccessToken();
        return token ? tokenManager.isTokenValid(token) : false;
    }
}; 