# Backend Implementation Guide

This guide provides step-by-step instructions for implementing the recommended enhancements to the MiddlewareAppV1.1 backend. The implementation is organized into phases to allow for incremental improvements while maintaining application stability.

## Phase 1: Preparation and Setup

### Step 1: Create Backup of Current Code

```bash
# Create a backup directory
mkdir -p ~/backend-backup

# Copy current backend code to backup
cp -r /path/to/MiddlewareAppV1.1/backend ~/backend-backup/
```

### Step 2: Add Required Dependencies

Update your `pom.xml` to include the necessary dependencies:

```xml
<!-- Redis for distributed data structures -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Session management -->
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>

<!-- Metrics and monitoring -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- TOTP for MFA -->
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>

<!-- Improved validation -->
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
</dependency>

<!-- Lombok for reducing boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Step 3: Configure Application Properties

Update your `application.properties` or `application.yml` file:

```yaml
# JWT Configuration
application:
  security:
    jwt:
      secret-key: ${JWT_SECRET:your-default-secret-key-should-be-at-least-256-bits}
      expiration: 3600000  # 1 hour in milliseconds
      refresh-token:
        expiration: 604800000  # 7 days in milliseconds

# Redis Configuration (for distributed token blacklist and rate limiting)
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    
  # Session Configuration
  session:
    store-type: redis
    redis:
      namespace: xml-processor:session
    timeout: 1800  # 30 minutes

# Security Configuration
security:
  csrf:
    enabled: true
    cookie-name: XSRF-TOKEN
    header-name: X-XSRF-TOKEN
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: Authorization,Content-Type,X-XSRF-TOKEN
    allow-credentials: true
    
# Logging Configuration
logging:
  level:
    com.xml.processor: INFO
    com.xml.processor.security: DEBUG
    org.springframework.security: INFO
