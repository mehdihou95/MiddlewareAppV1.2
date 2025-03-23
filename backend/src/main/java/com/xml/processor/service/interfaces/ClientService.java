package com.xml.processor.service.interfaces;

import com.xml.processor.model.Client;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Client entities.
 * Provides methods for CRUD operations and client-specific queries.
 */
public interface ClientService {
    /**
     * Retrieve all clients in the system.
     *
     * @return List of all clients
     */
    List<Client> getAllClients();

    /**
     * Find a client by their ID.
     *
     * @param id The client ID to search for
     * @return Optional containing the client if found
     */
    Optional<Client> getClientById(Long id);

    /**
     * Save a new client or update an existing one.
     *
     * @param client The client entity to save
     * @return The saved client with updated information
     */
    Client saveClient(Client client);

    /**
     * Delete a client by their ID.
     *
     * @param id The ID of the client to delete
     */
    void deleteClient(Long id);

    /**
     * Find a client by their name.
     *
     * @param name The name of the client to search for
     * @return The client if found, null otherwise
     */
    Client getClientByName(String name);

    /**
     * Check if a client with the given name exists.
     *
     * @param name The name to check
     * @return true if a client with the name exists, false otherwise
     */
    boolean existsByName(String name);
} 