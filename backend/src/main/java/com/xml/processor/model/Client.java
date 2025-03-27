package com.xml.processor.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a client.
 */
@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z0-9-_]+$", message = "Code must contain only letters, numbers, hyphens, and underscores")
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String code;

    @NotNull(message = "Status is required")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClientStatus status = ClientStatus.ACTIVE;

    @Size(max = 1000)
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Interface> interfaces = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MappingRule> mappingRules = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Interface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<Interface> interfaces) {
        this.interfaces = interfaces;
    }

    public Set<MappingRule> getMappingRules() {
        return mappingRules;
    }

    public void setMappingRules(Set<MappingRule> mappingRules) {
        this.mappingRules = mappingRules;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setActive(boolean active) {
        this.status = active ? ClientStatus.ACTIVE : ClientStatus.INACTIVE;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdAt = createdDate;
    }
} 