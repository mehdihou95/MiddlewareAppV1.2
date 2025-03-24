# Analysis of Client Loading and JWT Authentication Issues

## Client Loading Issues

After examining the client loading implementation in the frontend, I've identified several potential issues that could be causing problems with loading clients:

### 1. Inconsistent API URL Configuration

In the frontend code, there are inconsistencies in how the API URL is configured:

- In `clientService.ts`, the API URL is set as:
  ```typescript
  const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
  ```

- In `AuthContext.tsx`, the API URL is modified:
  ```typescript
  axios.defaults.baseURL = API_URL.replace(/\/api$/, ''); // Remove /api if present
  ```

This inconsistency could lead to requests being sent to incorrect endpoints.

### 2. Token Management Issues

The client loading depends on proper authentication. The current implementation has some issues:

- In `tokenManager.ts`, tokens are stored in `sessionStorage`, which means they're lost when the browser is closed.
- The token expiry validation in `isTokenValid()` has a potential issue:
  ```typescript
  if (!expiry) {
    return true; // If no expiry is set, assume token is valid
  }
  ```
  This assumes tokens without expiry are valid, which could lead to using expired tokens.

### 3. Error Handling in ClientInterfaceContext

The error handling in `ClientInterfaceContext.tsx` clears all client data on any error:

```typescript
catch (err: any) {
  const errorMessage = err.response?.data?.message || err.message || 'Failed to load clients';
  setError(`Failed to load clients: ${errorMessage}`);
  console.error('Error loading clients:', err);
  // Clear selections on error
  setClients([]);
  setSelectedClient(null);
  setSelectedInterface(null);
  localStorage.removeItem('selectedClientId');
  localStorage.removeItem('selectedInterfaceId');
}
```

This aggressive error handling might be clearing client data unnecessarily.

### 4. Axios Interceptor Configuration

There are multiple axios interceptor configurations across different files:
- In `clientService.ts`
- In `authService.ts`
- In `AuthContext.tsx`

This could lead to conflicts or unexpected behavior in request handling.

## JWT Authentication Issues

The JWT authentication implementation has several potential issues:

### 1. Refresh Token Implementation Inconsistency

There's an inconsistency between frontend and backend refresh token implementations:

- In the frontend (`authService.ts`), the refresh token is sent in the request body:
  ```typescript
  const response = await axios.post<RefreshTokenResponse>(`${API_URL}/auth/refresh`, {
    refreshToken
  });
  ```

- In the backend (`AuthController.java`), the refresh endpoint expects a `RefreshTokenRequest` object:
  ```java
  @PostMapping("/refresh")
  public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest request) {
  ```

But the response from the backend doesn't include a new refresh token:
```java
return ResponseEntity.ok(Map.of(
    "token", newToken,
    "username", userDetails.getUsername()
));
```

### 2. CSRF Protection Issues

The backend has CSRF protection enabled:
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/api/auth/**", "/h2-console/**")
)
```

But the frontend doesn't consistently handle CSRF tokens. Only `authService.ts` has CSRF token handling:
```typescript
// Add request interceptor to include CSRF token
api.interceptors.request.use((config) => {
    const token = document.cookie
        .split('; ')
        .find(row => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];
    
    if (token) {
        (config.headers as Record<string, string>)['X-XSRF-TOKEN'] = token;
    }
    
    return config;
});
```

### 3. Multiple Authentication Interceptors

There are two separate token refresh interceptors:
- One in `authService.ts`
- Another in `AuthContext.tsx`

This could lead to race conditions or duplicate refresh attempts.

### 4. Missing Validation Endpoint

The frontend tries to validate tokens using:
```typescript
const response = await axios.get(`${API_URL}/auth/validate`);
```

But there's no corresponding `/auth/validate` endpoint in the `AuthController.java`.

## Solutions

### Client Loading Issues

1. **Standardize API URL Configuration**:
   ```typescript
   // In a new file: src/config/apiConfig.ts
   export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
   export const API_URL = `${API_BASE_URL}/api`;
   ```
   Then import this in all services.

2. **Improve Token Management**:
   - Store tokens in `localStorage` instead of `sessionStorage` for persistence
   - Always check token expiration properly:
   ```typescript
   isTokenValid: (): boolean => {
     const token = localStorage.getItem(ACCESS_TOKEN_KEY);
     if (!token) return false;
     
     try {
       // Decode JWT to check expiration
       const payload = JSON.parse(atob(token.split('.')[1]));
       return payload.exp * 1000 > Date.now();
     } catch (e) {
       return false;
     }
   }
   ```

3. **Refine Error Handling**:
   - Only clear client data on authentication errors (401/403)
   - For other errors, keep existing data and show error message

4. **Centralize Axios Configuration**:
   - Create a single axios instance with all interceptors
   - Export and use this instance in all services

### JWT Authentication Issues

1. **Fix Refresh Token Implementation**:
   - Update backend to return both new access and refresh tokens:
   ```java
   return ResponseEntity.ok(Map.of(
       "token", newToken,
       "refreshToken", jwtService.generateRefreshToken(userDetails),
       "username", userDetails.getUsername()
   ));
   ```

2. **Implement CSRF Protection Consistently**:
   - Create a centralized axios instance with CSRF handling
   - Use this instance in all services

3. **Consolidate Authentication Interceptors**:
   - Move all authentication logic to a single service
   - Remove duplicate interceptors

4. **Add Validation Endpoint**:
   - Implement the missing endpoint in `AuthController.java`:
   ```java
   @GetMapping("/validate")
   public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
       if (authentication != null && authentication.isAuthenticated()) {
           UserDetails userDetails = (UserDetails) authentication.getPrincipal();
           return ResponseEntity.ok(Map.of(
               "valid", true,
               "username", userDetails.getUsername(),
               "roles", userDetails.getAuthorities().stream()
                   .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                   .collect(Collectors.toList())
           ));
       }
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
   }
   ```

## Implementation Plan

1. **Backend Changes**:
   - Update `AuthController.java` to include refresh token in response
   - Add validation endpoint
   - Ensure consistent error responses

2. **Frontend Changes**:
   - Create centralized API configuration
   - Improve token management
   - Consolidate axios interceptors
   - Refine error handling in client loading

These changes should resolve both the client loading issues and JWT authentication problems.
