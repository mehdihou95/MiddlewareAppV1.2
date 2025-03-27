import React, { createContext, useContext, useState, useEffect } from 'react';
import { Client, Interface, PageResponse } from '../types';
import { clientService } from '../services/clientService';
import { interfaceService } from '../services/interfaceService';
import { authService } from '../services/authService';
import { tokenService } from '../services/tokenService';
import { useNavigate } from 'react-router-dom';

interface ClientInterfaceContextType {
  clients: Client[];
  interfaces: Interface[];
  selectedClient: Client | null;
  selectedInterface: Interface | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
  userRoles: string[];
  refreshClients: (page?: number, pageSize?: number, sortField?: string, sortOrder?: 'asc' | 'desc') => Promise<void>;
  refreshInterfaces: () => Promise<void>;
  setSelectedClient: (client: Client) => void;
  setSelectedInterface: (interface_: Interface) => void;
  hasRole: (role: string) => boolean;
  setError: (error: string | null) => void;
  setClients: (clients: Client[]) => void;
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
  const navigate = useNavigate();

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
        const token = tokenService.getAccessToken();
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
            const response = await clientService.getAllClients({ page: 0, size: 10, sort: 'name', direction: 'asc' });
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
          tokenService.clearTokens(); // Clear invalid token
        }
      } catch (error) {
        console.error('Auth check error:', error);
        setIsAuthenticated(false);
        clearAllData();
        tokenService.clearTokens(); // Clear token on error
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
    if (!isAuthenticated) return;
    
    try {
      setLoading(true);
      setError(null); // Clear error before making the request
      const response = await clientService.getAllClients({
        page,
        size: pageSize,
        sort: sortField,
        direction: sortOrder
      });
      setClients(response.content);
    } catch (err: any) {
      console.error('Error loading clients:', err);
      
      // Only clear data on authentication errors
      if (err.response?.status === 401 || err.response?.status === 403) {
        clearAllData();
        tokenService.clearTokens();
        navigate('/login');
      } else {
        // For other errors, just show error message but keep existing data
        setError('Failed to load clients. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, navigate]);

  const refreshInterfaces = async () => {
    if (!selectedClient) return;
    try {
      setLoading(true);
      const response = await clientService.getClientInterfaces(selectedClient.id);
      setInterfaces(response);
    } catch (err) {
      setError('Failed to load interfaces');
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

  const value: ClientInterfaceContextType = {
    clients,
    interfaces,
    selectedClient,
    selectedInterface,
    loading,
    error,
    isAuthenticated,
    userRoles,
    refreshClients,
    refreshInterfaces,
    setSelectedClient,
    setSelectedInterface,
    hasRole,
    setError,
    setClients
  };

  return (
    <ClientInterfaceContext.Provider value={value}>
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