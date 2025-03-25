import React, { createContext, useContext, useState, useEffect } from 'react';
import { Client, Interface } from '../types';
import { clientService } from '../services/clientService';
import { interfaceService } from '../services/interfaceService';
import { authService } from '../services/authService';
import { tokenManager } from '../utils/tokenManager';

interface ClientInterfaceContextType {
  clients: Client[];
  interfaces: Interface[];
  selectedClient: Client | null;
  selectedInterface: Interface | null;
  setSelectedClient: (client: Client | null) => void;
  setSelectedInterface: (interfaceObj: Interface | null) => void;
  loading: boolean;
  error: string | null;
  setError: (error: string | null) => void;
  refreshClients: (page?: number, pageSize?: number, sortField?: string, sortOrder?: 'asc' | 'desc') => Promise<any>;
  refreshInterfaces: () => Promise<void>;
  userRoles: string[];
  hasRole: (role: string) => boolean;
  isAuthenticated: boolean;
}

const ClientInterfaceContext = createContext<ClientInterfaceContextType | undefined>(undefined);

export const ClientInterfaceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [clients, setClients] = useState<Client[]>([]);
  const [interfaces, setInterfaces] = useState<Interface[]>([]);
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);
  const [selectedInterface, setSelectedInterface] = useState<Interface | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userRoles, setUserRoles] = useState<string[]>([]);
  const requestRef = React.useRef<AbortController | null>(null);

  const clearAllData = () => {
    setClients([]);
    setInterfaces([]);
    setSelectedClient(null);
    setSelectedInterface(null);
    setError(null);
  };

  const clearSavedSelections = () => {
    localStorage.removeItem('selectedClientId');
    localStorage.removeItem('selectedInterfaceId');
  };

  useEffect(() => {
    const checkAuth = async () => {
      console.log('Checking authentication...');
      try {
        // First check if we have a token
        const token = tokenManager.getToken();
        if (!token) {
          console.log('No token found, clearing data');
          setIsAuthenticated(false);
          clearAllData();
          return;
        }

        // Validate the token
        const authResult = await authService.validateToken();
        console.log('Token validation result:', authResult);
        
        if (authResult.valid) {
          console.log('Token is valid, setting authenticated state');
          setIsAuthenticated(true);
          setUserRoles(authResult.roles || []);
          console.log('User roles:', authResult.roles);
          
          // Load initial clients after successful authentication
          setLoading(true);
          try {
            console.log('Loading initial clients...');
            const response = await clientService.getAllClients(0, 10, 'name', 'asc');
            console.log('Initial clients loaded:', response);
            if (response && response.content) {
              setClients(response.content);
              setError(null);
            } else {
              console.warn('No clients found in response');
              setClients([]);
            }
          } catch (error: any) {
            console.error('Failed to load initial clients:', error);
            if (error.response?.status === 403) {
              setError('You do not have permission to access client data.');
            } else {
              setError('Failed to load initial clients');
            }
          } finally {
            setLoading(false);
          }
        } else {
          console.log('Token is invalid, clearing data');
          setIsAuthenticated(false);
          setUserRoles([]);
          clearAllData();
          tokenManager.clearToken(); // Clear invalid token
        }
      } catch (error) {
        console.error('Auth check error:', error);
        setIsAuthenticated(false);
        clearAllData();
        tokenManager.clearToken(); // Clear token on error
      }
    };

    // Run authentication check
    checkAuth();

    // Cleanup function
    return () => {
      if (requestRef.current) {
        requestRef.current.abort();
      }
    };
  }, []);

  const refreshClients = React.useCallback(async (
    page = 0,
    pageSize = 10,
    sortField = 'name',
    sortOrder: 'asc' | 'desc' = 'asc'
  ) => {
    if (!isAuthenticated) {
      console.log('Not authenticated, skipping client refresh');
      return;
    }

    if (loading) {
      console.log('Already loading, skipping client refresh');
      return;
    }

    // Cancel previous request if it exists
    if (requestRef.current) {
      console.log('Cancelling previous request');
      requestRef.current.abort();
    }
    requestRef.current = new AbortController();
    
    setLoading(true);
    try {
      console.log('Making request to get all clients...', { page, pageSize, sortField, sortOrder });
      const response = await clientService.getAllClients(page, pageSize, sortField, sortOrder);
      
      // Only proceed if this is still the active request
      if (requestRef.current?.signal.aborted) {
        console.log('Request was aborted, skipping update');
        return;
      }
      
      console.log('Client response received:', response);
      setClients(response.content || []);
      setError(null);
      return response;
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('Request aborted');
        return;
      }
      
      console.error('Error loading clients:', err);
      
      if (err.response?.status === 401) {
        console.log('Authentication failed, clearing data');
        setIsAuthenticated(false);
        clearAllData();
        setError('Authentication failed. Please log in again.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to access client data.');
      } else {
        const errorMessage = err.response?.data?.message || err.message || 'Failed to load clients';
        setError(`Failed to load clients: ${errorMessage}`);
      }
      throw err;
    } finally {
      if (requestRef.current?.signal.aborted === false) {
        setLoading(false);
        requestRef.current = null;
      }
    }
  }, [isAuthenticated, loading]);

  const refreshInterfaces = async () => {
    if (!isAuthenticated || !selectedClient) return;

    setLoading(true);
    try {
      const response = await interfaceService.getInterfacesByClientId(selectedClient.id);
      setInterfaces(response || []);
      setError(null);
    } catch (err: any) {
      if (err.response?.status === 401) {
        setIsAuthenticated(false);
        clearAllData();
        setError('Authentication failed. Please log in again.');
      } else if (err.response?.status === 403) {
        setError('You do not have permission to access interface data.');
      } else {
        const errorMessage = err.response?.data?.message || err.message || 'Failed to load interfaces';
        setError(`Failed to load interfaces: ${errorMessage}`);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSetSelectedClient = (client: Client | null) => {
    setSelectedClient(client);
    setSelectedInterface(null);
    if (client) {
      localStorage.setItem('selectedClientId', client.id.toString());
    } else {
      clearSavedSelections();
    }
  };

  // Add hasRole helper method
  const hasRole = (role: string): boolean => {
    return userRoles.includes(role);
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
        setError,
        refreshClients,
        refreshInterfaces,
        userRoles,
        hasRole,
        isAuthenticated
      }}
    >
      {children}
    </ClientInterfaceContext.Provider>
  );
};

export const useClientInterface = () => {
  const context = useContext(ClientInterfaceContext);
  if (context === undefined) {
    throw new Error('useClientInterface must be used within a ClientInterfaceProvider');
  }
  return context;
}; 