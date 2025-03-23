package com.xml.processor.service.impl;

import com.xml.processor.model.Client;
import com.xml.processor.repository.ClientRepository;
import com.xml.processor.service.interfaces.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

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
    public Client getClientByName(String name) {
        return clientRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Client not found with name: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return clientRepository.existsByName(name);
    }
} 