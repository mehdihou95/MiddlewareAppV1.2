package com.xml.processor.service.impl;

import com.xml.processor.model.Interface;
import com.xml.processor.model.Client;
import com.xml.processor.repository.InterfaceRepository;
import com.xml.processor.service.interfaces.InterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("interfaceServiceRoot")
public class InterfaceServiceRootImpl implements InterfaceService {

    @Autowired
    private InterfaceRepository interfaceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Interface> getAllInterfaces() {
        return interfaceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interface> getClientInterfaces(Long clientId) {
        return interfaceRepository.findByClient_Id(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Interface> getInterfaceById(Long id) {
        return interfaceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Interface> getInterfaceByName(String name, Long clientId) {
        return interfaceRepository.findByClient_IdAndName(clientId, name);
    }

    @Override
    @Transactional
    public Interface createInterface(Interface interfaceEntity) {
        return interfaceRepository.save(interfaceEntity);
    }

    @Override
    @Transactional
    public Interface updateInterface(Long id, Interface interfaceEntity) {
        Interface existingInterface = interfaceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Interface not found with id: " + id));
        interfaceEntity.setId(id);
        return interfaceRepository.save(interfaceEntity);
    }

    @Override
    @Transactional
    public void deleteInterface(Long id) {
        interfaceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Interface detectInterface(String xmlContent, Long clientId) {
        // TODO: Implement interface detection logic
        throw new UnsupportedOperationException("Interface detection not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interface> getInterfacesByClient(Client client) {
        return interfaceRepository.findByClient(client);
    }
} 