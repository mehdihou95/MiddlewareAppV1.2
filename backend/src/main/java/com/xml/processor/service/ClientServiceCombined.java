package com.xml.processor.service;

import com.xml.processor.model.Client;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ClientServiceCombined {
    // New interface methods
    @Cacheable(value = "clients", key = "#id")
    Client getClientById(Long id);

    @Cacheable(value = "clients", key = "'all'")
    Page<Client> getAllClients(Pageable pageable);

    @Cacheable(value = "clients", key = "'search_' + #name")
    Page<Client> searchClients(String name, Pageable pageable);

    @Cacheable(value = "clients", key = "'status_' + #status")
    Page<Client> getClientsByStatus(String status, Pageable pageable);

    @CacheEvict(value = "clients", allEntries = true)
    Client createClient(Client client);

    @CacheEvict(value = "clients", key = "#id")
    Client updateClient(Long id, Client client);

    @CacheEvict(value = "clients", key = "#id")
    void deleteClient(Long id);

    Page<Client> getClients(int page, int size, String sortBy, String sortDirection, String searchTerm, String status);
    Page<Client> searchClients(String searchTerm, int page, int size, String sortBy, String sortDirection);
    Page<Client> getClientsByStatus(String status, int page, int size, String sortBy, String sortDirection);

    // Legacy interface methods
    Optional<Client> getClientByIdOptional(Long id);
    List<Client> getAllClientsLegacy();
    Optional<Client> findByClientName(String clientName);
    List<Client> findByClientNameContainingIgnoreCase(String clientName);
    List<Client> findByClientNameStartingWithIgnoreCase(String prefix);
    List<Client> findByClientNameEndingWithIgnoreCase(String suffix);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameContainingIgnoreCase(String name1, String name2);
    List<Client> findByClientNameContainingIgnoreCaseOrClientNameContainingIgnoreCase(String name1, String name2);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5, String name6);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5, String name6, String name7);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8, String name9);
    List<Client> findByClientNameContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCaseAndClientNameNotContainingIgnoreCase(String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8, String name9, String name10);
} 