package com.xml.processor.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwtBlacklistService {
    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklistService.class);
    private final ConcurrentMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public void blacklistToken(String token) {
        blacklistedTokens.put(token, Instant.now());
        logger.info("Token blacklisted. Current blacklist size: {}", blacklistedTokens.size());
    }

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
        logger.info("Starting scheduled cleanup of blacklisted tokens. Current size: {}", blacklistedTokens.size());
        Instant now = Instant.now();
        int initialSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry ->
            now.isAfter(entry.getValue().plusMillis(jwtExpiration)));
        
        int removedCount = initialSize - blacklistedTokens.size();
        logger.info("Completed blacklist cleanup. Removed {} expired tokens. New size: {}", 
            removedCount, blacklistedTokens.size());
    }
} 