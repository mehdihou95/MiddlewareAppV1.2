package com.xml.processor.service;

import com.xml.processor.model.Client;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
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
} 