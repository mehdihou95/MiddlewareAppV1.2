package com.xml.processor.controller;

import com.xml.processor.model.MappingRule;
import com.xml.processor.service.XsdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mapping")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MappingController {

    @Autowired
    private XsdService xsdService;

    @GetMapping("/xsd-structure")
    public ResponseEntity<List<Map<String, Object>>> getXsdStructure(@RequestParam String xsdPath) {
        List<Map<String, Object>> elements = xsdService.getXsdStructure(xsdPath);
        return ResponseEntity.ok(elements);
    }

    @GetMapping("/rules")
    public ResponseEntity<List<MappingRule>> getAllMappingRules() {
        return ResponseEntity.ok(xsdService.getAllMappingRules());
    }

    @PostMapping("/rules")
    public ResponseEntity<MappingRule> createMappingRule(@RequestBody MappingRule rule) {
        // Validate required fields
        if (rule.getXmlPath() == null || rule.getXmlPath().isEmpty() ||
            rule.getDatabaseField() == null || rule.getDatabaseField().isEmpty() ||
            rule.getTableName() == null || rule.getTableName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }

        // Set default values if not provided
        if (rule.getDescription() == null) {
            rule.setDescription("Map " + rule.getXsdElement() + " to " + rule.getDatabaseField());
        }
        if (rule.getDataType() == null) {
            rule.setDataType("String");
        }
        
        MappingRule savedRule = xsdService.saveMappingRule(rule);
        return ResponseEntity.ok(savedRule);
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteMappingRule(@PathVariable Long id) {
        xsdService.deleteMappingRule(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save-configuration")
    public ResponseEntity<Void> saveMappingConfiguration(@RequestBody List<MappingRule> rules) {
        xsdService.saveMappingConfiguration(rules);
        return ResponseEntity.ok().build();
    }
} 