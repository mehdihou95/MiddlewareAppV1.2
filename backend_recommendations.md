# Backend Enhancement Recommendations

Based on the identified issues in the MiddlewareAppV1.1 backend, I've prepared comprehensive recommendations to enhance the authentication, CSRF protection, and error handling mechanisms. These recommendations are designed to improve security, maintainability, and scalability while ensuring compatibility with the updated frontend.

## 1. Authentication Enhancements

### 1.1 JWT Token Implementation

#### Recommendations:

1. **Enhance JWT Payload**
   ```java
   private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
       // Add standard claims
       Map<String, Object> claims = new HashMap<>(extraClaims);
       claims.put("roles", userDetails.getAuthorities().stream()
           .map(GrantedAuthority::getAuthority)
           .collect(Collectors.toList()));
       
       // Add client context if available
       Long clientId = ClientContextHolder.getClientId();
       if (clientId != null) {
           claims.put("clientId", clientId);
       }
       
       // Add token fingerprint
       claims.put("fingerprint", generateFingerprint(userDetails));
       
       return Jwts.builder()
           .setClaims(claims)
           .setSubject(userDetails.getUsername())
           .setIssuedAt(new Date(System.currentTimeMillis()))
           .setExpiration(new Date(System.currentTimeMillis() + expiration))
           .signWith(getSignInKey(), SignatureAlgorithm.HS256)
           .compact();
   }
   
   private String generateFingerprint(UserDetails userDetails) {
       // Generate a unique fingerprint based on user agent, IP, etc.
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String userAgent = request.getHeader("User-Agent");
       String ipAddress = request.getRemoteAddr();
       return DigestUtils.sha256Hex(userDetails.getUsername() + ":" + userAgent + ":" + ipAddress);
   }
   ```

2. **Implement Different Token Types**
   ```java
   public enum TokenType {
       ACCESS,
       REFRESH,
       RESET_PASSWORD,
       EMAIL_VERIFICATION
   }
   
   public String generateToken(UserDetails userDetails, TokenType tokenType) {
       Map<String, Object> claims = new HashMap<>();
       claims.put("type", tokenType.name());
       
       long expiration = switch(tokenType) {
           case ACCESS -> jwtExpiration;
           case REFRESH -> refreshExpiration;
           case RESET_PASSWORD -> passwordResetExpiration;
           case EMAIL_VERIFICATION -> emailVerificationExpiration;
       };
       
       return buildToken(claims, userDetails, expiration);
   }
   ```

3. **Enhance Token Validation**
   ```java
   public boolean isTokenValid(String token, UserDetails userDetails) {
       try {
           final Claims claims = extractAllClaims(token);
           final String username = claims.getSubject();
           final Date expiration = claims.getExpiration();
           final String tokenType = claims.get("type", String.class);
           
           // Validate basic properties
           if (!username.equals(userDetails.getUsername()) || expiration.before(new Date())) {
               return false;
           }
           
           // Validate token type
           if (TokenType.ACCESS.name().equals(tokenType)) {
               // For access tokens, validate fingerprint if present
               if (claims.containsKey("fingerprint")) {
                   String expectedFingerprint = generateFingerprint(userDetails);
                   String tokenFingerprint = claims.get("fingerprint", String.class);
                   if (!expectedFingerprint.equals(tokenFingerprint)) {
                       logger.warn("Token fingerprint mismatch for user: {}", username);
                       return false;
                   }
               }
           }
           
           return true;
       } catch (Exception e) {
           logger.error("Error validating token: {}", e.getMessage());
           return false;
       }
   }
   ```

### 1.2 Authentication Flow

#### Recommendations:

1. **Fix Form-Based Login**
   ```java
   @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
   public ResponseEntity<Map<String, Object>> formLogin(
           @RequestParam("username") String username,
           @RequestParam("password") String password,
           HttpServletRequest httpRequest,
           HttpServletResponse response) {
       // Ensure we always have a valid request
       if (httpRequest == null) {
           return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
       }
       
       LoginRequest loginRequest = new LoginRequest(username, password);
       return login(loginRequest, httpRequest, response);
   }
   ```

