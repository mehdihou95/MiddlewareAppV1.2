import React, { createContext, useState, useContext, useEffect } from 'react';
import { Client, Interface } from '../types';
import { clientService } from '../services/clientService';
import { interfaceService } from '../services/interfaceService';

interface ClientInterfaceContextType {
  clients: Client[];
  interfaces: Interface[];
  selectedClient: Client | null;
  selectedInterface: Interface | null;
  setSelectedClient: (client: Client | null) => void;
  setSelectedInterface: (interfaceObj: Interface | null) => void;
  loading: boolean;
  error: string | null;
  refreshClients: () => Promise<void>;
  refreshInterfaces: () => Promise<void>;
}

const ClientInterfaceContext = createContext<ClientInterfaceContextType | null>(null);

export const ClientInterfaceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [clients, setClients] = useState<Client[]>([]);
  const [interfaces, setInterfaces] = useState<Interface[]>([]);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [selectedInterface, setSelectedInterface] = useState<Interface | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Load clients on component mount
  useEffect(() => {
    refreshClients();
  }, []);

  // Load interfaces when client is selected
  useEffect(() => {
    if (selectedClient) {
      refreshInterfaces();
    } else {
      setInterfaces([]);
      setSelectedInterface(null);
    }
  }, [selectedClient]);

  // Load from localStorage on mount
  useEffect(() => {
    const savedClientId = localStorage.getItem('selectedClientId');
    const savedInterfaceId = localStorage.getItem('selectedInterfaceId');
    
    if (savedClientId) {
      const loadSavedSelections = async () => {
        try {
          const client = await clientService.getClientById(parseInt(savedClientId));
          setSelectedClient(client);
          
          if (savedInterfaceId) {
            const interfaceObj = await interfaceService.getInterfaceById(parseInt(savedInterfaceId));
            if (interfaceObj.clientId === client.id) {
              setSelectedInterface(interfaceObj);
            }
          }
        } catch (error) {
          console.error('Error loading saved selections:', error);
          // Clear invalid saved selections
          localStorage.removeItem('selectedClientId');
          localStorage.removeItem('selectedInterfaceId');
        }
      };
      
      loadSavedSelections();
    }
  }, []);

  // Save to localStorage when selections change
  useEffect(() => {
    if (selectedClient) {
      localStorage.setItem('selectedClientId', selectedClient.id.toString());
    } else {
      localStorage.removeItem('selectedClientId');
    }
    
    if (selectedInterface) {
      localStorage.setItem('selectedInterfaceId', selectedInterface.id.toString());
    } else {
      localStorage.removeItem('selectedInterfaceId');
    }
  }, [selectedClient, selectedInterface]);

  const refreshClients = async () => {
    setLoading(true);
    try {
      const response = await clientService.getAllClients(0, 100); // Get first 100 clients for dropdown
      setClients(response.content || []);
      setError(null);
      
      // If selected client is not in the list anymore, clear selection
      if (selectedClient && !(response.content || []).find(c => c.id === selectedClient.id)) {
        setSelectedClient(null);
        setSelectedInterface(null);
        localStorage.removeItem('selectedClientId');
        localStorage.removeItem('selectedInterfaceId');
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to load clients';
      setError(`Failed to load clients: ${errorMessage}`);
      console.error('Error loading clients:', err);
      
      // Only clear data on authentication errors
      if (err.response?.status === 401 || err.response?.status === 403) {
        setClients([]);
        setSelectedClient(null);
        setSelectedInterface(null);
        localStorage.removeItem('selectedClientId');
        localStorage.removeItem('selectedInterfaceId');
      }
    } finally {
      setLoading(false);
    }
  };

  const refreshInterfaces = async () => {
    if (!selectedClient) return;
    
    setLoading(true);
    try {
      const data = await clientService.getClientInterfaces(selectedClient.id);
      setInterfaces(data);
      setError(null);
      
      // If selected interface is not in the list anymore, clear selection
      if (selectedInterface && !data.find(i => i.id === selectedInterface.id)) {
        setSelectedInterface(null);
        localStorage.removeItem('selectedInterfaceId');
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to load interfaces';
      setError(`Failed to load interfaces: ${errorMessage}`);
      console.error('Error loading interfaces:', err);
      
      // Only clear interface data on authentication errors
      if (err.response?.status === 401 || err.response?.status === 403) {
        setInterfaces([]);
        setSelectedInterface(null);
        localStorage.removeItem('selectedInterfaceId');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSetSelectedClient = (client: Client | null) => {
    setSelectedClient(client);
    setSelectedInterface(null); // Reset interface when client changes
  };

  return (
    <ClientInterfaceContext.Provider
      value={{
        clients,
        interfaces,
        selectedClient,
        selectedInterface,
        setSelectedClient: handleSetSelectedClient,
        setSelectedInterface,
        loading,
        error,
        refreshClients,
        refreshInterfaces
      }}
    >
      {children}
    </ClientInterfaceContext.Provider>
  );
};

export const useClientInterface = () => {
  const context = useContext(ClientInterfaceContext);
  if (!context) {
    throw new Error('useClientInterface must be used within a ClientInterfaceProvider');
  }
  return context;
}; 