```

## Phase 2: Enhance Error Handling

### Step 1: Create Standardized Error Response Class

Create a new file `src/main/java/com/xml/processor/model/StandardErrorResponse.java`:

```java
package com.xml.processor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardErrorResponse {
    private String code;           // Application-specific error code
    private String message;        // User-friendly message
    private String detail;         // Detailed explanation (optional)
    private Map<String, String> fieldErrors; // For validation errors
    private List<String> errorList;  // For multiple errors
    private LocalDateTime timestamp;
    
    // Static factory methods for common error types
    public static StandardErrorResponse validationError(String message, Map<String, String> fieldErrors) {
        return StandardErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(message)
            .fieldErrors(fieldErrors != null ? fieldErrors : new HashMap<>())
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static StandardErrorResponse authenticationError(String message) {
        return StandardErrorResponse.builder()
            .code("AUTH_ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static StandardErrorResponse businessError(String code, String message) {
        return StandardErrorResponse.builder()
            .code(code)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public static StandardErrorResponse systemError(String message) {
        return StandardErrorResponse.builder()
            .code("SYSTEM_ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### Step 2: Create Error Code Constants

Create a new file `src/main/java/com/xml/processor/exception/ErrorCodes.java`:

```java
package com.xml.processor.exception;

public final class ErrorCodes {
    // Authentication errors (AUTH_*)
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_001";
    public static final String AUTH_EXPIRED_TOKEN = "AUTH_002";
    public static final String AUTH_INVALID_TOKEN = "AUTH_003";
    public static final String AUTH_INSUFFICIENT_PRIVILEGES = "AUTH_004";
    
    // Validation errors (VAL_*)
    public static final String VAL_MISSING_FIELD = "VAL_001";
    public static final String VAL_INVALID_FORMAT = "VAL_002";
    public static final String VAL_BUSINESS_RULE = "VAL_003";
    
    // Business errors (BUS_*)
    public static final String BUS_RESOURCE_NOT_FOUND = "BUS_001";
    public static final String BUS_DUPLICATE_RESOURCE = "BUS_002";
    public static final String BUS_OPERATION_NOT_ALLOWED = "BUS_003";
    
    // System errors (SYS_*)
    public static final String SYS_UNEXPECTED_ERROR = "SYS_001";
    public static final String SYS_SERVICE_UNAVAILABLE = "SYS_002";
    public static final String SYS_DATABASE_ERROR = "SYS_003";
    
    private ErrorCodes() {
        // Prevent instantiation
    }
}
```

### Step 3: Create Base Application Exception

Create a new file `src/main/java/com/xml/processor/exception/ApplicationException.java`:

```java
package com.xml.processor.exception;

public abstract class ApplicationException extends RuntimeException {
    private final String errorCode;
    
    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
```

### Step 4: Create Specific Exception Classes

Create several specific exception classes:

`src/main/java/com/xml/processor/exception/AuthenticationFailedException.java`:
```java
package com.xml.processor.exception;

public class AuthenticationFailedException extends ApplicationException {
    public AuthenticationFailedException(String message) {
        super(ErrorCodes.AUTH_INVALID_CREDENTIALS, message);
    }
}
```

`src/main/java/com/xml/processor/exception/TokenException.java`:
```java
package com.xml.processor.exception;

public class TokenException extends ApplicationException {
    public TokenException(String message) {
        super(ErrorCodes.AUTH_INVALID_TOKEN, message);
    }
    
    public static TokenException expired() {
        return new TokenException("Token has expired");
    }
    
    public static TokenException invalid() {
        return new TokenException("Token is invalid");
    }
    
    public static TokenException blacklisted() {
        return new TokenException("Token has been revoked");
    }
}
```

### Step 5: Update Global Exception Handler

Update `src/main/java/com/xml/processor/exception/GlobalExceptionHandler.java`:

```java
package com.xml.processor.exception;

import com.xml.processor.model.StandardErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import io.jsonwebtoken.JwtException;

import jakarta.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<StandardErrorResponse> handleApplicationException(ApplicationException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return ResponseEntity.status(status).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<StandardErrorResponse> handleValidationException(ValidationException ex) {
        StandardErrorResponse response = StandardErrorResponse.validationError(
            "Validation failed", 
            ex.getFieldErrors()
        );
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage(),
                (existing, replacement) -> existing + "; " + replacement
            ));
        
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.VAL_INVALID_FORMAT)
            .message("Validation failed")
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<StandardErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> fieldErrors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                violation -> violation.getMessage(),
                (existing, replacement) -> existing + "; " + replacement
            ));
        
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.VAL_INVALID_FORMAT)
            .message("Validation failed")
            .fieldErrors(fieldErrors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.AUTH_INVALID_CREDENTIALS)
            .message("Authentication failed")
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.AUTH_INSUFFICIENT_PRIVILEGES)
            .message("Access denied")
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.AUTH_INVALID_CREDENTIALS)
            .message("Invalid credentials")
            .detail("Username or password is incorrect")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<StandardErrorResponse> handleJwtException(JwtException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.AUTH_INVALID_TOKEN)
            .message("Invalid or expired token")
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        
        String message = "Database constraint violation";
        Throwable cause = ex.getRootCause();
        
        // Extract specific database error information
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            SQLIntegrityConstraintViolationException sqlEx = (SQLIntegrityConstraintViolationException) cause;
            if (sqlEx.getErrorCode() == 1062) { // MySQL duplicate entry
                message = "A record with this information already exists";
            } else if (sqlEx.getErrorCode() == 1452) { // MySQL foreign key constraint
                message = "Referenced record does not exist";
            }
        }
        
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.BUS_DUPLICATE_RESOURCE)
            .message(message)
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<StandardErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex) {
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.BUS_RESOURCE_NOT_FOUND)
            .message("Resource not found")
            .detail(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        
        StandardErrorResponse response = StandardErrorResponse.builder()
            .code(ErrorCodes.SYS_UNEXPECTED_ERROR)
            .message("An unexpected error occurred")
            .detail("Please try again later or contact support if the problem persists")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode.startsWith("AUTH_")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (errorCode.startsWith("VAL_")) {
            return HttpStatus.BAD_REQUEST;
        } else if (errorCode.startsWith("BUS_")) {
            if (errorCode.equals(ErrorCodes.BUS_RESOURCE_NOT_FOUND)) {
                return HttpStatus.NOT_FOUND;
            } else if (errorCode.equals(ErrorCodes.BUS_DUPLICATE_RESOURCE)) {
                return HttpStatus.CONFLICT;
            }
            return HttpStatus.BAD_REQUEST;
        } else if (errorCode.startsWith("SYS_")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
```

## Phase 3: Enhance JWT Token Implementation

### Step 1: Create Token Type Enum

Create a new file `src/main/java/com/xml/processor/security/model/TokenType.java`:

```java
package com.xml.processor.security.model;

public enum TokenType {
    ACCESS,
    REFRESH,
    RESET_PASSWORD,
    EMAIL_VERIFICATION,
    REMEMBER_ME
}
```

### Step 2: Update JwtService

Update `src/main/java/com/xml/processor/config/JwtService.java`:

```java
package com.xml.processor.config;

import com.xml.processor.exception.TokenException;
import com.xml.processor.security.model.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for JWT token generation, validation, and parsing.
 * <p>
 * This service handles all JWT token operations including:
 * <ul>
 *   <li>Generating access tokens</li>
 *   <li>Generating refresh tokens</li>
 *   <li>Validating tokens</li>
 *   <li>Extracting claims from tokens</li>
 * </ul>
 * <p>
 * Token expiration times are configured via application properties.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates an access token for the specified user.
     *
     * @param userDetails the user details
     * @return a JWT access token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, TokenType.ACCESS);
    }

    /**
     * Generates a token of the specified type for the user.
     *
     * @param userDetails the user details
     * @param tokenType the type of token to generate
     * @return a JWT token
     */
    public String generateToken(UserDetails userDetails, TokenType tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType.name());
        
        // Add roles to token
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        // Add client context if available
        Long clientId = ClientContextHolder.getClientId();
        if (clientId != null) {
            claims.put("clientId", clientId);
        }
        
        // Add fingerprint for access tokens
        if (tokenType == TokenType.ACCESS) {
            claims.put("fingerprint", generateFingerprint(userDetails));
        }
        
        long expiration = switch(tokenType) {
            case ACCESS -> jwtExpiration;
            case REFRESH -> refreshExpiration;
            case RESET_PASSWORD -> 900000; // 15 minutes
            case EMAIL_VERIFICATION -> 86400000; // 24 hours
            case REMEMBER_ME -> 2592000000L; // 30 days
        };
        
        return buildToken(claims, userDetails, expiration);
    }

    /**
     * Generates a refresh token for the specified user.
     *
     * @param userDetails the user details
     * @return a JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, TokenType.REFRESH);
    }

    /**
     * Builds a JWT token with the specified claims, user details, and expiration.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details
     * @param expiration the token expiration time in milliseconds
     * @return a JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token for the specified user.
     *
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final Claims claims = extractAllClaims(token);
            final String username = claims.getSubject();
            final Date expiration = claims.getExpiration();
            
            // First check basic validity: matching username and not expired
            if (!username.equals(userDetails.getUsername()) || expiration.before(new Date())) {
                return false;
            }
            
            // For access tokens, validate fingerprint if present
            // This helps prevent token theft by binding the token to the original request context
            if (claims.containsKey("type") && "ACCESS".equals(claims.get("type"))) {
                if (claims.containsKey("fingerprint")) {
                    // Generate expected fingerprint based on current request
                    String expectedFingerprint = generateFingerprint(userDetails);
                    String tokenFingerprint = claims.get("fingerprint", String.class);
                    
                    // If fingerprints don't match, token might be stolen
                    if (!expectedFingerprint.equals(tokenFingerprint)) {
                        log.warn("Token fingerprint mismatch for user: {}", username);
                        return false;
                    }
                }
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Extracts all claims from a JWT token.
     *
     * @param token the JWT token
     * @return the claims
     * @throws TokenException if the token is invalid
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw TokenException.expired();
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw TokenException.invalid();
        }
    }

    /**
     * Gets the signing key for JWT tokens.
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts a JWT token from an Authentication object.
     *
     * @param authentication the Authentication object
     * @return the JWT token, or null if not found
     */
    public String getTokenFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }
    
    /**
     * Extracts a JWT token from an HTTP request.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not found
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
    
    /**
     * Generates a fingerprint for a token based on user details and request information.
     *
     * @param userDetails the user details
     * @return the fingerprint
     */
    private String generateFingerprint(UserDetails userDetails) {
        try {
            HttpServletRequest request = 
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            return DigestUtils.sha256Hex(userDetails.getUsername() + ":" + userAgent + ":" + ipAddress);
        } catch (Exception e) {
            log.warn("Could not generate token fingerprint: {}", e.getMessage());
            return DigestUtils.sha256Hex(userDetails.getUsername() + ":unknown");
        }
    }
    
    /**
     * Generates a test token for health checks.
     *
     * @return a test JWT token
     */
    public String generateTestToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "TEST");
        
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject("test-user")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validates a test token.
     *
     * @param token the test token
     * @return true if valid, false otherwise
     */
    public boolean validateTestToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "TEST".equals(claims.get("type")) && "test-user".equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Step 3: Implement Redis-Based Token Blacklist

Create a new file `src/main/java/com/xml/processor/config/RedisConfig.java`:

```java
package com.xml.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.redis.port:6379}")
    private int redisPort;
    
    @Value("${spring.redis.password:}")
    private String redisPassword;
    
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (!redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
    
    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
```

Create a new file `src/main/java/com/xml/processor/security/service/JwtBlacklistService.java`:

```java
package com.xml.processor.security.service;

public interface JwtBlacklistService {
    void blacklistToken(String token);
    boolean isBlacklisted(String token);
}
```

Create a new file `src/main/java/com/xml/processor/security/service/RedisJwtBlacklistService.java`:

```java
package com.xml.processor.security.service;

import com.xml.processor.config.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.security.token-blacklist", havingValue = "redis")
public class RedisJwtBlacklistService implements JwtBlacklistService {
    
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;
    
    public RedisJwtBlacklistService(StringRedisTemplate redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }
    
    @Override
    public void blacklistToken(String token) {
        try {
            // Extract expiration time
            Date expiration = jwtService.extractExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            
            if (ttl > 0) {
                // Store token hash in Redis with TTL
                String tokenHash = DigestUtils.sha256Hex(token);
                redisTemplate.opsForValue().set("blacklist:" + tokenHash, "1", ttl, TimeUnit.MILLISECONDS);
                log.info("Token blacklisted until {}", expiration);
            } else {
                log.info("Token already expired, no need to blacklist");
            }
        } catch (Exception e) {
            log.error("Error blacklisting token: {}", e.getMessage());
        }
    }
    
    @Override
    public boolean isBlacklisted(String token) {
        try {
            String tokenHash = DigestUtils.sha256Hex(token);
            Boolean exists = redisTemplate.hasKey("blacklist:" + tokenHash);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking blacklisted token: {}", e.getMessage());
            return false;
        }
    }
}
```

Update the existing `src/main/java/com/xml/processor/service/impl/JwtBlacklistService.java` to implement the interface:

```java
package com.xml.processor.service.impl;

import com.xml.processor.config.JwtService;
import com.xml.processor.security.service.JwtBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.security.token-blacklist", havingValue = "in-memory", matchIfMissing = true)
public class JwtBlacklistService implements com.xml.processor.security.service.JwtBlacklistService {
    
    private final ConcurrentMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    
    public JwtBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void blacklistToken(String token) {
        blacklistedTokens.put(token, Instant.now());
        log.info("Token blacklisted. Current blacklist size: {}", blacklistedTokens.size());
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (!blacklistedTokens.containsKey(token)) {
            return false;
        }
        
        // Check if the token has been in the blacklist longer than the JWT expiration time
        Instant blacklistedAt = blacklistedTokens.get(token);
        if (Instant.now().isAfter(blacklistedAt.plusMillis(jwtExpiration))) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of blacklisted tokens. Current size: {}", blacklistedTokens.size());
        Instant now = Instant.now();
        int initialSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry ->
            now.isAfter(entry.getValue().plusMillis(jwtExpiration)));
        
        int removedCount = initialSize - blacklistedTokens.size();
        log.info("Completed blacklist cleanup. Removed {} expired tokens. New size: {}", 
            removedCount, blacklistedTokens.size());
    }
}
```

### Step 4: Update JwtAuthenticationFilter

Update `src/main/java/com/xml/processor/config/JwtAuthenticationFilter.java`:

```java
package com.xml.processor.config;

import com.xml.processor.security.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("Processing request to: {}", path);
        
        // Skip authentication for public endpoints
        if (shouldSkipAuthentication(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String token = jwtService.extractTokenFromRequest(request);
        if (token == null) {
            log.debug("No JWT token found for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String userEmail = jwtService.extractUsername(token);
            
            log.debug("JWT token extracted for user: {}", userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(token, userDetails) && !jwtBlacklistService.isBlacklisted(token)) {
                    log.debug("Valid token for user: {}", userEmail);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            token, // Store token as credentials for potential access in services
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("Invalid or blacklisted token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
    
    private boolean shouldSkipAuthentication(String path) {
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/validate") ||
               path.startsWith("/h2-console") ||
               path.equals("/error") ||
               path.equals("/favicon.ico");
    }
}
```

## Phase 4: Enhance CSRF Protection

### Step 1: Create CSRF Configuration

Create a new file `src/main/java/com/xml/processor/security/config/CsrfConfig.java`:

```java
package com.xml.processor.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Slf4j
@Configuration
public class CsrfConfig {
    
    @Value("${security.csrf.cookie-name:XSRF-TOKEN}")
    private String cookieName;
    
    @Value("${security.csrf.header-name:X-XSRF-TOKEN}")
    private String headerName;
    
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieName(cookieName);
        repository.setHeaderName(headerName);
        
        log.info("Configured CSRF token repository with cookie name: {} and header name: {}", 
            cookieName, headerName);
        
        return repository;
    }
    
    @Bean
    public CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
        return new CsrfTokenRequestAttributeHandler();
    }
}
```

### Step 2: Create CSRF Token Service

Create a new file `src/main/java/com/xml/processor/security/service/CsrfTokenService.java`:

```java
package com.xml.processor.security.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CsrfTokenService {
    
    private final CsrfTokenRepository csrfTokenRepository;
    
    @Value("${security.csrf.cookie-name:XSRF-TOKEN}")
    private String cookieName;
    
    @Value("${security.csrf.header-name:X-XSRF-TOKEN}")
    private String headerName;
    
    public CsrfTokenService(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }
    
    /**
     * Generates a new CSRF token and saves it to the response.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the generated CSRF token
     */
    public CsrfToken generateToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken token = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(token, request, response);
        
        // Set secure cookie attributes
        Cookie cookie = new Cookie(cookieName, token.getToken());
        cookie.setPath("/");
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(false);
        cookie.setMaxAge(3600); // 1 hour
        
        // Add SameSite attribute
        response.setHeader("Set-Cookie", 
            String.format("%s=%s; Path=%s; Max-Age=%d; %sHttpOnly=%s; SameSite=%s", 
                cookieName, 
                token.getToken(), 
                "/", 
                3600, 
                request.isSecure() ? "Secure; " : "", 
                "false", 
                "Lax"));
        
        response.setHeader(token.getHeaderName(), token.getToken());
        
        log.debug("Generated new CSRF token");
        
        return token;
    }
    
    /**
     * Rotates the CSRF token by generating a new one.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @return the new CSRF token
     */
    public CsrfToken rotateToken(HttpServletRequest request, HttpServletResponse response) {
        return generateToken(request, response);
    }
}
```

### Step 3: Update AuthController for CSRF

Update the CSRF-related methods in `src/main/java/com/xml/processor/controller/AuthController.java`:

```java
@PostMapping("/refresh-csrf")
public ResponseEntity<Map<String, String>> refreshCsrfToken(HttpServletRequest request, HttpServletResponse response) {
    // Get the current authentication
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "User not authenticated"));
    }

    // Generate new CSRF token
    CsrfToken token = csrfTokenService.generateToken(request, response);
    
    return ResponseEntity.ok(Map.of("csrfToken", token.getToken()));
}
```

### Step 4: Update Security Configuration

Update `src/main/java/com/xml/processor/config/SecurityConfig.java` to use the new CSRF configuration:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                             CsrfTokenRepository csrfTokenRepository,
                                             CsrfTokenRequestAttributeHandler csrfTokenRequestHandler) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(csrfTokenRepository)
            .csrfTokenRequestHandler(csrfTokenRequestHandler)
            .ignoringRequestMatchers(
                "/api/auth/login",
                "/api/auth/refresh",
                "/api/auth/refresh-csrf",
                "/api/auth/validate",
                "/h2-console/**",
                "/error",
                "/favicon.ico"
            )
        )
        // Rest of the configuration...
        
    return http.build();
}
```

## Phase 5: Implement Security Headers

### Step 1: Create Security Headers Filter

Create a new file `src/main/java/com/xml/processor/security/filter/SecurityHeadersFilter.java`:

```java
package com.xml.processor.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that adds security headers to HTTP responses.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Add security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self'; object-src 'none'; " +
            "frame-ancestors 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline';");
        
        // Cache control for sensitive pages
        if (request.getRequestURI().startsWith("/api/auth/") || 
            request.getRequestURI().startsWith("/api/user/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Step 2: Update Security Configuration

Update `src/main/java/com/xml/processor/config/SecurityConfig.java` to include security headers:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Existing configuration...
        
        .headers(headers -> headers
            .xssProtection(xss -> xss.enable())
            .contentTypeOptions(contentType -> contentType.disable())
            .frameOptions(frame -> frame.deny())
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; " +
                                "frame-ancestors 'self'; img-src 'self' data:; " +
                                "style-src 'self' 'unsafe-inline';")
            )
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)
            )
            .permissionsPolicy(permissions -> permissions
                .policy("camera=(), microphone=(), geolocation=()")
            )
        );
        
    return http.build();
}
```

## Phase 6: Implement Rate Limiting

### Step 1: Create Enhanced Rate Limiter

Create a new file `src/main/java/com/xml/processor/security/service/EnhancedRateLimiter.java`:

```java
package com.xml.processor.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class EnhancedRateLimiter {
    
    // Primary rate limit: 5 attempts per 5 minutes
    private static final int PRIMARY_MAX_ATTEMPTS = 5;
    private static final int PRIMARY_WINDOW_MS = 300000;
    
    // Secondary rate limit: 10 attempts per hour across all IPs for a username
    private static final int SECONDARY_MAX_ATTEMPTS = 10;
    private static final int SECONDARY_WINDOW_MS = 3600000;
    
    // Tertiary rate limit: Progressive backoff
    private static final int MAX_BACKOFF_MINUTES = 60; // Max 1 hour backoff
    
    private final ConcurrentMap<String, AttemptInfo> ipAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AttemptInfo> usernameAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> backoffMinutes = new ConcurrentHashMap<>();
    
    private static class AttemptInfo {
        final AtomicInteger count;
        final Instant windowStart;

        AttemptInfo() {
            this.count = new AtomicInteger(1);
            this.windowStart = Instant.now();
        }
    }
    
    /**
     * Checks if a request is within rate limits.
     *
     * @param ipAddress the IP address
     * @param username the username (can be null)
     * @return true if within limits, false if rate limited
     */
    public boolean checkRateLimit(String ipAddress, String username) {
        cleanup();
        
        // Check IP-based rate limit
        String ipKey = "ip:" + ipAddress;
        if (!checkSingleRateLimit(ipKey, PRIMARY_MAX_ATTEMPTS, PRIMARY_WINDOW_MS)) {
            log.warn("IP rate limit exceeded: {}", ipAddress);
            return false;
        }
        
        // Check username-based rate limit if username is provided
        if (username != null && !username.isEmpty()) {
            String usernameKey = "user:" + username;
            if (!checkSingleRateLimit(usernameKey, SECONDARY_MAX_ATTEMPTS, SECONDARY_WINDOW_MS)) {
                log.warn("Username rate limit exceeded: {}", username);
                return false;
            }
            
            // Check backoff
            String backoffKey = ipAddress + ":" + username;
            Integer backoffMinute = backoffMinutes.get(backoffKey);
            if (backoffMinute != null) {
                log.warn("Backoff active for {}:{} - {} minutes", ipAddress, username, backoffMinute);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks a single rate limit.
     *
     * @param key the rate limit key
     * @param maxAttempts the maximum attempts allowed
     * @param windowMs the time window in milliseconds
     * @return true if within limits, false if rate limited
     */
    private boolean checkSingleRateLimit(String key, int maxAttempts, int windowMs) {
        AttemptInfo info = ipAttempts.compute(key, (k, v) -> {
            if (v == null || isWindowExpired(v.windowStart, windowMs)) {
                return new AttemptInfo();
            }
            v.count.incrementAndGet();
            return v;
        });

        int attemptCount = info.count.get();
        if (attemptCount > maxAttempts) {
            return false;
        }

        log.debug("Rate limit check for key: {}. Attempts: {}/{}", key, attemptCount, maxAttempts);
        return true;
    }
    
    /**
     * Records a failed authentication attempt and applies progressive backoff.
     *
     * @param ipAddress the IP address
     * @param username the username
     */
    public void recordFailedAttempt(String ipAddress, String username) {
        if (username == null || username.isEmpty()) {
            return;
        }
        
        String backoffKey = ipAddress + ":" + username;
        Integer currentBackoff = backoffMinutes.get(backoffKey);
        
        if (currentBackoff == null) {
            // First failed attempt, set 1 minute backoff
            backoffMinutes.put(backoffKey, 1);
        } else {
            // Progressive backoff - double the time up to max
            int newBackoff = Math.min(currentBackoff * 2, MAX_BACKOFF_MINUTES);
            backoffMinutes.put(backoffKey, newBackoff);
        }
        
        log.info("Set backoff for {}:{} to {} minutes", ipAddress, username, backoffMinutes.get(backoffKey));
    }
    
    /**
     * Resets rate limits for a specific user.
     *
     * @param ipAddress the IP address
     * @param username the username
     */
    public void resetLimit(String ipAddress, String username) {
        if (ipAddress != null) {
            ipAttempts.remove("ip:" + ipAddress);
        }
        
        if (username != null && !username.isEmpty()) {
            usernameAttempts.remove("user:" + username);
            
            if (ipAddress != null) {
                backoffMinutes.remove(ipAddress + ":" + username);
            }
        }
        
        log.debug("Rate limit reset for IP: {}, username: {}", ipAddress, username);
    }
    
    /**
     * Checks if a time window has expired.
     *
     * @param windowStart the start time of the window
     * @param windowMs the window duration in milliseconds
     * @return true if expired, false otherwise
     */
    private boolean isWindowExpired(Instant windowStart, int windowMs) {
        return Instant.now().isAfter(windowStart.plusMillis(windowMs));
    }
    
    /**
     * Cleans up expired rate limit entries.
     */
    private void cleanup() {
        Instant now = Instant.now();
        
        // Clean up IP attempts
        ipAttempts.entrySet().removeIf(entry -> 
            isWindowExpired(entry.getValue().windowStart, PRIMARY_WINDOW_MS));
        
        // Clean up username attempts
        usernameAttempts.entrySet().removeIf(entry -> 
            isWindowExpired(entry.getValue().windowStart, SECONDARY_WINDOW_MS));
        
        // Clean up backoff entries (assume 1-day max backoff)
        backoffMinutes.entrySet().removeIf(entry -> {
            int backoffMs = entry.getValue() * 60 * 1000;
            String[] parts = entry.getKey().split(":");
            String ipAddress = parts[0];
            String username = parts[1];
            
            String ipKey = "ip:" + ipAddress;
            String usernameKey = "user:" + username;
            
            // If neither IP nor username has recent attempts, remove backoff
            boolean ipHasAttempts = ipAttempts.containsKey(ipKey);
            boolean usernameHasAttempts = usernameAttempts.containsKey(usernameKey);
            
            return !ipHasAttempts && !usernameHasAttempts;
        });
    }
}
```

### Step 2: Update AuthController to Use Enhanced Rate Limiter

Update `src/main/java/com/xml/processor/controller/AuthController.java` to use the enhanced rate limiter:

```java
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, 
                                              HttpServletRequest servletRequest, 
                                              HttpServletResponse response) {
    String ipAddress = servletRequest.getRemoteAddr();
    String username = request.getUsername();
    
    // Check rate limiting
    if (!enhancedRateLimiter.checkRateLimit(ipAddress, username)) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(Map.of("error", "Too many login attempts. Please try again later."));
    }
    
    try {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate tokens
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        // Reset rate limiting on successful login
        enhancedRateLimiter.resetLimit(ipAddress, username);
        
        // Create response
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", accessToken);
        responseBody.put("refreshToken", refreshToken);
        responseBody.put("username", userDetails.getUsername());
        responseBody.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        // Add CSRF token
        CsrfToken csrf = (CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            Cookie cookie = new Cookie("XSRF-TOKEN", csrf.getToken());
            cookie.setPath("/");
            cookie.setHttpOnly(false);
            cookie.setSecure(servletRequest.isSecure());
            response.addCookie(cookie);
            response.setHeader(csrf.getHeaderName(), csrf.getToken());
            responseBody.put("csrfToken", csrf.getToken());
        }
        
        return ResponseEntity.ok(responseBody);
    } catch (BadCredentialsException e) {
        // Record failed attempt for backoff
        enhancedRateLimiter.recordFailedAttempt(ipAddress, username);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid username or password"));
    }
}
```

## Phase 7: Implement Health Checks

### Step 1: Create Security Health Indicator

Create a new file `src/main/java/com/xml/processor/security/health/SecurityHealthIndicator.java`:

```java
package com.xml.processor.security.health;

import com.xml.processor.config.JwtService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SecurityHealthIndicator implements HealthIndicator {
    
    private final JwtService jwtService;
    
    public SecurityHealthIndicator(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public Health health() {
        try {
            // Verify JWT service is working
            String testToken = jwtService.generateTestToken();
            boolean isValid = jwtService.validateTestToken(testToken);
            
            if (isValid) {
                return Health.up()
                    .withDetail("jwtService", "operational")
                    .build();
            } else {
                return Health.down()
                    .withDetail("jwtService", "validation failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("jwtService", "failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Step 2: Configure Actuator Endpoints

Update `application.properties` or `application.yml`:

```yaml
# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized
      roles: ADMIN
  health:
    defaults:
      enabled: true
```

## Phase 8: Testing and Verification

### Step 1: Create Test Configuration

Create a new file `src/test/resources/application-test.yml`:

```yaml
# Test Configuration
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: password
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  redis:
    host: localhost
    port: 6379

# Security Configuration
application:
  security:
    jwt:
      secret-key: testsecrettestsecrettestsecrettestsecrettestsecrettestsecret
      expiration: 60000  # 1 minute for testing
      refresh-token:
        expiration: 120000  # 2 minutes for testing

# Disable rate limiting for tests
app:
  security:
    token-blacklist: in-memory
    rate-limiting:
      enabled: false
```

### Step 2: Create Authentication Tests

Create a new file `src/test/java/com/xml/processor/security/AuthenticationTest.java`:

```java
package com.xml.processor.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xml.processor.config.JwtService;
import com.xml.processor.controller.AuthController;
import com.xml.processor.security.model.TokenType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Test
    public void testLoginSuccess() throws Exception {
        // Prepare login request
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin@example.com");
        loginRequest.put("password", "password");
        
        // Perform login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.username").value("admin@example.com"))
            .andExpect(jsonPath("$.roles").isArray())
            .andReturn();
        
        // Extract token
        String responseContent = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
        String token = (String) responseMap.get("token");
        
        // Verify token
        assertNotNull(token);
        String username = jwtService.extractUsername(token);
        assertTrue(username.equals("admin@example.com"));
    }
    
    @Test
    public void testLoginFailure() throws Exception {
        // Prepare login request with wrong password
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin@example.com");
        loginRequest.put("password", "wrongpassword");
        
        // Perform login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    public void testTokenRefresh() throws Exception {
        // Generate refresh token
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");
        String refreshToken = jwtService.generateToken(userDetails, TokenType.REFRESH);
        
        // Prepare refresh request
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);
        
        // Perform refresh
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.refreshToken").exists());
    }
    
    @Test
    public void testLogout() throws Exception {
        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");
        String token = jwtService.generateToken(userDetails);
        
        // Perform logout
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
```

### Step 3: Run Tests

```bash
# Run tests
./mvnw test
```

## Phase 9: Documentation

### Step 1: Create Security Architecture Documentation

Create a new file `docs/security-architecture.md`:

```markdown
# Security Architecture

## Overview

The security architecture is built around JWT-based authentication with CSRF protection.
The system uses a stateless approach where each request must include a valid JWT token.

## Components

### Authentication Flow

1. User submits credentials to `/api/auth/login`
2. System validates credentials and generates JWT token
3. Token is returned to client and stored in localStorage
4. Client includes token in Authorization header for subsequent requests

### JWT Token Structure

- **Header**: Algorithm and token type
- **Payload**:
  - `sub`: Username
  - `exp`: Expiration time
  - `iat`: Issued at time
  - `type`: Token type (ACCESS, REFRESH, etc.)
  - `roles`: User roles
  - `clientId`: Client context (if applicable)
  - `fingerprint`: Token fingerprint for additional security
- **Signature**: HMAC-SHA256 signature

### CSRF Protection

CSRF protection is implemented using the double-submit cookie pattern:

1. Server generates CSRF token and sends it as cookie and in response body
2. Client stores token and includes it in X-XSRF-TOKEN header for non-GET requests
3. Server validates that header token matches cookie token

## Security Filters

Requests flow through the following security filters:

1. `SecurityHeadersFilter`: Adds security headers to responses
2. `JwtAuthenticationFilter`: Validates JWT tokens and sets authentication
3. `CsrfFilter`: Validates CSRF tokens for non-GET requests
4. `AuthorizationFilter`: Checks authorization for protected resources

## Rate Limiting

The system implements multi-level rate limiting:

1. IP-based rate limiting: 5 attempts per 5 minutes
2. Username-based rate limiting: 10 attempts per hour across all IPs
3. Progressive backoff: Increasing timeout after failed attempts

## Token Blacklisting

Revoked tokens are stored in a blacklist:

1. In development: In-memory ConcurrentHashMap with scheduled cleanup
2. In production: Redis-based blacklist with automatic expiration

## Security Headers

The application sets the following security headers:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: same-origin`
- `Permissions-Policy: camera=(), microphone=(), geolocation=()`
- `Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline';`
```

### Step 2: Create API Documentation

Create a new file `docs/api-documentation.md`:

```markdown
# API Documentation

## Authentication Endpoints

### Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "user@example.com",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user@example.com",
  "roles": ["ROLE_USER"],
  "csrfToken": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6"
}
```

### Refresh Token

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Validate Token

**Endpoint:** `GET /api/auth/validate`

**Headers:**
- `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

**Response:**
```json
{
  "valid": true,
  "username": "user@example.com",
  "roles": ["ROLE_USER"]
}
```

### Logout

**Endpoint:** `POST /api/auth/logout`

**Headers:**
- `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

**Response:**
```json
{
  "message": "Logged out successfully"
}
```

### Refresh CSRF Token

**Endpoint:** `POST /api/auth/refresh-csrf`

**Headers:**
- `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

**Response:**
```json
{
  "csrfToken": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6"
}
```

## Error Responses

All API endpoints return standardized error responses:

```json
{
  "code": "AUTH_001",
  "message": "Authentication failed",
  "detail": "Invalid username or password",
  "timestamp": "2023-03-26T12:34:56.789"
}
```

Common error codes:
- `AUTH_001`: Invalid credentials
- `AUTH_002`: Expired token
- `AUTH_003`: Invalid token
- `AUTH_004`: Insufficient privileges
- `VAL_001`: Missing field
- `VAL_002`: Invalid format
- `BUS_001`: Resource not found
- `SYS_001`: Unexpected error
```

## Conclusion

This implementation guide provides a comprehensive approach to enhancing the backend security of the MiddlewareAppV1.1 application. By following these steps, you will:

1. Improve JWT token security with enhanced claims, token types, and fingerprinting
2. Implement proper CSRF protection with secure cookie handling
3. Add comprehensive security headers to protect against common web vulnerabilities
4. Implement sophisticated rate limiting to prevent brute force attacks
5. Create a standardized error handling system for better client experience
6. Add health checks for monitoring system status
7. Provide thorough documentation of the security architecture

The implementation is designed to be compatible with the frontend changes already made, ensuring a seamless integration between frontend and backend security mechanisms.

Remember to test thoroughly after each phase of implementation to ensure that the changes do not break existing functionality.
