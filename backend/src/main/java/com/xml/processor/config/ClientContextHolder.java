package com.xml.processor.config;

import com.xml.processor.model.Client;

public class ClientContextHolder {
    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Client> CLIENT_CONTEXT = new ThreadLocal<>();

    public static void setClientId(Long clientId) {
        CONTEXT.set(clientId);
    }

    public static Long getClientId() {
        return CONTEXT.get();
    }

    public static void setClient(Client client) {
        CLIENT_CONTEXT.set(client);
        if (client != null) {
            setClientId(client.getId());
        }
    }

    public static Client getClient() {
        return CLIENT_CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
        CLIENT_CONTEXT.remove();
    }
} 