2. **Enhance Rate Limiting**
   ```java
   @Component
   public class EnhancedRateLimiter {
       private static final Logger logger = LoggerFactory.getLogger(EnhancedRateLimiter.class);
       
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
       
       // Implementation details...
   }
   ```

3. **Implement Remember-Me Functionality**
   ```java
   @PostMapping("/login")
   public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, 
                                                  HttpServletRequest servletRequest, 
                                                  HttpServletResponse response) {
       // Existing authentication logic...
       
       // Handle remember-me
       if (request.isRememberMe()) {
           String rememberMeToken = jwtService.generateToken(userDetails, TokenType.REMEMBER_ME);
           Cookie rememberMeCookie = new Cookie("remember_me", rememberMeToken);
           rememberMeCookie.setPath("/");
           rememberMeCookie.setHttpOnly(true);
           rememberMeCookie.setSecure(servletRequest.isSecure());
           rememberMeCookie.setMaxAge(2592000); // 30 days
           response.addCookie(rememberMeCookie);
       }
       
       // Rest of the method...
   }
   ```

### 1.3 Token Blacklisting

#### Recommendations:

1. **Implement Redis-Based Token Blacklist**
   ```java
   @Service
   public class RedisJwtBlacklistService {
       private static final Logger logger = LoggerFactory.getLogger(RedisJwtBlacklistService.class);
       
       private final StringRedisTemplate redisTemplate;
       private final JwtService jwtService;
       
       public RedisJwtBlacklistService(StringRedisTemplate redisTemplate, JwtService jwtService) {
           this.redisTemplate = redisTemplate;
           this.jwtService = jwtService;
       }
       
       public void blacklistToken(String token) {
           try {
               // Extract expiration time
               Date expiration = jwtService.extractExpiration(token);
               long ttl = expiration.getTime() - System.currentTimeMillis();
               
               if (ttl > 0) {
                   // Store token hash in Redis with TTL
                   String tokenHash = DigestUtils.sha256Hex(token);
                   redisTemplate.opsForValue().set("blacklist:" + tokenHash, "1", ttl, TimeUnit.MILLISECONDS);
                   logger.info("Token blacklisted until {}", expiration);
               } else {
                   logger.info("Token already expired, no need to blacklist");
               }
           } catch (Exception e) {
               logger.error("Error blacklisting token: {}", e.getMessage());
           }
       }
       
       public boolean isBlacklisted(String token) {
           try {
               String tokenHash = DigestUtils.sha256Hex(token);
               Boolean exists = redisTemplate.hasKey("blacklist:" + tokenHash);
               return Boolean.TRUE.equals(exists);
           } catch (Exception e) {
               logger.error("Error checking blacklisted token: {}", e.getMessage());
               return false;
           }
       }
   }
   ```

2. **Add Redis Configuration**
   ```java
   @Configuration
   @EnableCaching
   public class RedisConfig {
       
       @Value("${spring.redis.host:localhost}")
       private String redisHost;
       
       @Value("${spring.redis.port:6379}")
       private int redisPort;
       
       @Bean
       public LettuceConnectionFactory redisConnectionFactory() {
           RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
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

3. **Fallback to In-Memory for Development**
   ```java
   @Service
   @ConditionalOnProperty(name = "app.security.token-blacklist", havingValue = "in-memory", matchIfMissing = true)
   public class InMemoryJwtBlacklistService implements JwtBlacklistService {
       // Existing implementation...
   }
   
   @Service
   @ConditionalOnProperty(name = "app.security.token-blacklist", havingValue = "redis")
   public class RedisJwtBlacklistService implements JwtBlacklistService {
       // Redis implementation...
   }
   ```

## 2. CSRF Protection Enhancements

### 2.1 CSRF Configuration

#### Recommendations:

1. **Centralize CSRF Configuration**
   ```java
   @Configuration
   public class CsrfConfig {
       private static final Logger logger = LoggerFactory.getLogger(CsrfConfig.class);
       
       @Bean
       public CsrfTokenRepository csrfTokenRepository() {
           CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
           repository.setCookiePath("/");
           repository.setCookieName("XSRF-TOKEN");
           repository.setHeaderName("X-XSRF-TOKEN");
           return repository;
       }
       
       @Bean
       public CsrfTokenRequestAttributeHandler csrfTokenRequestHandler() {
           return new CsrfTokenRequestAttributeHandler();
       }
       
       @Bean
       public WebFilter csrfCookieFilter() {
           return (exchange, chain) -> {
               ServerHttpRequest request = exchange.getRequest();
               ServerHttpResponse response = exchange.getResponse();
               
               CsrfToken csrfToken = exchange.getAttribute(CsrfToken.class.getName());
               if (csrfToken != null) {
                   response.getHeaders().add(csrfToken.getHeaderName(), csrfToken.getToken());
               }
               
               return chain.filter(exchange);
           };
       }
   }
   ```

2. **Update Security Configuration**
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
           );
       
       // Rest of the configuration...
       
       return http.build();
   }
   ```

