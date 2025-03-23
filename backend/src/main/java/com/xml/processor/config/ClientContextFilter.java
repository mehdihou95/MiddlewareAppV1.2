package com.xml.processor.config;

import com.xml.processor.model.Client;
import com.xml.processor.service.interfaces.ClientService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class ClientContextFilter extends OncePerRequestFilter {

    @Autowired
    private ClientService clientService;

    private static final String CLIENT_ID_HEADER = "X-Client-ID";
    private static final String CLIENT_NAME_HEADER = "X-Client-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIdHeader = request.getHeader(CLIENT_ID_HEADER);
        String clientName = request.getHeader(CLIENT_NAME_HEADER);

        if (clientIdHeader != null && !clientIdHeader.isEmpty()) {
            try {
                Long clientId = Long.parseLong(clientIdHeader);
                Optional<Client> clientOpt = clientService.getClientById(clientId);
                if (clientOpt.isPresent()) {
                    ClientContextHolder.setClient(clientOpt.get());
                } else {
                    throw new RuntimeException("Client not found with ID: " + clientId);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid client ID format: " + clientIdHeader);
            }
        } else if (clientName != null && !clientName.isEmpty()) {
            try {
                Client client = clientService.getClientByName(clientName);
                ClientContextHolder.setClient(client);
            } catch (RuntimeException e) {
                throw new RuntimeException("Client not found with name: " + clientName);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            ClientContextHolder.clear();
        }
    }
} 