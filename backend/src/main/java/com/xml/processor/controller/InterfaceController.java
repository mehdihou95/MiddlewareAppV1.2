package com.xml.processor.controller;

import com.xml.processor.model.Interface;
import com.xml.processor.service.InterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interfaces")
public class InterfaceController {

    @Autowired
    private InterfaceService interfaceService;

    @GetMapping
    public ResponseEntity<Page<Interface>> getInterfaces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Interface> interfaces = interfaceService.getInterfaces(page, size, sortBy, sortDirection, searchTerm, status, isActive);
        return ResponseEntity.ok(interfaces);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interface> getInterfaceById(@PathVariable Long id) {
        Interface interface_ = interfaceService.getInterfaceById(id);
        return ResponseEntity.ok(interface_);
    }

    @PostMapping
    public ResponseEntity<Interface> createInterface(@RequestBody Interface interface_) {
        Interface createdInterface = interfaceService.createInterface(interface_);
        return ResponseEntity.ok(createdInterface);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Interface> updateInterface(@PathVariable Long id, @RequestBody Interface interface_) {
        Interface updatedInterface = interfaceService.updateInterface(id, interface_);
        return ResponseEntity.ok(updatedInterface);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterface(@PathVariable Long id) {
        interfaceService.deleteInterface(id);
        return ResponseEntity.ok().build();
    }
} 