3. **Secure Logging**
   ```java
   @Component
   public class SecureCsrfLogger extends OncePerRequestFilter {
       private static final Logger logger = LoggerFactory.getLogger(SecureCsrfLogger.class);
       
       @Override
       protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
               throws ServletException, IOException {
           // Only log presence, not actual values
           String csrfHeader = request.getHeader("X-XSRF-TOKEN");
           boolean hasCsrfCookie = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
               .anyMatch(cookie -> "XSRF-TOKEN".equals(cookie.getName()));
               
           logger.debug("CSRF Debug - URI: {}, Method: {}, Has Header: {}, Has Cookie: {}", 
               request.getRequestURI(),
               request.getMethod(),
               csrfHeader != null,
               hasCsrfCookie);
               
           filterChain.doFilter(request, response);
       }
   }
   ```

### 2.2 CSRF Token Handling

#### Recommendations:

1. **Add SameSite Attribute**
   ```java
   @Bean
   public CsrfTokenRepository csrfTokenRepository() {
       CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository();
       repository.setCookiePath("/");
       repository.setCookieName("XSRF-TOKEN");
       repository.setHeaderName("X-XSRF-TOKEN");
       repository.setCookieHttpOnly(false);
       repository.setCookieDomain(null); // Use default domain
       
       // Add SameSite attribute
       repository.setCookieCustomizer(cookie -> {
           cookie.setAttribute("SameSite", "Lax");
       });
       
       return repository;
   }
   ```

2. **Implement Token Rotation**
   ```java
   @Service
   public class CsrfTokenService {
       private final CsrfTokenRepository csrfTokenRepository;
       
       public CsrfTokenService(CsrfTokenRepository csrfTokenRepository) {
           this.csrfTokenRepository = csrfTokenRepository;
       }
       
       public CsrfToken generateToken(HttpServletRequest request, HttpServletResponse response) {
           CsrfToken token = csrfTokenRepository.generateToken(request);
           csrfTokenRepository.saveToken(token, request, response);
           return token;
       }
       
       public void rotateToken(HttpServletRequest request, HttpServletResponse response) {
           // Generate and save a new token
           generateToken(request, response);
       }
   }
   ```

3. **Enhance CSRF Endpoint**
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
       
       // Set secure cookie attributes
       Cookie cookie = new Cookie("XSRF-TOKEN", token.getToken());
       cookie.setPath("/");
       cookie.setSecure(request.isSecure());
       cookie.setHttpOnly(false);
       cookie.setMaxAge(3600); // 1 hour
       
       // Add SameSite attribute
       response.setHeader("Set-Cookie", 
           String.format("%s=%s; Path=%s; Max-Age=%d; %sHttpOnly=%s; SameSite=%s", 
               "XSRF-TOKEN", 
               token.getToken(), 
               "/", 
               3600, 
               request.isSecure() ? "Secure; " : "", 
               "false", 
               "Lax"));
       
       response.setHeader(token.getHeaderName(), token.getToken());

       return ResponseEntity.ok(Map.of("csrfToken", token.getToken()));
   }
   ```

## 3. Error Handling Enhancements

### 3.1 Error Response Structure

#### Recommendations:

1. **Standardize Error Response Class**
   ```java
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
               .fieldErrors(fieldErrors)
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

