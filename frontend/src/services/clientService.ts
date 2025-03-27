import { api } from '../services/apiService';
import { handleApiError } from '../utils/errorHandler';
import { createPaginationParams, PaginationParams } from '../utils/paginationUtils';
import { Client, ClientInput, Interface, PageResponse } from '../types';
import { setClientContext } from '../utils/clientContext';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

interface ClientResponse {
    content: Client[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

interface ClientOnboardingData {
    name: string;
    description?: string;
    active?: boolean;
}

export const clientService = {
    getAllClients: async (params: PaginationParams): Promise<ClientResponse> => {
        try {
            const searchParams = createPaginationParams(params);
            const response = await api.get<ClientResponse>(`${API_URL}/clients`, { 
                params: searchParams 
            });
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    getClientById: async (id: number): Promise<Client> => {
        try {
            const response = await api.get<Client>(`${API_URL}/clients/${id}`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    createClient: async (client: Omit<Client, 'id'>): Promise<Client> => {
        console.log('ClientService: Attempting to create client:', client);
        try {
            console.log('ClientService: Making POST request to:', `${API_URL}/clients`);
            console.log('ClientService: Request payload:', client);
            
            const response = await api.post<Client>(`${API_URL}/clients`, client, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            console.log('ClientService: Create client response:', {
                status: response.status,
                headers: response.headers,
                data: response.data
            });
            
            if (!response.data) {
                throw new Error('No data received from server');
            }
            
            return response.data;
        } catch (error: any) {
            console.error('ClientService: Error creating client:', {
                error,
                response: error.response?.data,
                status: error.response?.status,
                headers: error.response?.headers
            });
            
            // Log the request that was made
            if (error.config) {
                console.log('ClientService: Failed request details:', {
                    url: error.config.url,
                    method: error.config.method,
                    headers: error.config.headers,
                    data: error.config.data
                });
            }
            
            throw handleApiError(error);
        }
    },

    updateClient: async (id: number, client: Partial<Client>): Promise<Client> => {
        try {
            const response = await api.put<Client>(`${API_URL}/clients/${id}`, client);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    deleteClient: async (id: number): Promise<void> => {
        try {
            await api.delete(`${API_URL}/clients/${id}`);
        } catch (error) {
            throw handleApiError(error);
        }
    },

    getClientInterfaces: async (id: number): Promise<Interface[]> => {
        try {
            const response = await api.get<Interface[]>(`${API_URL}/clients/${id}/interfaces`);
            return response.data;
        } catch (error) {
            console.error('Error fetching client interfaces:', error);
            return [];
        }
    },

    onboardNewClient: async (clientData: ClientInput): Promise<Client> => {
        try {
            const response = await api.post<Client>('/clients/onboarding/new', clientData);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    cloneClientConfiguration: async (sourceClientId: number, newClientData: ClientInput): Promise<Client> => {
        try {
            const response = await api.post<Client>(`/clients/onboarding/clone/${sourceClientId}`, newClientData);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    searchClients: async (
        searchTerm: string,
        page = 0,
        size = 10,
        sortBy = 'name',
        direction = 'asc'
    ): Promise<PageResponse<Client>> => {
        const params = new URLSearchParams({
            search: searchTerm,
            page: page.toString(),
            size: size.toString(),
            sort: `${sortBy},${direction}`
        });

        const response = await api.get<PageResponse<Client>>(`${API_URL}/clients?${params.toString()}`);
        return response.data;
    },

    getClientsByStatus: async (
        active: boolean,
        page = 0,
        size = 10,
        sortBy = 'name',
        direction = 'asc'
    ): Promise<PageResponse<Client>> => {
        const params = new URLSearchParams({
            active: active.toString(),
            page: page.toString(),
            size: size.toString(),
            sort: `${sortBy},${direction}`
        });

        const response = await api.get<PageResponse<Client>>(`${API_URL}/clients?${params.toString()}`);
        return response.data;
    }
}; 