package com.xml.processor.model;
    
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
    
import java.util.HashSet;
import java.util.Set;
    
@Entity
@Table(name = "interfaces")
@Getter
@Setter
@NoArgsConstructor
public class Interface extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type; // ASN, ORDER, INVOICE, etc.
    
    @Column(length = 500)
    private String description;
    
    @Column
    private String schemaPath; // Path to the XSD schema
    
    @Column
    private String rootElement; // Root element name for detection
    
    @Column
    private String namespace; // XML namespace
    
    @Column
    private Boolean isActive = true;
    
    @Column
    private Integer priority = 0; // For processing order
    
    @OneToMany(mappedBy = "interfaceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MappingRule> mappingRules = new HashSet<>();
} 