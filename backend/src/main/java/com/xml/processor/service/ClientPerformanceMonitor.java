package com.xml.processor.service;

import com.xml.processor.service.interfaces.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClientPerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ClientPerformanceMonitor.class);
    private final ClientService clientService;
    private final Map<Long, Instant> operationStartTimes = new HashMap<>();
    private final Map<Long, Map<String, Long>> clientMetrics = new HashMap<>();

    public ClientPerformanceMonitor(ClientService clientService) {
        this.clientService = clientService;
    }

    public void startOperation(Long clientId, String operationType) {
        operationStartTimes.put(clientId, Instant.now());
        logger.debug("Starting {} operation for client {}", operationType, clientId);
    }

    public void endOperation(Long clientId, String operationType) {
        Instant startTime = operationStartTimes.remove(clientId);
        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            recordMetric(clientId, operationType, duration.toMillis());
            logger.info("Client {} completed {} operation in {} ms", clientId, operationType, duration.toMillis());
        }
    }

    private void recordMetric(Long clientId, String operationType, long durationMs) {
        clientMetrics.computeIfAbsent(clientId, k -> new HashMap<>())
                    .merge(operationType, durationMs, (old, current) -> (old + current) / 2);
        
        // Log performance metrics
        Map<String, Long> metrics = clientMetrics.get(clientId);
        logger.info("Performance metrics for client {}: {}", clientId, metrics);
        
        // Check for performance thresholds
        if (durationMs > 5000) { // 5 seconds threshold
            logger.warn("Performance warning: Client {} operation {} took {} ms", clientId, operationType, durationMs);
        }
    }

    public Map<String, Long> getClientMetrics(Long clientId) {
        return clientMetrics.getOrDefault(clientId, new HashMap<>());
    }

    public void clearMetrics(Long clientId) {
        clientMetrics.remove(clientId);
        operationStartTimes.remove(clientId);
        logger.debug("Cleared performance metrics for client {}", clientId);
    }
} 