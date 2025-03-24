package com.xml.processor.service.impl;

import com.xml.processor.model.Client;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.service.interfaces.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    @Transactional
    public Client saveClient(Client client) {
        if (clientRepository.existsByName(client.getName())) {
            throw new IllegalArgumentException("Client with name " + client.getName() + " already exists");
        }
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientByName(String name) {
        return clientRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return clientRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> getClients(Pageable pageable, String nameFilter, String statusFilter) {
        if (nameFilter != null && !nameFilter.isEmpty() && statusFilter != null && !statusFilter.isEmpty()) {
            return clientRepository.findByNameContainingIgnoreCaseAndStatus(nameFilter, statusFilter, pageable);
        } else if (nameFilter != null && !nameFilter.isEmpty()) {
            return clientRepository.findByNameContainingIgnoreCase(nameFilter, pageable);
        } else if (statusFilter != null && !statusFilter.isEmpty()) {
            return clientRepository.findByStatus(statusFilter, pageable);
        }

        return clientRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findByStatus(String status, Pageable pageable) {
        return clientRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findByNameContaining(String name, Pageable pageable) {
        return clientRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findActiveClients(Pageable pageable) {
        return clientRepository.findByStatus("ACTIVE", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Client> findInactiveClients(Pageable pageable) {
        return clientRepository.findByStatus("INACTIVE", pageable);
    }
} 