# Backend Architecture Documentation

## 1. System Overview

### 1.1 Core Components
```mermaid
graph TB
    Client[Frontend Client]
    Auth[Authentication Layer]
    Security[Security Filters]
    Controllers[REST Controllers]
    Services[Service Layer]
    Repos[Repository Layer]
    DB[(H2 Database)]

    Client --> Auth
    Auth --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> Repos
    Repos --> DB
```

## 2. Authentication Flow

### 2.1 Login Process
```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant AS as AuthService
    participant JS as JwtService
    participant US as UserService
    participant DB as Database

    C->>AC: POST /api/auth/login
    AC->>AS: authenticate(credentials)
    AS->>US: loadUserByUsername(username)
    US->>DB: findByUsername(username)
    DB-->>US: User
    US-->>AS: UserDetails
    AS->>JS: generateToken(userDetails)
    JS-->>AS: tokens
    AS-->>AC: AuthResponse
    AC-->>C: tokens + user info
```

### 2.2 Request Authentication Process
```mermaid
sequenceDiagram
    participant C as Client
    participant JF as JwtAuthFilter
    participant JS as JwtService
    participant US as UserService
    participant SC as SecurityContext

    C->>JF: HTTP Request
    JF->>JF: extractToken()
    JF->>JS: validateToken()
    JS->>US: loadUserByUsername()
    US-->>JS: UserDetails
    JS-->>JF: isValid
    JF->>SC: setAuthentication()
    JF->>C: Continue Request
```

## 3. Component Responsibilities

### 3.1 Security Layer
- **JwtAuthenticationFilter**
  - Location: `security/filter/JwtAuthenticationFilter.java`
  - Order: 2
  - Responsibilities:
    1. Extract JWT from request
    2. Validate token
    3. Load user details
    4. Set authentication context

- **ClientContextFilter**
  - Location: `filter/ClientContextFilter.java`
  - Order: 1
  - Responsibilities:
    1. Extract client context from request
    2. Validate client access
    3. Set client context holder

### 3.2 Controllers Layer
- **AuthController**
  - Location: `controller/AuthController.java`
  - Endpoints:
    * POST `/api/auth/login`
    * POST `/api/auth/logout`
    * POST `/api/auth/refresh`
    * GET `/api/auth/validate`

- **ClientController**
  - Location: `controller/ClientController.java`
  - Endpoints:
    * GET `/api/clients`
    * POST `/api/clients`
    * GET `/api/clients/{id}`
    * PUT `/api/clients/{id}`
    * DELETE `/api/clients/{id}`

### 3.3 Service Layer
- **JwtService**
  - Location: `security/service/JwtService.java`
  - Responsibilities:
    1. Generate access tokens
    2. Generate refresh tokens
    3. Validate tokens
    4. Extract claims
    5. Handle token blacklisting

- **UserService**
  - Location: `service/impl/UserServiceImpl.java`
  - Responsibilities:
    1. User CRUD operations
    2. Password management
    3. Role management
    4. User authentication

- **ClientService**
  - Location: `service/impl/ClientServiceImpl.java`
  - Responsibilities:
    1. Client CRUD operations
    2. Client validation
    3. Client context management

## 4. Process Flows

### 4.1 Application Startup
```mermaid
graph TB
    Start[Application Start] --> Config[Load Configuration]
    Config --> Security[Initialize Security]
    Security --> Filters[Register Filters]
    Filters --> DB[Initialize Database]
    DB --> Data[Load Initial Data]
    Data --> Ready[Application Ready]
```

### 4.2 Request Processing
```mermaid
graph TB
    Request[HTTP Request] --> CORS[CORS Filter]
    CORS --> Client[Client Context Filter]
    Client --> JWT[JWT Authentication Filter]
    JWT --> CSRF[CSRF Filter]
    CSRF --> Controller[Controller Layer]
    Controller --> Service[Service Layer]
    Service --> Repository[Repository Layer]
    Repository --> DB[(Database)]
```

## 5. Security Configuration

### 5.1 Filter Chain Order
1. CorsFilter (Order: -100)
2. ClientContextFilter (Order: 1)
3. JwtAuthenticationFilter (Order: 2)
4. CsrfFilter (Order: 3)

### 5.2 Security Rules
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("ADMIN", "USER")
    .requestMatchers(HttpMethod.POST, "/api/clients/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT, "/api/clients/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasRole("ADMIN")
    .anyRequest().authenticated()
)
```

## 6. Data Flow

### 6.1 Client Creation Process
```mermaid
stateDiagram-v2
    [*] --> ValidateRequest
    ValidateRequest --> CheckPermissions
    CheckPermissions --> ValidateClientData
    ValidateClientData --> GenerateClientId
    GenerateClientId --> SaveToDatabase
    SaveToDatabase --> UpdateAuditLog
    UpdateAuditLog --> [*]
```

### 6.2 Authentication Process
```mermaid
stateDiagram-v2
    [*] --> ValidateCredentials
    ValidateCredentials --> LoadUserDetails
    LoadUserDetails --> GenerateTokens
    GenerateTokens --> StoreTokens
    StoreTokens --> SetSecurityContext
    SetSecurityContext --> [*]
```

## 7. Error Handling

### 7.1 Global Exception Handling
```mermaid
graph TB
    Error[Exception Thrown] --> Handler[Global Exception Handler]
    Handler --> Type{Exception Type}
    Type -->|Authentication| Auth[Return 401]
    Type -->|Authorization| Forbidden[Return 403]
    Type -->|Validation| BadRequest[Return 400]
    Type -->|Other| Server[Return 500]
```

## 8. Database Operations

### 8.1 Transaction Flow
```mermaid
graph TB
    Start[Service Method] --> Begin[Begin Transaction]
    Begin --> Operation[Database Operation]
    Operation --> Success{Success?}
    Success -->|Yes| Commit[Commit Transaction]
    Success -->|No| Rollback[Rollback Transaction]
    Commit --> End[End]
    Rollback --> End
```

## 9. Initialization Process

### 9.1 Application Bootstrap
1. Load application.properties
2. Initialize Security Configuration
3. Set up Database Connection
4. Register Filters
5. Initialize Services
6. Load Default Data
7. Start Web Server

### 9.2 Default Data Creation
```mermaid
graph TB
    Start[Application Start] --> Check[Check Default Users]
    Check --> Admin{Admin Exists?}
    Admin -->|No| CreateAdmin[Create Admin User]
    Admin -->|Yes| User{User Exists?}
    CreateAdmin --> User
    User -->|No| CreateUser[Create Regular User]
    User -->|Yes| End[End]
    CreateUser --> End
``` 