export interface Client {
  id: number;
  name: string;
  code: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface Interface {
  id: number;
  name: string;
  description?: string;
  schemaPath?: string;
  clientId: number;
  status: string;
  type: string;
  isActive: boolean;
  priority: number;
  rootElement: string;
  namespace: string;
  configuration?: {
    xsdPath?: string;
  };
  createdAt?: string;
  updatedAt?: string;
}

export interface MappingRule {
  id?: number;
  clientId: number;
  interfaceId: number;
  xmlPath: string;
  databaseField: string;
  xsdElement: string;
  tableName: string;
  dataType: string;
  isAttribute: boolean;
  description: string;
  sourceField?: string;
  targetField?: string;
  transformationType?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProcessedFile {
  id: number;
  fileName: string;
  status: string;
  errorMessage?: string;
  interfaceEntity: Interface;
  processedData?: Record<string, any>;
  processedAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface User {
  id: number;
  username: string;
  password?: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
  lastLogin?: string;
  failedLoginAttempts?: number;
  accountLocked: boolean;
  passwordResetToken?: string;
  passwordResetExpiry?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: Sort;
    pageNumber: number;
    pageSize: number;
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  sort: Sort;
  numberOfElements: number;
  size: number;
  number: number;
  empty: boolean;
}

export interface Sort {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  details?: string[];
}

export interface AuditLog {
  id: number;
  action: string;
  username: string;
  clientId?: number;
  details: string;
  ipAddress: string;
  userAgent?: string;
  requestMethod?: string;
  requestUrl?: string;
  requestParams?: string;
  responseStatus?: number;
  errorMessage?: string;
  createdAt: string;
  executionTime?: number;
}

export interface XsdElement {
  name: string;
  path: string;
  type: string;
  isAttribute: boolean;
  minOccurs?: number;
  maxOccurs?: number;
  description?: string;
} 