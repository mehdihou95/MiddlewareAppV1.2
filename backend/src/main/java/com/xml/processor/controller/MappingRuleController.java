package com.xml.processor.controller;

import com.xml.processor.model.MappingRule;
import com.xml.processor.service.MappingRuleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mapping-rules")
public class MappingRuleController {

    private final MappingRuleService mappingRuleService;

    public MappingRuleController(MappingRuleService mappingRuleService) {
        this.mappingRuleService = mappingRuleService;
    }

    @GetMapping
    public ResponseEntity<Page<MappingRule>> getMappingRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) Boolean isActiveFilter) {
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRules(page, size, sortBy, direction, nameFilter, isActiveFilter);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingRule> getMappingRule(@PathVariable Long id) {
        return ResponseEntity.ok(mappingRuleService.getMappingRuleById(id));
    }

    @PostMapping
    public ResponseEntity<MappingRule> createMappingRule(@RequestBody MappingRule mappingRule) {
        return ResponseEntity.ok(mappingRuleService.createMappingRule(mappingRule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingRule> updateMappingRule(@PathVariable Long id, @RequestBody MappingRule mappingRule) {
        return ResponseEntity.ok(mappingRuleService.updateMappingRule(id, mappingRule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMappingRule(@PathVariable Long id) {
        mappingRuleService.deleteMappingRule(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interface/{interfaceId}")
    public ResponseEntity<Page<MappingRule>> getMappingRulesByInterface(
            @PathVariable Long interfaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRulesByInterface(interfaceId, page, size, sortBy, direction);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MappingRule>> searchMappingRules(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<MappingRule> mappingRules = mappingRuleService.searchMappingRules(name, page, size, sortBy, direction);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/status/{isActive}")
    public ResponseEntity<Page<MappingRule>> getMappingRulesByStatus(
            @PathVariable boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRulesByStatus(isActive, page, size, sortBy, direction);
        return ResponseEntity.ok(mappingRules);
    }
} 