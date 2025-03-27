# API and Configuration Discrepancies Fix Guide

This guide provides precise, step-by-step instructions to fix the discrepancies found between frontend and backend in the MiddlewareAppV1.1 application.

## Implementation Plan Overview

1. Fix Authentication Configuration
2. Implement Missing Backend Endpoints
3. Fix Path Mismatch for Mapping Rules
4. Improve Client Loading Implementation
5. Standardize JWT Configuration
6. Test All Changes

## 1. Fix Authentication Configuration

### Step 1.1: Choose In-Memory JWT Blacklist Implementation
The in-memory implementation is recommended for simplicity and reliability unless you have a specific need for distributed token storage.

### Step 1.2: Update application.properties
1. Open `/backend/src/main/resources/application.properties`
2. Add the following line at the end of the file:
```
app.security.token-blacklist=in-memory
```

### Step 1.3: Comment Out Conflicting Configuration in application.yml
1. Open `/backend/src/main/resources/application.yml`
2. Comment out the Redis configuration section:
```yaml
# Redis Configuration (for distributed token blacklist and rate limiting)
#spring:
#  redis:
#    host: ${REDIS_HOST:localhost}
#    port: ${REDIS_PORT:6379}
#    password: ${REDIS_PASSWORD:}
#    
#  # Session Configuration
#  session:
#    store-type: redis
#    redis:
#      namespace: xml-processor:session
#    timeout: 1800  # 30 minutes
```

## 2. Implement Missing Backend Endpoints

### Step 2.1: Add Client Onboarding Endpoints
1. Create a new DTO class for client onboarding:
```java
// Create file: /backend/src/main/java/com/xml/processor/dto/ClientOnboardingDTO.java
package com.xml.processor.dto;

import lombok.Data;

@Data
public class ClientOnboardingDTO {
    private String name;
    private String description;
    private Boolean active = true;
}
```

2. Add methods to ClientService interface:
```java
// Update file: /backend/src/main/java/com/xml/processor/service/interfaces/ClientService.java
Client onboardNewClient(ClientOnboardingDTO clientData);
Client cloneClient(Long sourceClientId, ClientOnboardingDTO clientData);
```

3. Implement methods in ClientServiceImpl:
```java
// Update file: /backend/src/main/java/com/xml/processor/service/impl/ClientServiceImpl.java
@Override
public Client onboardNewClient(ClientOnboardingDTO clientData) {
    Client client = new Client();
    client.setName(clientData.getName());
    client.setDescription(clientData.getDescription());
    client.setActive(clientData.getActive() != null ? clientData.getActive() : true);
    client.setCreatedDate(LocalDateTime.now());
    return clientRepository.save(client);
}

@Override
public Client cloneClient(Long sourceClientId, ClientOnboardingDTO clientData) {
    Client sourceClient = clientRepository.findById(sourceClientId)
        .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + sourceClientId));
    
    Client newClient = new Client();
    newClient.setName(clientData.getName());
    newClient.setDescription(clientData.getDescription());
    newClient.setActive(clientData.getActive() != null ? clientData.getActive() : true);
    newClient.setCreatedDate(LocalDateTime.now());
    
    // Save the new client first to get an ID
    Client savedClient = clientRepository.save(newClient);
    
    // Clone interfaces if needed
    List<Interface> sourceInterfaces = interfaceRepository.findByClientId(sourceClientId);
    for (Interface sourceInterface : sourceInterfaces) {
        Interface newInterface = new Interface();
        newInterface.setName(sourceInterface.getName());
        newInterface.setDescription(sourceInterface.getDescription());
        newInterface.setType(sourceInterface.getType());
        newInterface.setActive(sourceInterface.isActive());
        newInterface.setClient(savedClient);
        interfaceRepository.save(newInterface);
    }
    
    return savedClient;
}
```

4. Add endpoints to ClientController:
```java
// Update file: /backend/src/main/java/com/xml/processor/controller/ClientController.java
@PostMapping("/onboarding/new")
public ResponseEntity<Client> onboardNewClient(@RequestBody ClientOnboardingDTO clientData) {
    Client client = clientService.onboardNewClient(clientData);
    return ResponseEntity.ok(client);
}

@PostMapping("/onboarding/clone/{sourceClientId}")
public ResponseEntity<Client> cloneClient(
    @PathVariable Long sourceClientId,
    @RequestBody ClientOnboardingDTO clientData
) {
    Client client = clientService.cloneClient(sourceClientId, clientData);
    return ResponseEntity.ok(client);
}
```

