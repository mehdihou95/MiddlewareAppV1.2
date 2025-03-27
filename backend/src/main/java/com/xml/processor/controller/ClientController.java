package com.xml.processor.controller;

import com.xml.processor.model.Client;
import com.xml.processor.model.Interface;
import com.xml.processor.service.interfaces.ClientService;
import com.xml.processor.service.interfaces.InterfaceService;
import com.xml.processor.dto.ClientOnboardingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Page<Client>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) String statusFilter) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Client> clients = clientService.getClients(pageRequest, nameFilter, statusFilter);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Client> getClientByName(@PathVariable String name) {
        try {
            Optional<Client> clientOpt = clientService.getClientByName(name);
            return clientOpt.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
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

    @PostMapping("/onboarding/new")
    public ResponseEntity<Client> onboardNewClient(@RequestBody ClientOnboardingDTO clientData) {
        Client client = clientService.onboardNewClient(clientData);
        return ResponseEntity.ok(client);
    }

    @PostMapping("/onboarding/clone/{sourceClientId}")
    public ResponseEntity<Client> cloneClient(
        @PathVariable Long sourceClientId,
        @RequestBody ClientOnboardingDTO clientData
    ) {
        Client client = clientService.cloneClient(sourceClientId, clientData);
        return ResponseEntity.ok(client);
    }
} 