# Cursor Rules

## Spring Security Form Login with React

### Problem
When integrating React frontend with Spring Security's form-based authentication, using complex state management (like AuthContext with axios) can lead to authentication failures where the server receives empty credentials, even though the form appears to have data.

### Solution
Use the native `fetch` API with proper form data serialization:
```typescript
const response = await fetch('/login', {
  method: 'POST',
  body: new URLSearchParams(formData),
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
  credentials: 'include'
});
```

### Key Points
1. Spring Security expects form data in `application/x-www-form-urlencoded` format
2. Use `URLSearchParams` to properly serialize form data
3. Include `credentials: 'include'` for proper session management
4. Keep the authentication flow simple and match Spring Security's expectations
5. Check server logs for "Failed to find user ''" as an indicator of improper form data serialization

### Evidence from Logs
```
Before fix:
DEBUG o.s.s.a.dao.DaoAuthenticationProvider : Failed to find user ''

After fix:
DEBUG o.s.s.a.dao.DaoAuthenticationProvider : Authenticated user
DEBUG w.a.UsernamePasswordAuthenticationFilter : Set SecurityContextHolder to UsernamePasswordAuthenticationToken
```

### Best Practices
1. Match Spring Security's expected format exactly
2. Use simpler, direct approaches over complex state management when possible
3. Monitor backend logs during authentication attempts
4. Test with network tab open to verify request format
5. Ensure proper CORS and session handling configuration 