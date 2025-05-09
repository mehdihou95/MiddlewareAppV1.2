import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, AxiosRequestHeaders } from 'axios';
import { tokenService, TokenResponse } from './tokenService';
import { API_URL, DEFAULT_TIMEOUT } from '../config/apiConfig';
import { authService } from './authService';

// Request retry configuration
interface RetryConfig extends AxiosRequestConfig {
  _retry?: boolean;
  _csrfRetry?: boolean;
}

// Response types
export interface ApiResponse<T = any> {
  data: T;
  status: number;
  statusText: string;
  headers: any;
}

export interface ErrorResponse {
  code: string;
  message: string;
  details?: string;
}

// Create API instance
const createApiInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: API_URL,
    timeout: DEFAULT_TIMEOUT,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // Enable cookies for CSRF
  });
  
  // Configure request interceptor
  instance.interceptors.request.use(
    async (config) => {
      if (!config.headers) {
        config.headers = {} as AxiosRequestHeaders;
      }
      
      // Debug: Log request details
      console.log('Making request to:', config.url);
      
      // Add Authorization header for all requests except auth endpoints
      if (!config.url?.includes('/auth/')) {
        const token = tokenService.getAccessToken();
        console.log('Token available for request:', !!token);
        
        if (token) {
          // Validate token before using it
          if (tokenService.isTokenValid(token)) {
            config.headers['Authorization'] = `Bearer ${token}`;
            console.log('Valid token added to Authorization header');
            
            // Log user info from token
            const userInfo = tokenService.getUserInfo();
            console.log('Current user info:', userInfo);
          } else {
            console.log('Token is invalid or expired, attempting refresh');
            try {
              const newToken = await authService.refreshToken();
              config.headers['Authorization'] = `Bearer ${newToken}`;
              console.log('New token obtained and added to Authorization header');
            } catch (error) {
              console.error('Token refresh failed:', error);
              // Only redirect to login if not already there and not trying to login
              if (!window.location.pathname.includes('/login')) {
                window.location.href = '/login';
                return Promise.reject('Authentication required');
              }
            }
          }
        } else {
          console.warn('No token available for request to:', config.url);
          // Only redirect to login if not already there and not trying to login
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
            return Promise.reject('Authentication required');
          }
        }
      }
      
      // Add CSRF token for non-GET requests
      if (config.method?.toUpperCase() !== 'GET') {
        const csrfToken = tokenService.getCsrfToken();
        if (csrfToken) {
          config.headers['X-XSRF-TOKEN'] = csrfToken;
          console.log('CSRF token added to request');
        }
      }
      
      // Add client context if available
      const clientId = localStorage.getItem('selectedClientId');
      if (clientId) {
        config.headers['X-Client-ID'] = clientId;
        console.log('Client context added:', clientId);
      }
      
      return config;
    },
    (error) => {
      console.error('Request interceptor error:', error);
      return Promise.reject(error);
    }
  );
  
  // Configure response interceptor
  instance.interceptors.response.use(
    (response) => {
      // Debug: Log response details
      console.log('Response received from:', response.config.url);
      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);
      
      // Store CSRF token from response headers if present
      const csrfToken = response.headers['x-xsrf-token'];
      if (csrfToken) {
        console.log('New CSRF token received');
        tokenService.setCsrfToken(csrfToken);
      }
      return response;
    },
    async (error: AxiosError) => {
      // Debug: Log error details
      console.error('Response error:', {
        status: error.response?.status,
        url: error.config?.url,
        message: error.message
      });
      
      const originalRequest = error.config as RetryConfig;
      if (!originalRequest) {
        return Promise.reject(error);
      }
      
      // Handle CSRF token expiration (403 Forbidden)
      if (error.response?.status === 403 && !originalRequest._csrfRetry) {
        console.log('Attempting CSRF token refresh');
        originalRequest._csrfRetry = true;
        
        try {
          const response = await instance.post<{ csrfToken: string }>('/auth/refresh-csrf');
          if (response.data.csrfToken) {
            console.log('New CSRF token obtained');
            tokenService.setCsrfToken(response.data.csrfToken);
            
            if (!originalRequest.headers) {
              originalRequest.headers = {};
            }
            originalRequest.headers['X-XSRF-TOKEN'] = response.data.csrfToken;
            
            return instance(originalRequest);
          }
        } catch (csrfError) {
          console.error('CSRF refresh failed:', csrfError);
          return Promise.reject(csrfError);
        }
      }
      
      // Handle JWT token expiration (401 Unauthorized)
      if (error.response?.status === 401 && !originalRequest._retry) {
        console.log('Attempting token refresh');
        originalRequest._retry = true;
        
        try {
          const refreshToken = tokenService.getRefreshToken();
          if (!refreshToken) {
            throw new Error('No refresh token available');
          }
          
          const response = await instance.post<TokenResponse>('/auth/refresh', { refreshToken });
          const { token, refreshToken: newRefreshToken } = response.data;
          
          console.log('New tokens obtained');
          tokenService.setTokens(token, newRefreshToken);
          
          if (!originalRequest.headers) {
            originalRequest.headers = {};
          }
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          
          return instance(originalRequest);
        } catch (refreshError) {
          console.error('Token refresh failed:', refreshError);
          tokenService.clearTokens();
          
          // Redirect to login page if not already there
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login';
          }
          
          return Promise.reject(refreshError);
        }
      }
      
      return Promise.reject(error);
    }
  );
  
  return instance;
};

// Create and export the API instance
export const api = createApiInstance();

// Helper methods for common API operations
export const apiService = {
  get: async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.get<T>(url, config);
    return response.data;
  },
  
  post: async <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.post<T>(url, data, config);
    return response.data;
  },
  
  put: async <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.put<T>(url, data, config);
    return response.data;
  },
  
  delete: async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.delete<T>(url, config);
    return response.data;
  },
  
  // Method to handle API errors consistently
  handleError: (error: any): ErrorResponse => {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<any>;
      
      // Return structured error response
      return {
        code: axiosError.response?.data?.code || 'ERROR',
        message: axiosError.response?.data?.message || 'An error occurred',
        details: axiosError.response?.data?.details,
      };
    }
    
    // Handle non-Axios errors
    return {
      code: 'UNKNOWN_ERROR',
      message: error.message || 'An unknown error occurred',
    };
  }
}; 