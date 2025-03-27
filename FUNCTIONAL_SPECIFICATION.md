# XML Middleware Application - Functional Specification

## 1. Overview
The XML Middleware Application is a multi-tenant system designed to process and transform XML documents according to client-specific rules. It provides a secure, scalable platform for handling XML data transformation with support for multiple clients and interfaces.

## 2. User Roles and Access Control
### 2.1 Admin Users (ROLE_ADMIN)
- Full system access
- Client management capabilities (Create, Read, Update, Delete)
- Interface configuration
- Mapping rule management
- User management
- Audit log access
- System monitoring

### 2.2 Regular Users (ROLE_USER)
- Read-only access to client data
- View client list and details
- View interfaces
- View mapping rules
- View audit logs for assigned clients

## 3. Core Features

### 3.1 User Authentication and Authorization
- JWT-based authentication with separate access and refresh tokens
- Access token lifetime: 1 hour
- Refresh token lifetime: 24 hours
- Role-based access control (ADMIN, USER)
- Multi-tenant data isolation using client context
- Automatic token refresh mechanism
- Session invalidation on logout
- Password encryption with BCrypt
- Failed login attempt tracking
- CSRF protection for non-GET requests
- Token blacklisting on logout
- Debug logging for authentication process
- Client context management in requests

### 3.2 Client Management
- Create and manage client profiles
- Client status tracking (active/inactive)
- Client code validation (regex: ^[A-Z0-9-_]+$)
- Client name uniqueness enforcement
- Client-specific settings
- Role-based access to client operations:
  * ADMIN: Full CRUD operations
  * USER: Read-only access
- Client context isolation
- Client selection in UI
- Client data persistence in H2 database

### 3.3 Security Features
- HTTPS encryption for all communications
- CSRF token protection
- JWT token validation and refresh
- Role-based endpoint security
- Client context isolation
- Token blacklisting
- Secure password storage
- Rate limiting for login attempts
- Debug logging for security operations
- Session management

### 3.4 Frontend Features
- Modern Material-UI based interface
- Responsive design
- Client management dashboard
- User authentication flows
- Token management
- CSRF token handling
- Error handling and display
- Loading states
- Navigation guards
- Role-based UI elements

### 3.5 API Security
- JWT authentication required for all non-auth endpoints
- CSRF token required for state-changing operations
- Role-based endpoint access control
- Client context validation
- Rate limiting
- Error handling with appropriate status codes
- Secure header management
- Token refresh mechanism
- Debug logging for API operations

### 3.6 XML Processing
- Upload XML files
- Validate against XSD schemas
- Transform according to mapping rules
- Process multiple file formats
- Real-time processing status
- Asynchronous processing
- Retry mechanism for failed processing
- Comprehensive error handling

### 3.7 Mapping Rules
- Create and edit mapping rules
- Rule validation
- Version control for rules
- Rule testing capabilities
- Support for complex transformations
- Client-specific rule management

### 3.8 Interface Management
- Configure input/output interfaces
- Define interface schemas
- Manage interface versions
- Monitor interface performance
- Interface-specific validation

### 3.9 Audit Logging
- Comprehensive activity tracking
- User action logging
- System event logging
- Performance monitoring
- Error tracking
- Security event logging

## 4. User Interface Requirements

### 4.1 Dashboard
- Processing statistics
- Recent activity
- Error reports
- Performance metrics
- Audit log viewer
- Client-specific views

### 4.2 File Upload Interface
- Drag-and-drop functionality
- Multi-file upload
- Progress indicators
- Validation feedback
- Error reporting
- Processing status updates

### 4.3 Rule Management Interface
- Rule editor
- Rule testing tools
- Version history
- Documentation tools
- Validation feedback
- Client-specific rules

### 4.4 Reporting Interface
- Processing history
- Error logs
- Performance reports
- Audit trails
- Client-specific reports
- Export capabilities

### 4.5 Audit Log Interface
- Filterable log viewer
- Search capabilities
- Date range selection
- User-specific views
- Client-specific views
- Export functionality

## 5. Business Rules

### 5.1 Data Processing
- All XML files must be validated against schemas
- Processing must follow client-specific rules
- Failed validations must be logged
- Processed files must be archived
- Asynchronous processing for large files
- Automatic retry for failed processing
- Comprehensive error handling

### 5.2 Security
- Complete data isolation between clients using ClientContextHolder
- Encrypted data transmission over HTTPS
- Comprehensive audit logging of all user actions
- Regular security assessments and monitoring
- JWT-based authentication with token refresh
- Role-based access control (ADMIN, USER)
- Client-specific data isolation at service layer
- Password security with BCrypt encryption
- Session management with token invalidation
- Rate limiting for API endpoints

### 5.3 Performance
- Process files within 30 seconds
- Support concurrent processing
- Handle files up to 100MB
- Maintain 99.9% uptime
- Asynchronous processing
- Caching for frequently accessed data
- Optimized database queries

## 6. Compliance Requirements
- GDPR compliance for EU data
- Data retention policies
- Audit trail maintenance
- Security standards compliance
- Comprehensive logging
- Data isolation
- Access control

## 7. Error Handling
- Clear error messages
- Retry mechanisms
- Error notification system
- Error recovery procedures
- Validation error handling
- Processing error handling
- System error handling

## 8. Reporting Requirements
- Processing statistics
- Error reports
- Performance metrics
- Usage analytics
- Audit logs
- Client-specific reports
- System health reports

## 9. Integration Requirements
- REST API support
- Batch processing capabilities
- External system notifications
- Database integration
- File system integration
- Authentication service integration
- Audit logging integration

## 10. Service Level Agreements
- 99.9% system availability
- Maximum 30-second processing time
- 24/7 system monitoring
- Regular backup procedures
- Comprehensive audit logging
- Error recovery procedures
- Performance monitoring

## 11. Technical Implementation Details

### 11.1 Authentication Flow
1. User submits login credentials
2. Backend validates credentials and generates tokens
3. Frontend stores tokens securely
4. Access token used for subsequent requests
5. Refresh token used to obtain new access token
6. CSRF token included in state-changing requests
7. Tokens cleared on logout

### 11.2 Client Management Flow
1. Admin creates new client with unique name and code
2. Client status set to active/inactive
3. Client data isolated by context
4. Users assigned appropriate roles for client access
5. Client operations logged for audit purposes

### 11.3 Security Measures
1. BCrypt password encryption
2. JWT token validation
3. CSRF protection
4. Role-based access control
5. Client context isolation
6. Rate limiting
7. Token blacklisting
8. Secure session management

## 12. Current Limitations and Future Enhancements
1. Enhanced password policies
2. Multi-factor authentication
3. OAuth2 integration
4. Advanced client configuration options
5. Improved audit logging
6. Enhanced monitoring capabilities
7. Automated testing coverage
8. Performance optimization

## 13. Known Issues
1. Token refresh edge cases
2. Client context persistence
3. Role prefix handling in authentication
4. CSRF token refresh scenarios
5. Error message standardization

## 14. Development Status
- Authentication system: Implemented
- Client management: Basic implementation complete
- Role-based access: Implemented
- Security features: Core implementation complete
- Frontend UI: Basic implementation complete
- API endpoints: Core endpoints implemented
- Database: H2 implementation complete 