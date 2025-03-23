package com.xml.processor.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "asn_headers")
@Getter
@Setter
public class AsnHeader extends BaseEntity {

    @Column(nullable = false)
    private String documentNumber;

    @Column
    private String documentType;

    @Column
    private String senderId;

    @Column
    private String receiverId;

    @Column
    private String documentDate;

    @Column
    private String documentTime;

    @Column
    private String status;

    @Column(length = 1000)
    private String notes;
} 