2. **Update Global Exception Handler**
   ```java
   @RestControllerAdvice
   public class GlobalExceptionHandler {
       
       @ExceptionHandler(ValidationException.class)
       public ResponseEntity<StandardErrorResponse> handleValidationException(ValidationException ex) {
           Map<String, String> fieldErrors = ex.getFieldErrors();
           StandardErrorResponse response = StandardErrorResponse.validationError(
               "Validation failed", 
               fieldErrors
           );
           return ResponseEntity.badRequest().body(response);
       }
       
       @ExceptionHandler(AuthenticationException.class)
       public ResponseEntity<StandardErrorResponse> handleAuthenticationException(AuthenticationException ex) {
           StandardErrorResponse response = StandardErrorResponse.authenticationError(
               "Authentication failed: " + ex.getMessage()
           );
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
       }
       
       // Other exception handlers...
   }
   ```

3. **Add Error Code Constants**
   ```java
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

### 3.2 Exception Handling

#### Recommendations:

1. **Add Specific Exception Handlers**
   ```java
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
   ```

2. **Implement Hierarchical Exception Handling**
   ```java
   // Base application exception
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
   
   // Authentication exceptions
   public class AuthenticationFailedException extends ApplicationException {
       public AuthenticationFailedException(String message) {
           super(ErrorCodes.AUTH_INVALID_CREDENTIALS, message);
       }
   }
   
   // Business exceptions
   public class ResourceNotFoundException extends ApplicationException {
       public ResourceNotFoundException(String resourceType, Object id) {
           super(ErrorCodes.BUS_RESOURCE_NOT_FOUND, 
                 String.format("%s with id %s not found", resourceType, id));
       }
   }
   
   // Handler for application exceptions
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
   ```

3. **Add Validation Exception Handler**
   ```java
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
   ```

## 4. Security Configuration Enhancements

### 4.1 Security Headers

#### Recommendations:

1. **Add Comprehensive Security Headers**
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

2. **Create Custom Security Headers Filter**
   ```java
   @Component
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

### 4.2 Authentication Provider

#### Recommendations:

1. **Implement Multi-Factor Authentication Support**
   ```java
   @Service
   public class MfaService {
       private final UserRepository userRepository;
       private final TotpService totpService;
       
       public MfaService(UserRepository userRepository, TotpService totpService) {
           this.userRepository = userRepository;
           this.totpService = totpService;
       }
       
       public boolean verifyMfaCode(String username, String code) {
           User user = userRepository.findByUsername(username)
               .orElseThrow(() -> new UsernameNotFoundException("User not found"));
               
           if (!user.isMfaEnabled()) {
               return true; // MFA not required
           }
           
           return totpService.verifyCode(user.getMfaSecret(), code);
       }
       
       public String generateMfaSecret() {
           return totpService.generateSecret();
       }
       
       public String generateQrCodeUrl(String username, String secret) {
           return totpService.generateQrCodeUrl("MyApp", username, secret);
       }
   }
   ```

2. **Add Step-Up Authentication**
   ```java
   @Service
   public class StepUpAuthService {
       private final JwtService jwtService;
       
       public StepUpAuthService(JwtService jwtService) {
           this.jwtService = jwtService;
       }
       
       public String generateStepUpToken(Authentication authentication, String operation) {
           UserDetails userDetails = (UserDetails) authentication.getPrincipal();
           
           Map<String, Object> claims = new HashMap<>();
           claims.put("type", "STEP_UP");
           claims.put("operation", operation);
           
           return jwtService.buildToken(claims, userDetails, 300000); // 5 minutes
       }
       
       public boolean validateStepUpToken(String token, String operation) {
           try {
               Claims claims = jwtService.extractAllClaims(token);
               
               if (!"STEP_UP".equals(claims.get("type"))) {
                   return false;
               }
               
               if (!operation.equals(claims.get("operation"))) {
                   return false;
               }
               
               return !jwtService.isTokenExpired(token);
           } catch (Exception e) {
               return false;
           }
       }
   }
   ```

