# XML Middleware Application - Technical Specification

## 1. System Architecture

### 1.1 Overview
The application follows a microservices architecture with a clear separation between frontend and backend components. It uses Spring Boot for the backend and React for the frontend, communicating via RESTful APIs.

### 1.2 Components
- Frontend (React)
- Backend (Spring Boot)
- Database (H2/PostgreSQL)
- File Storage System
- Authentication Service
- XML Processing Engine
- Audit Logging Service
- Caching Service

## 2. Technology Stack

### 2.1 Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 (Development), PostgreSQL (Production)
- **ORM**: JPA/Hibernate
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger/OpenAPI
- **Testing**: JUnit, Mockito
- **Migration**: Flyway
- **Caching**: Spring Cache
- **AOP**: Spring AOP for audit logging
- **Async Processing**: Spring Async

### 2.2 Frontend
- **Framework**: React 18.x
- **Language**: TypeScript
- **State Management**: Context API
- **UI Components**: Material-UI
- **HTTP Client**: Axios
- **Build Tool**: npm/yarn
- **Testing**: Jest, React Testing Library
- **Form Handling**: React Hook Form
- **Validation**: Yup
- **Error Handling**: Custom error handler

### 2.3 Development Tools
- **Version Control**: Git
- **CI/CD**: GitHub Actions
- **Code Quality**: SonarQube
- **IDE**: IntelliJ IDEA/VS Code
- **API Testing**: Postman
- **Database Tools**: DBeaver

## 3. Database Design

### 3.1 Core Entities
```sql
-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN,
    account_locked BOOLEAN,
    failed_login_attempts INTEGER,
    last_login TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- User Roles Table
CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(50),
    PRIMARY KEY (user_id, role)
);

-- Clients Table
CREATE TABLE clients (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Interfaces Table
CREATE TABLE interfaces (
    id BIGINT PRIMARY KEY,
    client_id BIGINT,
    name VARCHAR(255),
    type VARCHAR(50),
    schema_location VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Mapping Rules Table
CREATE TABLE mapping_rules (
    id BIGINT PRIMARY KEY,
    client_id BIGINT,
    interface_id BIGINT,
    name VARCHAR(255),
    rule_content TEXT,
    required BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Processed Files Table
CREATE TABLE processed_files (
    id BIGINT PRIMARY KEY,
    client_id BIGINT,
    interface_id BIGINT,
    filename VARCHAR(255),
    status VARCHAR(50),
    error_message TEXT,
    retry_count INTEGER,
    processed_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Audit Logs Table
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255),
    client_id BIGINT,
    action VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP
);
```

## 4. API Endpoints

### 4.1 Authentication
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/user`
- `POST /api/auth/refresh`

### 4.2 Client Management
- `GET /api/clients`
- `POST /api/clients`
- `GET /api/clients/{id}`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

### 4.3 Interface Management
- `GET /api/interfaces`
- `POST /api/interfaces`
- `GET /api/interfaces/{id}`
- `PUT /api/interfaces/{id}`
- `DELETE /api/interfaces/{id}`

### 4.4 Mapping Rules
- `GET /api/mapping-rules`
- `POST /api/mapping-rules`
- `GET /api/mapping-rules/{id}`
- `PUT /api/mapping-rules/{id}`
- `DELETE /api/mapping-rules/{id}`

### 4.5 File Processing
- `POST /api/files/upload`
- `GET /api/files/process/{id}`
- `GET /api/files/status/{id}`
- `POST /api/files/retry/{id}`

### 4.6 Audit Logs
- `GET /api/audit-logs`
- `GET /api/audit-logs/{id}`
- `GET /api/audit-logs/user/{username}`
- `GET /api/audit-logs/client/{clientId}`
- `GET /api/audit-logs/date-range`

## 5. Security Implementation

### 5.1 Authentication
- JWT-based authentication with refresh token support
- Access token expiration: 1 hour
- Refresh token expiration: 24 hours
- Password encryption using BCrypt
- CORS configuration for frontend access
- CSRF protection
- Rate limiting for API endpoints

### 5.2 Authorization
- Role-based access control (ADMIN, USER roles)
- Method-level security with @PreAuthorize
- Client-specific data isolation using ClientContextHolder
- Custom security annotations for endpoint protection
- Permission-based access with Spring Security

### 5.3 Data Security
- HTTPS encryption
- Input validation
- SQL injection prevention
- XSS protection
- Data encryption at rest
- Secure file handling

## 6. XML Processing Engine

### 6.1 Components
- XML Parser
- XSD Validator
- Transformation Engine
- Rule Processor
- Error Handler
- Retry Manager

### 6.2 Processing Flow
1. File Upload
2. Schema Validation
3. Rule Application
4. Transformation
5. Result Generation
6. Status Update
7. Audit Logging
8. Error Handling

## 7. Performance Optimization

### 7.1 Caching
- Interface schemas caching with Caffeine
- Mapping rules caching
- User authentication data
- Client configurations
- Token validation results
- Maximum cache size: 500 entries
- Cache expiration: 10 minutes after last access

### 7.2 Database
- Index optimization
- Query optimization
- Connection pooling
- Batch processing
- Caching layer
- Query result caching

### 7.3 File Processing
- Asynchronous processing
- Batch processing
- Parallel execution
- Chunked file handling
- Memory optimization
- Progress tracking

## 8. Monitoring and Logging

### 8.1 Application Metrics
- Response times for API endpoints
- Error rates and types
- Processing times for XML files
- Resource usage monitoring
- Cache hit/miss rates
- Database query performance
- Authentication success/failure rates
- Token refresh statistics

### 8.2 Logging
- Log levels
- Log rotation
- Error tracking
- Audit logging
- Performance logging
- Security logging
- System events

## 9. Deployment

### 9.1 Requirements
- Java 17 Runtime
- Node.js 16+
- PostgreSQL 13+
- Minimum 4GB RAM
- 2 CPU cores
- 50GB storage
- SSL certificate

### 9.2 Configuration
- Environment variables
- Database configuration
- File storage configuration
- Security settings
- Cache settings
- Logging configuration
- Monitoring setup

### 9.3 Deployment Process
1. Build frontend
2. Build backend
3. Database migration
4. Service deployment
5. Health check
6. Monitoring setup
7. Backup configuration
8. Security verification

## 10. Testing Strategy

### 10.1 Unit Testing
- Service layer tests
- Repository tests
- Controller tests
- Component tests
- Utility tests
- Validation tests
- Security tests

### 10.2 Integration Testing
- API endpoint tests
- Database integration
- File processing tests
- Security tests
- Cache tests
- Audit logging tests
- Error handling tests

### 10.3 Performance Testing
- Load testing
- Stress testing
- Endurance testing
- Scalability testing
- Cache performance
- Database performance
- File processing performance 