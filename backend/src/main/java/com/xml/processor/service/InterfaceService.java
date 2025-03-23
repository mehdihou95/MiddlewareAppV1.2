package com.xml.processor.service;

import com.xml.processor.model.Interface;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterfaceService {
    @Cacheable(value = "interfaces", key = "#id")
    Interface getInterfaceById(Long id);

    @Cacheable(value = "interfaces", key = "'all'")
    Page<Interface> getAllInterfaces(Pageable pageable);

    @Cacheable(value = "interfaces", key = "'client_' + #clientId")
    Page<Interface> getInterfacesByClient(Long clientId, Pageable pageable);

    @Cacheable(value = "interfaces", key = "'search_' + #name")
    Page<Interface> searchInterfaces(String name, Pageable pageable);

    @Cacheable(value = "interfaces", key = "'active_' + #isActive")
    Page<Interface> getInterfacesByStatus(boolean isActive, Pageable pageable);

    @CacheEvict(value = "interfaces", allEntries = true)
    Interface createInterface(Interface interfaceEntity);

    @CacheEvict(value = "interfaces", key = "#id")
    Interface updateInterface(Long id, Interface interfaceEntity);

    @CacheEvict(value = "interfaces", key = "#id")
    void deleteInterface(Long id);

    Page<Interface> getInterfaces(int page, int size, String sortBy, String sortDirection, String searchTerm, String status, Boolean isActive);
    Page<Interface> getInterfacesByClient(Long clientId, int page, int size, String sortBy, String sortDirection);
    Page<Interface> searchInterfaces(String searchTerm, int page, int size, String sortBy, String sortDirection);
    Page<Interface> getInterfacesByType(String type, int page, int size, String sortBy, String sortDirection);
    Page<Interface> getInterfacesByStatus(boolean isActive, int page, int size, String sortBy, String sortDirection);
} 