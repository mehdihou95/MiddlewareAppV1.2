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