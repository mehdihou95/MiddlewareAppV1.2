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