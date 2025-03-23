package com.xml.processor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "asn_lines")
@Getter
@Setter
public class AsnLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private AsnHeader header;

    @Column
    private Integer lineNumber;

    @Column
    private String itemNumber;

    @Column
    private String itemDescription;

    @Column
    private Integer quantity;

    @Column
    private String unitOfMeasure;

    @Column
    private String lotNumber;

    @Column
    private String serialNumber;

    @Column
    private String status;

    @Column(length = 500)
    private String notes;

    // Compatibility method for tests
    public void setLineNumber(String lineNumberStr) {
        try {
            this.lineNumber = Integer.parseInt(lineNumberStr);
        } catch (NumberFormatException e) {
            this.lineNumber = 0;
        }
    }

    // Compatibility method for header ID
    public void setHeaderId(Long headerId) {
        if (headerId != null) {
            this.header = new AsnHeader();
            this.header.setId(headerId);
        }
    }
} 