# XML Middleware Application - Technical Specification

## 1. System Architecture

### 1.1 Overview
The application follows a microservices architecture with a clear separation between frontend and backend components. It uses Spring Boot for the backend and React for the frontend, communicating via RESTful APIs.

### 1.2 Components
- Frontend (React with TypeScript)
  * Material-UI components
  * JWT authentication
  * CSRF protection
  * Client context management
  * Role-based UI
- Backend (Spring Boot)
  * JWT authentication service
  * Client management service
  * Role-based security
  * H2 database integration
- Database (H2)
  * User management
  * Client management
  * Role management
  * Audit logging

## 2. Technology Stack

### 2.1 Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: H2 (Development)
- **Security**: 
  * Spring Security with JWT
  * CSRF protection
  * Role-based access control
  * Client context isolation
- **Testing**: JUnit, Mockito
- **Logging**: SLF4J with Logback

### 2.2 Frontend
- **Framework**: React 18.x
- **Language**: TypeScript
- **State Management**: Context API
- **UI Components**: Material-UI
- **HTTP Client**: Axios with interceptors
- **Authentication**: JWT with refresh tokens
- **Security**: CSRF protection
- **Build Tool**: npm
- **Error Handling**: Custom error handler with logging

## 3. Database Schema

### 3.1 Core Tables
```sql
-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- User Roles Table
CREATE TABLE user_roles (
    user_id BIGINT,
    role VARCHAR(50),
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Clients Table
CREATE TABLE clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Audit Logs Table
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255),
    client_id BIGINT,
    action VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);
```

## 4. API Endpoints

### 4.1 Authentication
```
POST /api/auth/login
Request: {
    "username": string,
    "password": string
}
Response: {
    "token": string,
    "refreshToken": string,
    "username": string,
    "roles": string[],
    "csrfToken": string
}

POST /api/auth/logout
Request: Headers: {
    "Authorization": "Bearer {token}"
}
Response: {
    "message": "Logged out successfully"
}

POST /api/auth/refresh
Request: {
    "refreshToken": string
}
Response: {
    "token": string,
    "refreshToken": string,
    "username": string,
    "roles": string[]
}

GET /api/auth/validate
Response: {
    "valid": boolean,
    "username": string,
    "roles": string[]
}
```

### 4.2 Client Management
```
GET /api/clients
Headers: {
    "Authorization": "Bearer {token}",
    "X-XSRF-TOKEN": "{csrfToken}"
}
Response: {
    "content": Client[],
    "totalElements": number,
    "totalPages": number,
    "size": number,
    "number": number
}

POST /api/clients
Headers: {
    "Authorization": "Bearer {token}",
    "X-XSRF-TOKEN": "{csrfToken}"
}
Request: {
    "name": string,
    "code": string,
    "active": boolean
}
Response: Client

GET /api/clients/{id}
Headers: {
    "Authorization": "Bearer {token}"
}
Response: Client

PUT /api/clients/{id}
Headers: {
    "Authorization": "Bearer {token}",
    "X-XSRF-TOKEN": "{csrfToken}"
}
Request: {
    "name": string,
    "code": string,
    "active": boolean
}
Response: Client

DELETE /api/clients/{id}
Headers: {
    "Authorization": "Bearer {token}",
    "X-XSRF-TOKEN": "{csrfToken}"
}
```

## 5. Security Implementation

### 5.1 Authentication Flow
1. User submits credentials
2. Backend validates credentials
3. JWT tokens generated (access + refresh)
4. CSRF token generated
5. Tokens returned to frontend
6. Frontend stores tokens
7. Tokens included in subsequent requests
8. Auto refresh on token expiration

### 5.2 JWT Configuration
```java
@Configuration
public class JwtConfig {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // 1 hour
    
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration; // 24 hours
}
```

### 5.3 Security Filter Chain
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/api/clients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/clients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## 6. Frontend Implementation

### 6.1 API Service
```typescript
const createApiInstance = (): AxiosInstance => {
    const instance = axios.create({
        baseURL: API_URL,
        timeout: DEFAULT_TIMEOUT,
        withCredentials: true
    });

    instance.interceptors.request.use(config => {
        const token = tokenService.getAccessToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        
        const csrfToken = tokenService.getCsrfToken();
        if (csrfToken && config.method !== 'GET') {
            config.headers['X-XSRF-TOKEN'] = csrfToken;
        }
        
        return config;
    });

    return instance;
};
```

### 6.2 Token Service
```typescript
export const tokenService = {
    setTokens: (accessToken: string, refreshToken: string) => {
        localStorage.setItem('auth_token', accessToken);
        localStorage.setItem('refresh_token', refreshToken);
    },
    
    clearTokens: () => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('refresh_token');
    },
    
    isTokenValid: () => {
        const token = tokenService.getAccessToken();
        if (!token) return false;
        
        try {
            const payload = jwtDecode<JwtPayload>(token);
            return payload.exp * 1000 > Date.now();
        } catch {
            return false;
        }
    }
};
```

## 7. Current Implementation Status

### 7.1 Completed Features
- JWT authentication
- Role-based access control
- CSRF protection
- Client CRUD operations
- Token refresh mechanism
- Client context isolation
- Basic audit logging
- Error handling

### 7.2 In Progress
- Enhanced error handling
- Improved logging
- Performance optimization
- Testing coverage
- Documentation updates

### 7.3 Known Issues
1. Role prefix handling
2. Token refresh edge cases
3. CSRF token refresh scenarios
4. Client context persistence
5. Error message standardization

### 7.4 Next Steps
1. Implement remaining XML processing features
2. Add interface management
3. Enhance client configuration
4. Improve error handling
5. Add comprehensive testing
6. Optimize performance
7. Complete documentation 