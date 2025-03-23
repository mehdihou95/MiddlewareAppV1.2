package com.xml.processor.controller;

import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.service.interfaces.ClientService;
import com.xml.processor.service.interfaces.InterfaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final InterfaceService interfaceService;

    public ClientController(ClientService clientService, InterfaceService interfaceService) {
        this.clientService = clientService;
        this.interfaceService = interfaceService;
    }

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.saveClient(client));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        Optional<Client> clientOpt = clientService.getClientById(id);
        return clientOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        Optional<Client> existingClientOpt = clientService.getClientById(id);
        if (existingClientOpt.isPresent()) {
            client.setId(id);
            return ResponseEntity.ok(clientService.saveClient(client));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        if (clientService.getClientById(id).isPresent()) {
            clientService.deleteClient(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Client> getClientByName(@PathVariable String name) {
        try {
            return ResponseEntity.ok(clientService.getClientByName(name));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{clientId}/interfaces")
    public ResponseEntity<Interface> createClientInterface(
            @PathVariable Long clientId,
            @RequestBody Interface interfaceEntity) {
        try {
            Client client = clientService.getClientById(clientId).orElseThrow();
            interfaceEntity.setClient(client);
            Interface created = interfaceService.createInterface(interfaceEntity);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{clientId}/interfaces")
    public ResponseEntity<List<Interface>> getClientInterfaces(@PathVariable Long clientId) {
        try {
            Client client = clientService.getClientById(clientId).orElseThrow();
            return ResponseEntity.ok(interfaceService.getInterfacesByClient(client));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 