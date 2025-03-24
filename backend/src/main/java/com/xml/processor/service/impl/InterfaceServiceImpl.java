package com.xml.processor.service.impl;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.exception.ResourceNotFoundException;
import com.xml.processor.exception.ValidationException;
import com.xml.processor.model.Interface;
import com.xml.processor.model.Client;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.interfaces.InterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the InterfaceService interface.
 * Provides CRUD operations, search functionality, and client-specific operations for Interface entities.
 */
@Service
public class InterfaceServiceImpl implements InterfaceService {
    
    @Autowired
    private InterfaceRepository interfaceRepository;
    
    @Override
    public List<Interface> getAllInterfaces() {
        return interfaceRepository.findAll();
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "#id")
    public Optional<Interface> getInterfaceById(Long id) {
        return interfaceRepository.findById(id);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", allEntries = true)
    public Interface createInterface(Interface interfaceEntity) {
        validateInterface(interfaceEntity);
        return interfaceRepository.save(interfaceEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public Interface updateInterface(Long id, Interface interfaceEntity) {
        Interface existingInterface = interfaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interface not found with id: " + id));
        
        validateInterface(interfaceEntity);
        interfaceEntity.setId(id);
        return interfaceRepository.save(interfaceEntity);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "interfaces", key = "#id")
    public void deleteInterface(Long id) {
        if (!interfaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interface not found with id: " + id);
        }
        interfaceRepository.deleteById(id);
    }
    
    @Override
    public List<Interface> getInterfacesByClient(Client client) {
        return interfaceRepository.findByClient(client);
    }
    
    @Override
    public List<Interface> getClientInterfaces(Long clientId) {
        return interfaceRepository.findByClient_Id(clientId);
    }
    
    @Override
    public Optional<Interface> getInterfaceByName(String name, Long clientId) {
        return interfaceRepository.findByNameAndClient_Id(name, clientId);
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
    public Page<Interface> getInterfacesByClient(Long clientId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByClient_Id(clientId, pageRequest);
    }
    
    @Override
    public Page<Interface> getInterfaces(int page, int size, String sortBy, String sortDirection, 
                                       String searchTerm, Boolean isActive) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return interfaceRepository.findByNameContaining(searchTerm, pageRequest);
        } else if (isActive != null) {
            return interfaceRepository.findByIsActive(isActive, pageRequest);
        }
        
        return interfaceRepository.findAll(pageRequest);
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "'search_' + #name")
    public Page<Interface> searchInterfaces(String name, Pageable pageable) {
        return interfaceRepository.findByNameContaining(name, pageable);
    }
    
    @Override
    public Page<Interface> searchInterfaces(String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByNameContaining(searchTerm, pageRequest);
    }
    
    @Override
    public Page<Interface> getInterfacesByType(String type, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByType(type, pageRequest);
    }
    
    @Override
    @Cacheable(value = "interfaces", key = "'active_' + #isActive")
    public Page<Interface> getInterfacesByStatus(boolean isActive, Pageable pageable) {
        return interfaceRepository.findByIsActive(isActive, pageable);
    }
    
    @Override
    public Page<Interface> getInterfacesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return interfaceRepository.findByIsActive(isActive, pageRequest);
    }
    
    @Override
    public Interface detectInterface(String xmlContent, Long clientId) {
        // TODO: Implement XML content analysis to detect interface type
        throw new UnsupportedOperationException("Interface detection not yet implemented");
    }
    
    /**
     * Validates an interface entity before saving.
     *
     * @param interfaceEntity The interface to validate
     * @throws ValidationException if the interface is invalid
     */
    private void validateInterface(Interface interfaceEntity) {
        if (interfaceEntity.getName() == null || interfaceEntity.getName().trim().isEmpty()) {
            throw new ValidationException("Interface name is required");
        }

        if (interfaceEntity.getClient() == null || interfaceEntity.getClient().getId() == null) {
            throw new ValidationException("Client is required");
        }

        if (interfaceRepository.existsByNameAndClient_Id(interfaceEntity.getName(), interfaceEntity.getClient().getId())) {
            throw new ValidationException("Interface with name " + interfaceEntity.getName() + 
                " already exists for this client");
        }
    }
} 