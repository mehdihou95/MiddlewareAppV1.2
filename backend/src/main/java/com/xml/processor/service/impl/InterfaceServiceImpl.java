package com.xml.processor.service.impl;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.Interface;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.InterfaceService;
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
public class InterfaceServiceImpl implements InterfaceService {
    
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
    @Transactional
    @CacheEvict(value = "interfaces", allEntries = true)
    public Interface createInterface(Interface interfaceEntity) {
        if (interfaceRepository.existsByNameAndClient_Id(interfaceEntity.getName(), interfaceEntity.getClient().getId())) {
            throw new RuntimeException("Interface with name " + interfaceEntity.getName() + " already exists for this client");
        }
        return interfaceRepository.save(interfaceEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public Interface updateInterface(Long id, Interface interfaceEntity) {
        Interface existingInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interface not found with id: " + id));
        
        if (interfaceRepository.existsByNameAndClient_IdAndIdNot(
                interfaceEntity.getName(),
                interfaceEntity.getClient().getId(),
                id)) {
            throw new RuntimeException("Interface with name " + interfaceEntity.getName() + " already exists for this client");
        }

        existingInterface.setName(interfaceEntity.getName());
        existingInterface.setType(interfaceEntity.getType());
        existingInterface.setDescription(interfaceEntity.getDescription());
        existingInterface.setSchemaPath(interfaceEntity.getSchemaPath());
        existingInterface.setRootElement(interfaceEntity.getRootElement());
        existingInterface.setNamespace(interfaceEntity.getNamespace());
        existingInterface.setIsActive(interfaceEntity.getIsActive());
        existingInterface.setPriority(interfaceEntity.getPriority());

        return interfaceRepository.save(existingInterface);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public void deleteInterface(Long id) {
        interfaceRepository.deleteById(id);
    }
    
    @Override
    public Page<Interface> getInterfaces(int page, int size, String sortBy, String sortDirection, String searchTerm, String status, Boolean isActive) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            return searchInterfaces(searchTerm, pageable);
        } else if (isActive != null) {
            return getInterfacesByStatus(isActive, pageable);
        }

        return getAllInterfaces(pageable);
    }
    
    @Override
    public Page<Interface> getInterfacesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return getInterfacesByClient(clientId, PageRequest.of(page, size, sort));
    }
    
    @Override
    public Page<Interface> searchInterfaces(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return searchInterfaces(searchTerm, PageRequest.of(page, size, sort));
    }
    
    @Override
    public Page<Interface> getInterfacesByType(String type, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return interfaceRepository.findByType(type, PageRequest.of(page, size, sort));
    }
    
    @Override
    public Page<Interface> getInterfacesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return getInterfacesByStatus(isActive, PageRequest.of(page, size, sort));
    }
} 