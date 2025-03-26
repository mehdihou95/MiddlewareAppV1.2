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