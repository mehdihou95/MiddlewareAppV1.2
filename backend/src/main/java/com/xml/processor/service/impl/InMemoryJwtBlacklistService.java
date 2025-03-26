package com.xml.processor.service.impl;

import com.xml.processor.security.service.JwtService;
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
public class InMemoryJwtBlacklistService implements JwtBlacklistService {
    
    private final ConcurrentMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private final JwtService jwtService;
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    
    public InMemoryJwtBlacklistService(JwtService jwtService) {
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