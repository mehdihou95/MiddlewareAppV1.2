package com.xml.processor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "mapping_rules")
@EqualsAndHashCode(callSuper = true)
public class MappingRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "xml_path", nullable = false)
    private String xmlPath;

    @Column(name = "database_field", nullable = false)
    private String databaseField;

    @Column
    private String transformation;

    @Column(name = "is_required")
    private Boolean required;

    @Column(name = "default_value")
    private String defaultValue;

    @Column
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_id")
    private Interface interfaceEntity;

    @Column(name = "interface_id", insertable = false, updatable = false)
    private Long interfaceId;

    @Column(length = 500)
    private String description;

    @Column
    private String sourceField;

    @Column
    private String targetField;

    @Column
    private String validationRule;

    @Column
    private Boolean isActive;

    @Column
    private String tableName;

    @Column
    private String dataType;

    @Column
    private Boolean isAttribute = false;

    @Column
    private String xsdElement;

    // Compatibility methods
    public String getXmlPath() {
        return xmlPath != null ? xmlPath : sourceField;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
        if (this.sourceField == null) {
            this.sourceField = xmlPath;
        }
    }

    public String getDatabaseField() {
        return databaseField != null ? databaseField : targetField;
    }

    public void setDatabaseField(String databaseField) {
        this.databaseField = databaseField;
        if (this.targetField == null) {
            this.targetField = databaseField;
        }
    }

    public boolean isAttribute() {
        return isAttribute != null ? isAttribute : false;
    }

    public void setAttribute(boolean isAttribute) {
        this.isAttribute = isAttribute;
    }

    // Interface compatibility methods
    public Long getInterfaceId() {
        return interfaceEntity != null ? interfaceEntity.getId() : null;
    }

    public void setInterfaceId(Long interfaceId) {
        if (interfaceId != null) {
            this.interfaceEntity = new Interface();
            this.interfaceEntity.setId(interfaceId);
        }
    }

    public boolean isRequired() {
        return required != null ? required : false;
    }
} 