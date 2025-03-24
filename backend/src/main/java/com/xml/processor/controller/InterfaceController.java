package com.xml.processor.controller;

import com.xml.processor.exception.ResourceNotFoundException;
import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Interface;
import com.xml.processor.service.interfaces.InterfaceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/interfaces")
public class InterfaceController {

    private final InterfaceService interfaceService;

    public InterfaceController(InterfaceService interfaceService) {
        this.interfaceService = interfaceService;
    }

    @GetMapping
    public ResponseEntity<Page<Interface>> getInterfaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isActive) {
        
        try {
            Page<Interface> interfaces = interfaceService.getInterfaces(page, size, sortBy, sortDirection, searchTerm, isActive);
            return ResponseEntity.ok(interfaces);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interface> getInterfaceById(@PathVariable Long id) {
        try {
            Optional<Interface> interfaceOpt = interfaceService.getInterfaceById(id);
            return interfaceOpt.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Interface> createInterface(@RequestBody Interface interface_) {
        try {
            Interface createdInterface = interfaceService.createInterface(interface_);
            return ResponseEntity.ok(createdInterface);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interface> updateInterface(@PathVariable Long id, @RequestBody Interface interface_) {
        try {
            Interface updatedInterface = interfaceService.updateInterface(id, interface_);
            return ResponseEntity.ok(updatedInterface);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterface(@PathVariable Long id) {
        try {
            interfaceService.deleteInterface(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 