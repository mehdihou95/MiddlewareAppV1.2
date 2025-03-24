import axios from 'axios';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const TOKEN_EXPIRY_KEY = 'token_expiry';

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
    sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    
    // Set default authorization header
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  },

  getToken: (): string | null => {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  },

  getRefreshToken: (): string | null => {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  },

  isTokenValid: (): boolean => {
    const token = sessionStorage.getItem(ACCESS_TOKEN_KEY);
    const expiry = sessionStorage.getItem(TOKEN_EXPIRY_KEY);
    
    if (!token) {
      return false;
    }
    
    if (!expiry) {
      return true; // If no expiry is set, assume token is valid
    }
    
    return Date.now() < parseInt(expiry);
  },

  clearToken: () => {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(TOKEN_EXPIRY_KEY);
    delete axios.defaults.headers.common['Authorization'];
  },

  refreshToken: async (): Promise<boolean> => {
    try {
      const refreshToken = tokenManager.getRefreshToken();
      if (!refreshToken) {
        return false;
      }

      const response = await axios.post<RefreshResponse>('/api/auth/refresh', {
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