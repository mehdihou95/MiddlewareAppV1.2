import api from '../config/apiConfig';
import { Client, Interface, PageResponse } from '../types';
import { handleApiError } from '../utils/errorHandler';
import { setClientContext } from '../utils/clientContext';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface ClientResponse {
    content: Client[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const clientService = {
    getAllClients: async (page = 0, size = 10, sortBy = 'name', direction = 'asc'): Promise<ClientResponse> => {
        const response = await api.get<ClientResponse>('/clients', {
            params: {
                page,
                size,
                sortBy,
                direction
            }
        });
        return response.data;
    },

    getClientById: async (id: number): Promise<Client> => {
        const response = await api.get<Client>(`/clients/${id}`);
        return response.data;
    },

    createClient: async (client: Omit<Client, 'id' | 'createdAt' | 'updatedAt'>): Promise<Client> => {
        const response = await api.post<Client>('/clients', client);
        return response.data;
    },

    updateClient: async (id: number, client: Partial<Client>): Promise<Client> => {
        const response = await api.put<Client>(`/clients/${id}`, client);
        return response.data;
    },

    deleteClient: async (id: number): Promise<void> => {
        await api.delete(`/clients/${id}`);
    },

    getClientInterfaces: async (id: number): Promise<Interface[]> => {
        try {
            setClientContext(id);
            const response = await api.get<Interface[]>(`/clients/${id}/interfaces`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    onboardNewClient: async (clientData: Omit<Client, 'id'>): Promise<Client> => {
        try {
            const response = await api.post<Client>(`/client-onboarding/new`, clientData);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    cloneClientConfiguration: async (sourceClientId: number, newClientData: Omit<Client, 'id'>): Promise<Client> => {
        try {
            setClientContext(sourceClientId);
            const response = await api.post<Client>(
                `/client-onboarding/clone/${sourceClientId}`, 
                newClientData
            );
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    searchClients: async (
        name: string,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'name',
        direction: string = 'asc'
    ): Promise<PageResponse<Client>> => {
        try {
            let params = new URLSearchParams();
            params.append('page', page.toString());
            params.append('size', size.toString());
            params.append('sortBy', sortBy);
            params.append('direction', direction);
            params.append('nameFilter', name);
            
            const response = await api.get<PageResponse<Client>>(`/clients?${params.toString()}`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    getClientsByStatus: async (
        status: string,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'name',
        direction: string = 'asc'
    ): Promise<PageResponse<Client>> => {
        try {
            let params = new URLSearchParams();
            params.append('page', page.toString());
            params.append('size', size.toString());
            params.append('sortBy', sortBy);
            params.append('direction', direction);
            params.append('statusFilter', status);
            
            const response = await api.get<PageResponse<Client>>(`/clients?${params.toString()}`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    }
}; 