### Step 2.2: Add Interface Mappings Endpoints
1. Add methods to InterfaceService interface:
```java
// Update file: /backend/src/main/java/com/xml/processor/service/interfaces/InterfaceService.java
List<MappingRule> getInterfaceMappings(Long interfaceId);
List<MappingRule> updateInterfaceMappings(Long interfaceId, List<MappingRule> mappings);
```

2. Implement methods in InterfaceServiceImpl:
```java
// Update file: /backend/src/main/java/com/xml/processor/service/impl/InterfaceServiceImpl.java
@Override
public List<MappingRule> getInterfaceMappings(Long interfaceId) {
    Interface interfaceEntity = interfaceRepository.findById(interfaceId)
        .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + interfaceId));
    return mappingRuleRepository.findByInterfaceId(interfaceId);
}

@Override
public List<MappingRule> updateInterfaceMappings(Long interfaceId, List<MappingRule> mappings) {
    Interface interfaceEntity = interfaceRepository.findById(interfaceId)
        .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + interfaceId));
    
    // Delete existing mappings
    mappingRuleRepository.deleteByInterfaceId(interfaceId);
    
    // Set interface reference and save new mappings
    List<MappingRule> savedMappings = new ArrayList<>();
    for (MappingRule mapping : mappings) {
        mapping.setInterface(interfaceEntity);
        savedMappings.add(mappingRuleRepository.save(mapping));
    }
    
    return savedMappings;
}
```

3. Add endpoints to InterfaceController:
```java
// Update file: /backend/src/main/java/com/xml/processor/controller/InterfaceController.java
@GetMapping("/{id}/mappings")
public ResponseEntity<List<MappingRule>> getInterfaceMappings(@PathVariable Long id) {
    List<MappingRule> mappings = interfaceService.getInterfaceMappings(id);
    return ResponseEntity.ok(mappings);
}

@PutMapping("/{id}/mappings")
public ResponseEntity<List<MappingRule>> updateInterfaceMappings(
    @PathVariable Long id,
    @RequestBody List<MappingRule> mappings
) {
    List<MappingRule> updatedMappings = interfaceService.updateInterfaceMappings(id, mappings);
    return ResponseEntity.ok(updatedMappings);
}
```

## 3. Fix Path Mismatch for Mapping Rules

### Step 3.1: Add New Endpoint to Match Frontend Path
1. Update MappingRuleController to add a new endpoint that matches the frontend path:
```java
// Update file: /backend/src/main/java/com/xml/processor/controller/MappingRuleController.java
@GetMapping("/interfaces/{interfaceId}/mapping-rules")
public ResponseEntity<Page<MappingRule>> getMappingRulesByInterfaceId(
    @PathVariable Long interfaceId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "name") String sortBy,
    @RequestParam(defaultValue = "asc") String direction
) {
    // Reuse existing method to avoid duplication
    return getMappingRulesByInterface(interfaceId, page, size, sortBy, direction);
}
```

## 4. Improve Client Loading Implementation

