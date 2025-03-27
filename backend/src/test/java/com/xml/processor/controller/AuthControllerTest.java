package com.xml.processor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xml.processor.dto.LoginRequest;
import com.xml.processor.security.RateLimiter;
import com.xml.processor.security.service.CsrfTokenService;
import com.xml.processor.security.service.JwtService;
import com.xml.processor.security.service.impl.InMemoryJwtBlacklistService;
import com.xml.processor.service.impl.SecurityLoggerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTestConfig.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private InMemoryJwtBlacklistService jwtBlacklistService;

    @MockBean
    private SecurityLoggerServiceImpl securityLoggerService;

    @MockBean
    private CsrfTokenService csrfTokenService;

    private UserDetails userDetails;
    private String testToken;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userDetails = new User("testuser", "testpass", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        testToken = "test-token";
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(testToken);
        when(jwtService.isTokenValid(any(), any())).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(rateLimiter.checkRateLimit(any())).thenReturn(true);
        when(jwtBlacklistService.isBlacklisted(any())).thenReturn(false);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testFailedLogin() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTokenValidation() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void testRefreshToken() throws Exception {
        String newToken = "new-test-token";
        when(jwtService.generateToken(userDetails)).thenReturn(newToken);

        mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"" + testToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newToken));
    }

    @Test
    void testRateLimitExceeded() throws Exception {
        when(rateLimiter.checkRateLimit(any())).thenReturn(false);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests());
    }
} 