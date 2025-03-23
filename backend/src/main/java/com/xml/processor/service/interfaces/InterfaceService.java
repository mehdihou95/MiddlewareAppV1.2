package com.xml.processor.service.interfaces;
    
import com.xml.processor.model.Interface;
import com.xml.processor.model.Client;
import java.util.List;
import java.util.Optional;
    
public interface InterfaceService {
    List<Interface> getAllInterfaces();
    List<Interface> getClientInterfaces(Long clientId);
    Optional<Interface> getInterfaceById(Long id);
    Optional<Interface> getInterfaceByName(String name, Long clientId);
    Interface createInterface(Interface interfaceEntity);
    Interface updateInterface(Long id, Interface interfaceEntity);
    void deleteInterface(Long id);
    Interface detectInterface(String xmlContent, Long clientId);
    List<Interface> getInterfacesByClient(Client client);
} 