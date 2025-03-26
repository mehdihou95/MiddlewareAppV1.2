import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, AxiosRequestHeaders } from 'axios';
import { tokenService, TokenResponse } from './tokenService';

// API configuration
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_URL = `${API_BASE_URL}/api`;

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
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true, // Enable cookies for CSRF
  });
  
  // Configure request interceptor
  instance.interceptors.request.use(
    (config) => {
      if (!config.headers) {
        config.headers = {} as AxiosRequestHeaders;
      }
      
      // Skip auth token for auth endpoints
      if (!config.url?.includes('/auth/')) {
        const token = tokenService.getAccessToken();
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`;
        }
      }
      
      // Add CSRF token for non-GET requests
      if (config.method?.toUpperCase() !== 'GET') {
        const csrfToken = tokenService.getCsrfToken();
        if (csrfToken) {
          config.headers['X-CSRF-TOKEN'] = csrfToken;
        }
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
    (response) => response,
    async (error: AxiosError) => {
      const originalRequest = error.config as RetryConfig;
      if (!originalRequest) {
        return Promise.reject(error);
      }
      
      // Handle CSRF token expiration (403 Forbidden)
      if (error.response?.status === 403 && !originalRequest._csrfRetry) {
        originalRequest._csrfRetry = true;
        
        try {
          const response = await instance.post<{ csrfToken: string }>('/auth/refresh-csrf');
          if (response.data.csrfToken) {
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
        originalRequest._retry = true;
        
        try {
          const refreshToken = tokenService.getRefreshToken();
          if (!refreshToken) {
            throw new Error('No refresh token available');
          }
          
          const response = await instance.post<TokenResponse>('/auth/refresh', { refreshToken });
          const { token, refreshToken: newRefreshToken } = response.data;
          
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