3. **Implement Role-Based Security**
   ```java
   @Configuration
   @EnableMethodSecurity
   public class MethodSecurityConfig {
       
       @Bean
       public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
           DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
           expressionHandler.setPermissionEvaluator(new CustomPermissionEvaluator());
           return expressionHandler;
       }
   }
   
   public class CustomPermissionEvaluator implements PermissionEvaluator {
       
       @Override
       public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
           if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
               return false;
           }
           
           String targetType = targetDomainObject.getClass().getSimpleName().toLowerCase();
           return hasPrivilege(authentication, targetType, permission.toString().toLowerCase());
       }
       
       @Override
       public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
           if (authentication == null || targetType == null || !(permission instanceof String)) {
               return false;
           }
           
           return hasPrivilege(authentication, targetType.toLowerCase(), permission.toString().toLowerCase());
       }
       
       private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
           // Implementation details...
       }
   }
   ```

## 5. Performance and Scalability Enhancements

### 5.1 Distributed Data Structures

#### Recommendations:

1. **Implement Redis-Based Rate Limiting**
   ```java
   @Service
   public class RedisRateLimiter {
       private final StringRedisTemplate redisTemplate;
       private final RedisScript<Long> rateLimiterScript;
       
       public RedisRateLimiter(StringRedisTemplate redisTemplate) {
           this.redisTemplate = redisTemplate;
           
           // Load Lua script for atomic rate limiting
           DefaultRedisScript<Long> script = new DefaultRedisScript<>();
           script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate-limiter.lua")));
           script.setResultType(Long.class);
           this.rateLimiterScript = script;
       }
       
       public boolean checkRateLimit(String key, int maxRequests, int windowSeconds) {
           List<String> keys = Collections.singletonList("rate-limit:" + key);
           Long count = redisTemplate.execute(
               rateLimiterScript,
               keys,
               String.valueOf(maxRequests),
               String.valueOf(windowSeconds)
           );
           
           return count != null && count <= maxRequests;
       }
   }
   ```

2. **Implement Distributed Session Management**
   ```java
   @Configuration
   @EnableRedisHttpSession
   public class SessionConfig {
       
       @Bean
       public RedisOperationsSessionRepository sessionRepository(RedisConnectionFactory connectionFactory) {
           RedisOperationsSessionRepository repository = new RedisOperationsSessionRepository(connectionFactory);
           repository.setDefaultMaxInactiveInterval(1800); // 30 minutes
           return repository;
       }
       
       @Bean
       public HttpSessionIdResolver httpSessionIdResolver() {
           return HeaderHttpSessionIdResolver.xAuthToken();
       }
   }
   ```

### 5.2 Logging and Monitoring

#### Recommendations:

1. **Implement Structured Logging**
   ```java
   @Aspect
   @Component
   public class LoggingAspect {
       private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
       
       @Around("execution(* com.xml.processor.controller.*.*(..))")
       public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
           long startTime = System.currentTimeMillis();
           String className = joinPoint.getSignature().getDeclaringTypeName();
           String methodName = joinPoint.getSignature().getName();
           
           // Create structured log entry
           Map<String, Object> logEntry = new HashMap<>();
           logEntry.put("type", "controller");
           logEntry.put("class", className);
           logEntry.put("method", methodName);
           
           try {
               // Execute method
               Object result = joinPoint.proceed();
               
               // Log success
               long executionTime = System.currentTimeMillis() - startTime;
               logEntry.put("status", "success");
               logEntry.put("executionTime", executionTime);
               logger.info("Controller execution: {}", logEntry);
               
               return result;
           } catch (Exception e) {
               // Log failure
               long executionTime = System.currentTimeMillis() - startTime;
               logEntry.put("status", "error");
               logEntry.put("executionTime", executionTime);
               logEntry.put("exception", e.getClass().getName());
               logEntry.put("message", e.getMessage());
               logger.error("Controller execution failed: {}", logEntry);
               
               throw e;
           }
       }
   }
   ```

2. **Add Metrics Collection**
   ```java
   @Configuration
   @EnablePrometheusMetrics
   public class MetricsConfig {
       
       @Bean
       public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
           return registry -> registry.config().commonTags("application", "xml-processor");
       }
       
       @Bean
       public TimedAspect timedAspect(MeterRegistry registry) {
           return new TimedAspect(registry);
       }
   }
   
   // Usage in controllers
   @RestController
   @RequestMapping("/api/auth")
   public class AuthController {
       
       @Timed(value = "login.time", description = "Time taken to process login")
       @PostMapping("/login")
       public ResponseEntity<Map<String, Object>> login(...) {
           // Method implementation
       }
   }
   ```

