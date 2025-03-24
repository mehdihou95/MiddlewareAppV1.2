package com.xml.processor.service.impl;

import com.xml.processor.model.Client;
import com.xml.processor.model.MappingRule;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.repository.MappingRuleRepository;
import com.xml.processor.service.interfaces.ClientOnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ClientOnboardingServiceImpl implements ClientOnboardingService {

    private final ClientRepository clientRepository;
    private final MappingRuleRepository mappingRuleRepository;

    @Autowired
    public ClientOnboardingServiceImpl(ClientRepository clientRepository, MappingRuleRepository mappingRuleRepository) {
        this.clientRepository = clientRepository;
        this.mappingRuleRepository = mappingRuleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MappingRule> getDefaultMappingRules() {
        return mappingRuleRepository.findByIsDefaultTrue();
    }

    @Override
    @Transactional
    public Client onboardNewClient(Client client, List<MappingRule> defaultRules) {
        Client savedClient = clientRepository.save(client);
        defaultRules.forEach(rule -> {
            rule.setClient(savedClient);
            mappingRuleRepository.save(rule);
        });
        return savedClient;
    }

    @Override
    @Transactional
    public Client cloneClientConfiguration(Long sourceClientId, Client newClient) {
        Client sourceClient = clientRepository.findById(sourceClientId)
                .orElseThrow(() -> new RuntimeException("Source client not found"));
        
        Client savedClient = clientRepository.save(newClient);
        
        List<MappingRule> sourceRules = mappingRuleRepository.findByClient(sourceClient);
        sourceRules.forEach(rule -> {
            MappingRule newRule = new MappingRule(rule);
            newRule.setClient(savedClient);
            mappingRuleRepository.save(newRule);
        });
        
        return savedClient;
    }

    @Override
    @Transactional
    public Client onboardClient(Client client, MultipartFile[] configFiles) {
        // TODO: Implement configuration file processing
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client updateClientConfiguration(Long clientId, MultipartFile[] configFiles) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        // TODO: Implement configuration file processing
        return clientRepository.save(client);
    }

    @Override
    public boolean validateClientConfiguration(MultipartFile[] configFiles) {
        // TODO: Implement configuration validation
        return true;
    }

    @Override
    @Transactional
    public void processClientConfiguration(Long clientId, MultipartFile[] configFiles) {
        // TODO: Implement configuration processing
    }
} 