### Step 4.1: Create Centralized API Configuration
1. Create a new apiConfig.ts file:
```typescript
// Create file: /frontend/src/config/apiConfig.ts
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
export const API_URL = `${API_BASE_URL}/api`;

// Default request timeout
export const DEFAULT_TIMEOUT = 30000; // 30 seconds

// API endpoints
export const ENDPOINTS = {
  // Auth endpoints
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    VALIDATE: '/auth/validate',
  },
  // Client endpoints
  CLIENTS: {
    BASE: '/clients',
    BY_ID: (id: number) => `/clients/${id}`,
    INTERFACES: (id: number) => `/clients/${id}/interfaces`,
    ONBOARDING: {
      NEW: '/clients/onboarding/new',
      CLONE: (sourceId: number) => `/clients/onboarding/clone/${sourceId}`,
    },
  },
  // Interface endpoints
  INTERFACES: {
    BASE: '/interfaces',
    BY_ID: (id: number) => `/interfaces/${id}`,
    CLIENT: '/interfaces/client',
    STATUS: (isActive: boolean) => `/interfaces/status/${isActive}`,
    MAPPINGS: (id: number) => `/interfaces/${id}/mappings`,
  },
  // Mapping rule endpoints
  MAPPING_RULES: {
    BASE: '/mapping-rules',
    BY_ID: (id: number) => `/mapping-rules/${id}`,
    BY_INTERFACE: (interfaceId: number) => `/interfaces/${interfaceId}/mapping-rules`,
  },
  // Processed file endpoints
  PROCESSED_FILES: {
    BASE: '/processed-files',
    BY_ID: (id: number) => `/processed-files/${id}`,
    BY_CLIENT: (clientId: number) => `/processed-files/client/${clientId}`,
    SEARCH: '/processed-files/search',
    STATUS: (status: string) => `/processed-files/status/${status}`,
    DATE_RANGE: '/processed-files/date-range',
    CLIENT_STATUS: (clientId: number, status: string) => 
      `/processed-files/client/${clientId}/status/${status}`,
    CLIENT_DATE_RANGE: (clientId: number) => 
      `/processed-files/client/${clientId}/date-range`,
  },
  // User endpoints
  USER: {
    CURRENT: '/user',
  },
};
```

### Step 4.2: Update apiService.ts to Use Centralized Configuration
1. Update apiService.ts:
```typescript
// Update file: /frontend/src/services/apiService.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError, AxiosRequestHeaders } from 'axios';
import { tokenService, TokenResponse } from './tokenService';
import { API_URL, DEFAULT_TIMEOUT } from '../config/apiConfig';

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
      
      // Add client context if available
      const clientId = localStorage.getItem('selectedClientId');
      if (clientId) {
        config.headers['X-Client-ID'] = clientId;
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
```

### Step 4.3: Improve Error Handling in ClientInterfaceContext.tsx
1. Update the refreshClients method in ClientInterfaceContext.tsx:
```typescript
// Update file: /frontend/src/context/ClientInterfaceContext.tsx
const refreshClients = React.useCallback(async (
  page = 0,
  pageSize = 10,
  sortField = 'name',
  sortOrder: 'asc' | 'desc' = 'asc'
) => {
  if (!isAuthenticated) return;
  
  try {
    setLoading(true);
    setError(null); // Clear error before making the request
    const response = await clientService.getAllClients({
      page,
      size: pageSize,
      sort: sortField,
      direction: sortOrder
    });
    setClients(response.content);
  } catch (err: any) {
    console.error('Error loading clients:', err);
    
    // Only clear data on authentication errors
    if (err.response?.status === 401 || err.response?.status === 403) {
      clearAllData();
      tokenService.clearTokens();
      navigate('/login');
    } else {
      // For other errors, just show error message but keep existing data
      setError('Failed to load clients. Please try again.');
    }
  } finally {
    setLoading(false);
  }
}, [isAuthenticated, navigate]);
```

## 5. Standardize JWT Configuration

### Step 5.1: Update JWT Expiration Time
1. Update application.properties:
```
# Update file: /backend/src/main/resources/application.properties
# Change the JWT expiration to 1 hour (3600000 ms)
application.security.jwt.expiration=3600000
```

## 6. Test All Changes

### Step 6.1: Test Authentication Flow
1. Start the backend server
2. Start the frontend application
3. Test login functionality
4. Verify token validation works correctly
5. Test token refresh functionality

### Step 6.2: Test Client Loading
1. Login to the application
2. Verify clients load correctly
3. Test client selection functionality
4. Verify interfaces load for selected client

### Step 6.3: Test New Endpoints
1. Test client onboarding endpoints:
   - Create a new client via onboarding
   - Clone an existing client
2. Test interface mappings endpoints:
   - Get mappings for an interface
   - Update mappings for an interface
3. Test mapping rules by interface endpoint:
   - Verify the new path works correctly

## Conclusion

This implementation plan addresses all the discrepancies found between the frontend and backend of the MiddlewareAppV1.1 application. By following these steps, you will:

1. Fix the authentication configuration conflict
2. Implement all missing backend endpoints
3. Fix the path mismatch for mapping rules
4. Improve client loading implementation with better error handling
5. Standardize JWT configuration

These changes will ensure proper communication between frontend and backend components, improving the overall stability and functionality of the application.