3. **Implement Health Checks**
   ```java
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
               jwtService.validateTestToken(testToken);
               
               return Health.up()
                   .withDetail("jwtService", "operational")
                   .build();
           } catch (Exception e) {
               return Health.down()
                   .withDetail("jwtService", "failed")
                   .withDetail("error", e.getMessage())
                   .build();
           }
       }
   }
   ```

## 6. Code Quality and Maintainability Enhancements

### 6.1 Code Organization

#### Recommendations:

1. **Reorganize Security Classes**
   ```
   com.xml.processor.security/
   ├── config/
   │   ├── SecurityConfig.java
   │   ├── CsrfConfig.java
   │   ├── CorsConfig.java
   │   └── MethodSecurityConfig.java
   ├── filter/
   │   ├── JwtAuthenticationFilter.java
   │   ├── SecurityHeadersFilter.java
   │   └── CsrfTokenFilter.java
   ├── service/
   │   ├── JwtService.java
   │   ├── TokenBlacklistService.java
   │   ├── RateLimitService.java
   │   └── MfaService.java
   ├── model/
   │   ├── TokenType.java
   │   ├── SecurityUser.java
   │   └── AuthenticationRequest.java
   └── util/
       ├── SecurityUtils.java
       └── TokenGenerator.java
   ```

2. **Extract Inner Classes**
   ```java
   // Before: Inner class in SecurityConfig.java
   class CsrfTokenLoggingFilter extends OncePerRequestFilter {
       // Implementation...
   }
   
   // After: Separate class file
   @Component
   public class CsrfTokenLoggingFilter extends OncePerRequestFilter {
       private static final Logger logger = LoggerFactory.getLogger(CsrfTokenLoggingFilter.class);
       
       @Override
       protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
               throws ServletException, IOException {
           // Implementation...
       }
   }
   ```

3. **Create Interface Hierarchy**
   ```java
   // Base token service interface
   public interface TokenService {
       String generateToken(UserDetails userDetails);
       boolean validateToken(String token, UserDetails userDetails);
       String extractUsername(String token);
   }
   
   // JWT implementation
   @Service
   public class JwtTokenService implements TokenService {
       // Implementation...
   }
   
   // For testing or alternative implementations
   @Service
   @Profile("test")
   public class SimpleTokenService implements TokenService {
       // Implementation...
   }
   ```

### 6.2 Documentation and Comments

#### Recommendations:

1. **Add Comprehensive JavaDoc**
   ```java
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
   @Service
   public class JwtService {
       
       /**
        * Generates a JWT token for the specified user.
        * 
        * @param userDetails the user details for whom to generate the token
        * @return a JWT token string
        */
       public String generateToken(UserDetails userDetails) {
           // Implementation...
       }
       
       // Other methods...
   }
   ```

2. **Create Architecture Documentation**
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
   ```

3. **Add Code Comments for Complex Logic**
   ```java
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
                       logger.warn("Token fingerprint mismatch for user: {}", username);
                       return false;
                   }
               }
           }
           
           return true;
       } catch (Exception e) {
           logger.error("Error validating token: {}", e.getMessage());
           return false;
       }
   }
   ```

## Implementation Dependencies

To implement these recommendations, add the following dependencies to your pom.xml:

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

## Conclusion

These recommendations provide a comprehensive approach to enhancing the backend authentication, CSRF protection, and error handling mechanisms in the MiddlewareAppV1.1 application. By implementing these changes, you will:

1. Strengthen security through improved JWT token handling, CSRF protection, and authentication flows
2. Enhance scalability with distributed data structures for token blacklisting and rate limiting
3. Improve error handling with standardized responses and comprehensive exception handling
4. Increase maintainability through better code organization and documentation
5. Enable monitoring and observability with structured logging and metrics

The recommendations are designed to be compatible with the frontend changes already implemented, ensuring a seamless integration between frontend and backend security mechanisms.
