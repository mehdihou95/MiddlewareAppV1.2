package com.xml.processor.service.impl;

import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.interfaces.ClientService;
import com.xml.processor.dto.ClientOnboardingDTO;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final InterfaceRepository interfaceRepository;
    private final Validator validator;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, InterfaceRepository interfaceRepository, Validator validator) {
        this.clientRepository = clientRepository;
        this.interfaceRepository = interfaceRepository;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    @Transactional
    public Client saveClient(Client client) {
        // Validation with Bean Validation
        var violations = validator.validate(client);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", ")));
        }
        
        // Specific validation for name uniqueness
        if (clientRepository.existsByName(client.getName()) && 
            (client.getId() == null || !clientRepository.findById(client.getId()).get().getName().equals(client.getName()))) {
            throw new ValidationException("Client with name " + client.getName() + " already exists");
        }
        
        // Specific validation for code uniqueness
        if (clientRepository.existsByCode(client.getCode()) && 
            (client.getId() == null || !clientRepository.findById(client.getId()).get().getCode().equals(client.getCode()))) {
            throw new ValidationException("Client with code " + client.getCode() + " already exists");
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
    public Page<Client> getClients(Pageable pageable, String filter, String status) {
        if (status != null && !status.isEmpty()) {
            return clientRepository.findByStatus(status, pageable);
        }
        
        if (filter != null && !filter.isEmpty()) {
            return clientRepository.findByNameContainingIgnoreCase(filter, pageable);
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

    @Override
    @Transactional
    public Client onboardNewClient(ClientOnboardingDTO clientData) {
        Client client = new Client();
        client.setName(clientData.getName());
        client.setDescription(clientData.getDescription());
        client.setActive(clientData.getActive() != null ? clientData.getActive() : true);
        client.setCreatedDate(LocalDateTime.now());
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client cloneClient(Long sourceClientId, ClientOnboardingDTO clientData) {
        Client sourceClient = clientRepository.findById(sourceClientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + sourceClientId));
        
        Client newClient = new Client();
        newClient.setName(clientData.getName());
        newClient.setDescription(clientData.getDescription());
        newClient.setActive(clientData.getActive() != null ? clientData.getActive() : true);
        newClient.setCreatedDate(LocalDateTime.now());
        
        // Save the new client first to get an ID
        Client savedClient = clientRepository.save(newClient);
        
        // Clone interfaces if needed
        List<Interface> sourceInterfaces = interfaceRepository.findByClientId(sourceClientId);
        for (Interface sourceInterface : sourceInterfaces) {
            Interface newInterface = new Interface();
            newInterface.setName(sourceInterface.getName());
            newInterface.setDescription(sourceInterface.getDescription());
            newInterface.setType(sourceInterface.getType());
            newInterface.setActive(sourceInterface.isActive());
            newInterface.setClient(savedClient);
            interfaceRepository.save(newInterface);
        }
        
        return savedClient;
    }
} 