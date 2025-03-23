package com.xml.processor.service.impl;

import com.xml.processor.model.Interface;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.InterfaceServiceCombined;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterfaceServiceCombinedImpl implements InterfaceServiceCombined {

    @Autowired
    private InterfaceRepository interfaceRepository;

    @Override
    @Cacheable(value = "interfaces", key = "#id")
    public Interface getInterfaceById(Long id) {
        return interfaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interface not found with id: " + id));
    }

    @Override
    @Cacheable(value = "interfaces", key = "'all'")
    public Page<Interface> getAllInterfaces(Pageable pageable) {
        return interfaceRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "interfaces", key = "'client_' + #clientId")
    public Page<Interface> getInterfacesByClient(Long clientId, Pageable pageable) {
        return interfaceRepository.findByClient_Id(clientId, pageable);
    }

    @Override
    @Cacheable(value = "interfaces", key = "'search_' + #name")
    public Page<Interface> searchInterfaces(String name, Pageable pageable) {
        return interfaceRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Cacheable(value = "interfaces", key = "'active_' + #isActive")
    public Page<Interface> getInterfacesByStatus(boolean isActive, Pageable pageable) {
        return interfaceRepository.findByIsActive(isActive, pageable);
    }

    @Override
    @CacheEvict(value = "interfaces", allEntries = true)
    public Interface createInterface(Interface interfaceEntity) {
        return interfaceRepository.save(interfaceEntity);
    }

    @Override
    @CacheEvict(value = "interfaces", key = "#id")
    public Interface updateInterface(Long id, Interface interfaceEntity) {
        Interface existingInterface = getInterfaceById(id);
        // Update fields
        existingInterface.setName(interfaceEntity.getName());
        existingInterface.setType(interfaceEntity.getType());
        existingInterface.setDescription(interfaceEntity.getDescription());
        existingInterface.setIsActive(interfaceEntity.getIsActive());
        existingInterface.setPriority(interfaceEntity.getPriority());
        existingInterface.setRootElement(interfaceEntity.getRootElement());
        existingInterface.setNamespace(interfaceEntity.getNamespace());
        existingInterface.setSchemaPath(interfaceEntity.getSchemaPath());
        return interfaceRepository.save(existingInterface);
    }

    @Override
    @CacheEvict(value = "interfaces", key = "#id")
    public void deleteInterface(Long id) {
        interfaceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Interface> getInterfaces(int page, int size, String sortBy, String sortDirection, String searchTerm, String status, Boolean isActive) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return interfaceRepository.findByNameContainingIgnoreCase(searchTerm, pageRequest);
        } else if (isActive != null) {
            return interfaceRepository.findByIsActive(isActive, pageRequest);
        }
        
        return interfaceRepository.findAll(pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Interface> searchInterfaces(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return interfaceRepository.findByNameContainingIgnoreCase(searchTerm, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Interface> getInterfacesByType(String type, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return interfaceRepository.findByType(type, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Interface> getInterfacesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return interfaceRepository.findByIsActive(isActive, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Interface> getInterfacesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByClient_Id(clientId, pageRequest);
    }
} 