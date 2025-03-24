import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { Client } from '../types';
import { clientService } from '../services/clientService';
import { useAuth } from './AuthContext';

interface ClientContextType {
    clients: Client[];
    selectedClient: Client | null;
    error: string | null;
    loading: boolean;
    loadClients: () => Promise<void>;
    selectClient: (client: Client | null) => void;
}

const ClientContext = createContext<ClientContextType | undefined>(undefined);

export const ClientProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [clients, setClients] = useState<Client[]>([]);
    const [selectedClient, setSelectedClient] = useState<Client | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const { isAuthenticated } = useAuth();

    const loadClients = useCallback(async () => {
        if (!isAuthenticated) {
            setError('Authentication required');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await clientService.getAllClients();
            setClients(response.content);
            
            // Restore selected client if it exists in the new client list
            const savedClientId = localStorage.getItem('selectedClientId');
            if (savedClientId) {
                const savedClient = response.content.find(c => c.id === parseInt(savedClientId));
                if (savedClient) {
                    setSelectedClient(savedClient);
                }
            }
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to load clients';
            setError(`Failed to load clients: ${errorMessage}`);
            
            // Only clear client data on authentication errors
            if (err.response?.status === 401 || err.response?.status === 403) {
                setClients([]);
                setSelectedClient(null);
                localStorage.removeItem('selectedClientId');
            }
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated]);

    const selectClient = useCallback((client: Client | null) => {
        setSelectedClient(client);
        if (client) {
            localStorage.setItem('selectedClientId', client.id.toString());
        } else {
            localStorage.removeItem('selectedClientId');
        }
    }, []);

    useEffect(() => {
        if (isAuthenticated) {
            loadClients();
        }
    }, [isAuthenticated, loadClients]);

    return (
        <ClientContext.Provider value={{
            clients,
            selectedClient,
            error,
            loading,
            loadClients,
            selectClient
        }}>
            {children}
        </ClientContext.Provider>
    );
};

export const useClients = () => {
    const context = useContext(ClientContext);
    if (context === undefined) {
        throw new Error('useClients must be used within a ClientProvider');
    }
    return context;
}; 