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

    isTokenValid: (providedToken?: string): boolean => {
        const token = providedToken || localStorage.getItem(ACCESS_TOKEN_KEY);
        if (!token) return false;
        
        try {
            // Decode JWT to check expiration
            const payload = JSON.parse(atob(token.split('.')[1]));
            
            // Check if token has required fields
            if (!payload || typeof payload.exp !== 'number') {
                console.error('Invalid token format: missing or invalid expiration');
                return false;
            }

            // Add buffer time (5 minutes) to prevent edge cases
            const bufferTime = 5 * 60 * 1000; // 5 minutes in milliseconds
            const currentTime = Date.now();
            const expirationTime = payload.exp * 1000; // Convert to milliseconds

            // Check if token is expired or will expire within buffer time
            if (expirationTime <= currentTime + bufferTime) {
                console.warn('Token is expired or will expire soon');
                return false;
            }

            return true;
        } catch (e) {
            console.error('Error validating token:', e);
            return false;
        }
    },

    hasValidAccessToken: (): boolean => {
        const token = tokenManager.getAccessToken();
        return token ? tokenManager.isTokenValid(token) : false;
    }
}; 