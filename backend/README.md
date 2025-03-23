# XML Processor Backend

This is the backend service for the XML Processor application. It provides REST APIs for XML file processing, user authentication, and file management.

## Technologies Used

- Java 17
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- H2 Database
- Apache Camel
- Lombok

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the application:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The server will start on port 8080.

## API Endpoints

### Authentication
- POST `/login` - Login with username and password
- POST `/logout` - Logout current user
- GET `/api/user` - Get current user information

### File Processing
- POST `/api/upload` - Upload XML file for processing
- GET `/api/files/processed` - Get list of successfully processed files
- GET `/api/files/errors` - Get list of files with processing errors

### User Credentials

1. Admin User:
   - Username: `admin`
   - Password: `admin123`
   - Roles: ADMIN, USER

2. Regular User:
   - Username: `user`
   - Password: `user123`
   - Role: USER

## Database

The application uses H2 in-memory database. You can access the H2 console at:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:xmldb
- Username: sa
- Password: (empty) 