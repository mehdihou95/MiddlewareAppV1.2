package com.xml.processor.controller;

import com.xml.processor.model.MappingRule;
import com.xml.processor.service.interfaces.MappingRuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRules(pageRequest, nameFilter, isActiveFilter);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingRule> getMappingRule(@PathVariable Long id) {
        Optional<MappingRule> ruleOpt = mappingRuleService.getMappingRuleById(id);
        return ruleOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRulesByInterface(interfaceId, pageRequest);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MappingRule>> searchMappingRules(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<MappingRule> mappingRules = mappingRuleService.searchMappingRules(name, pageRequest);
        return ResponseEntity.ok(mappingRules);
    }

    @GetMapping("/status/{isActive}")
    public ResponseEntity<Page<MappingRule>> getMappingRulesByStatus(
            @PathVariable boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<MappingRule> mappingRules = mappingRuleService.getMappingRulesByStatus(isActive, pageRequest);
        return ResponseEntity.ok(mappingRules);
    }
} 