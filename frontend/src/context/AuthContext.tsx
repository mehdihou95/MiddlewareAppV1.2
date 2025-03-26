import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import { tokenManager } from '../utils/tokenManager';

// Configure axios defaults
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
axios.defaults.baseURL = API_URL.replace(/\/api$/, ''); // Remove /api if present to match service URLs

interface User {
  username: string;
  roles: string[];
  authenticated: boolean;
}

interface AuthResponse {
  token: string;
  refreshToken: string;
  username: string;
  roles: string[];
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<boolean>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const token = tokenManager.getToken();
      if (!token) {
        setUser(null);
        setLoading(false);
        return;
      }

      const response = await axios.get<User>('/api/auth/validate');
      if (response.status === 200 && response.data) {
        setUser({
          username: response.data.username,
          roles: response.data.roles,
          authenticated: true
        });
      } else {
        setUser(null);
        tokenManager.clearTokens();
      }
    } catch (err) {
      console.error('Auth check error:', err);
      setUser(null);
      tokenManager.clearTokens();
    } finally {
      setLoading(false);
    }
  };

  const login = async (username: string, password: string): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);

      const cleanUsername = username.trim();
      const cleanPassword = password.trim();

      if (!cleanUsername || !cleanPassword) {
        setError('Username and password are required');
        return false;
      }

      const response = await axios.post<AuthResponse>('/api/auth/login', {
        username: cleanUsername,
        password: cleanPassword
      });

      if (response.status === 200 && response.data) {
        const { token, refreshToken, username: responseUsername, roles } = response.data;
        tokenManager.setToken(token, refreshToken);
        
        setUser({
          username: responseUsername,
          roles,
          authenticated: true
        });

        // Set up axios default authorization header
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        return true;
      }
      return false;
    } catch (err: any) {
      console.error('Login error:', err.response || err);
      const errorMessage = err.response?.data?.message || 'Invalid username or password';
      setError(errorMessage);
      setUser(null);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      tokenManager.clearTokens();
      delete axios.defaults.headers.common['Authorization'];
      setUser(null);
      setError(null);
    }
  };

  const hasRole = (role: string): boolean => {
    return user?.roles.includes(role) ?? false;
  };

  // Set up axios interceptors for authentication
  useEffect(() => {
    const requestInterceptor = axios.interceptors.request.use(
      (config) => {
        const token = tokenManager.getToken();
        if (token && config.headers) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    const responseInterceptor = axios.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          
          try {
            const success = await tokenManager.refreshToken();
            if (success) {
              const token = tokenManager.getToken();
              if (token) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return axios(originalRequest);
              }
            }
          } catch (refreshError) {
            await logout();
          }
        }
        
        return Promise.reject(error);
      }
    );

    return () => {
      axios.interceptors.request.eject(requestInterceptor);
      axios.interceptors.response.eject(responseInterceptor);
    };
  }, []);

  const value = {
    user,
    loading,
    error,
    login,
    logout,
    isAuthenticated: !!user,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext; 