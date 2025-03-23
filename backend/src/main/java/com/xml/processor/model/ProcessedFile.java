package com.xml.processor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "processed_files")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessedFile extends BaseEntity {
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String status;
    
    @Column(length = 1000)
    private String errorMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_id")
    private Interface interfaceEntity;
    
    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonAttributeConverter.class)
    private Map<String, Object> processedData;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        processedAt = LocalDateTime.now();
    }
} 