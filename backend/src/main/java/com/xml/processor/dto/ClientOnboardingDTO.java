package com.xml.processor.dto;

import lombok.Data;

@Data
public class ClientOnboardingDTO {
    private String name;
    private String description;
    private Boolean active = true;
} 