package com.xml.processor.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsrfTokenService {

    private final CsrfTokenRepository csrfTokenRepository;

    public CsrfToken generateToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken token = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(token, request, response);
        log.debug("Generated new CSRF token");
        return token;
    }

    public boolean validateToken(HttpServletRequest request, String token) {
        CsrfToken storedToken = csrfTokenRepository.loadToken(request);
        if (storedToken == null) {
            log.warn("No CSRF token found in repository");
            return false;
        }
        
        boolean valid = storedToken.getToken().equals(token);
        if (!valid) {
            log.warn("CSRF token validation failed");
        }
        return valid;
    }

    public void clearToken(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.saveToken(null, request, response);
        log.debug("Cleared CSRF token");
    }
} 