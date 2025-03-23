package com.xml.processor.service;

import com.xml.processor.model.Client;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientOnboardingService {
    private static final Logger logger = LoggerFactory.getLogger(ClientOnboardingService.class);
    private final ClientService clientService;
    private final ClientRepository clientRepository;
    private final MappingRuleRepository mappingRuleRepository;

    public ClientOnboardingService(ClientService clientService, ClientRepository clientRepository, MappingRuleRepository mappingRuleRepository) {
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.mappingRuleRepository = mappingRuleRepository;
    }

    @Transactional
    public Client onboardClient(Client client) {
        logger.info("Starting onboarding process for client: {}", client.getName());
        
        // Check if client already exists
        if (clientService.existsByName(client.getName())) {
            throw new IllegalArgumentException("Client with name " + client.getName() + " already exists");
        }

        // Save the client
        Client savedClient = clientService.saveClient(client);
        logger.info("Client saved successfully with ID: {}", savedClient.getId());

        // Initialize client-specific configurations
        initializeClientConfigurations(savedClient);

        return savedClient;
    }

    private void initializeClientConfigurations(Client client) {
        logger.info("Initializing configurations for client: {}", client.getName());
        // Create default interface configurations
        createDefaultInterfaces(client);
        // Create default mapping rules
        createDefaultMappingRules(client);
        // Set up monitoring
        setupClientMonitoring(client);
    }

    private void createDefaultInterfaces(Client client) {
        logger.debug("Creating default interfaces for client: {}", client.getName());
        // Implementation for creating default interfaces
    }

    private void createDefaultMappingRules(Client client) {
        logger.debug("Creating default mapping rules for client: {}", client.getName());
        // Implementation for creating default mapping rules
    }

    private void setupClientMonitoring(Client client) {
        logger.debug("Setting up monitoring for client: {}", client.getName());
        // Implementation for setting up client monitoring
    }

    @Transactional
    public Client onboardNewClient(Client client, List<MappingRule> defaultMappingRules) {
        // Validate client data
        validateClientData(client);

        // Create the client
        Client newClient = clientService.saveClient(client);

        // Apply default mapping rules
        if (defaultMappingRules != null && !defaultMappingRules.isEmpty()) {
            for (MappingRule rule : defaultMappingRules) {
                rule.setClient(newClient);
                mappingRuleRepository.save(rule);
            }
        }

        return newClient;
    }

    @Transactional
    public Client cloneClientConfiguration(Long sourceClientId, Client newClient) {
        // Get source client
        Client sourceClient = clientService.getClientById(sourceClientId)
                .orElseThrow(() -> new IllegalArgumentException("Source client not found"));

        // Create new client
        Client createdClient = clientService.saveClient(newClient);

        // Clone mapping rules
        List<MappingRule> sourceRules = mappingRuleRepository.findByClient_Id(sourceClientId);
        for (MappingRule sourceRule : sourceRules) {
            MappingRule newRule = new MappingRule();
            newRule.setClient(createdClient);
            newRule.setName(sourceRule.getName());
            newRule.setDescription(sourceRule.getDescription());
            newRule.setSourceField(sourceRule.getSourceField());
            newRule.setTargetField(sourceRule.getTargetField());
            newRule.setTransformation(sourceRule.getTransformation());
            mappingRuleRepository.save(newRule);
        }

        return createdClient;
    }

    private void validateClientData(Client client) {
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }

        if (client.getName().length() > 255) {
            throw new IllegalArgumentException("Client name must not exceed 255 characters");
        }

        if (client.getDescription() != null && client.getDescription().length() > 500) {
            throw new IllegalArgumentException("Client description must not exceed 500 characters");
        }

        if (client.getStatus() == null) {
            throw new IllegalArgumentException("Client status is required");
        }
    }

    public List<MappingRule> getDefaultMappingRules() {
        // Return a list of default mapping rules that can be used as a template
        List<MappingRule> defaultRules = new ArrayList<>();
        
        MappingRule rule1 = new MappingRule();
        rule1.setName("Default Header Rule");
        rule1.setDescription("Default mapping rule for ASN headers");
        rule1.setSourceField("documentNumber");
        rule1.setTargetField("docNumber");
        defaultRules.add(rule1);

        MappingRule rule2 = new MappingRule();
        rule2.setName("Default Line Rule");
        rule2.setDescription("Default mapping rule for ASN lines");
        rule2.setSourceField("lineNumber");
        rule2.setTargetField("lineNum");
        defaultRules.add(rule2);

        return defaultRules;
    }
} 