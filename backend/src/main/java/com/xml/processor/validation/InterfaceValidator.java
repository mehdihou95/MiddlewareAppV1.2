package com.xml.processor.validation;

import com.xml.processor.model.Interface;
import com.xml.processor.repository.InterfaceRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dedicated validator for Interface entities.
 * Handles both standard Bean Validation and specific business rules for interfaces.
 */
@Component
public class InterfaceValidator {
    
    @Autowired
    private InterfaceRepository interfaceRepository;
    
    @Autowired
    private Validator validator;
    
    /**
     * Validates an interface entity.
     * Performs both standard Bean Validation and specific business rule validation.
     * 
     * @param interface_ The interface to validate
     * @throws ValidationException if validation fails
     */
    public void validate(Interface interface_) {
        // Standard validation with Bean Validation
        Set<ConstraintViolation<Interface>> violations = validator.validate(interface_);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", ")));
        }
        
        // Specific validation for name uniqueness per client
        if (interface_.getClient() != null && interface_.getClient().getId() != null) {
            boolean exists = interfaceRepository.existsByNameAndClient_Id(
                interface_.getName(), 
                interface_.getClient().getId()
            );
            
            if (exists && (interface_.getId() == null || 
                !interfaceRepository.findById(interface_.getId()).get().getName().equals(interface_.getName()))) {
                throw new ValidationException("Interface with name " + interface_.getName() + 
                    " already exists for this client");
            }
        }
    }
} 