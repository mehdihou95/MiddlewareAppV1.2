import api from '../config/apiConfig';
import { Client, ClientInput, Interface, PageResponse } from '../types';
import { handleApiError } from '../utils/errorHandler';
import { setClientContext } from '../utils/clientContext';

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
    getAllClients: async (page = 0, size = 10, sortBy = 'name', direction = 'asc'): Promise<ClientResponse> => {
        try {
            console.log('Fetching clients with params:', { page, size, sortBy, direction });
            const response = await api.get<ClientResponse>('/clients', {
                params: {
                    page,
                    size,
                    sort: `${sortBy},${direction}`
                }
            });
            console.log('Client response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching clients:', error);
            throw error;
        }
    },

    getClientById: async (id: number): Promise<Client> => {
        const response = await api.get<Client>(`/clients/${id}`);
        return response.data;
    },

    createClient: async (client: ClientInput): Promise<Client> => {
        const response = await api.post<Client>('/clients', client);
        return response.data;
    },

    updateClient: async (id: number, client: ClientInput): Promise<Client> => {
        const response = await api.put<Client>(`/clients/${id}`, client);
        return response.data;
    },

    deleteClient: async (id: number): Promise<void> => {
        await api.delete(`/clients/${id}`);
    },

    getClientInterfaces: async (id: number): Promise<Interface[]> => {
        try {
            const response = await api.get<Interface[]>(`/clients/${id}/interfaces`);
            return response.data;
        } catch (error) {
            console.error('Error fetching client interfaces:', error);
            return [];
        }
    },

    onboardNewClient: async (clientData: ClientOnboardingData): Promise<Client> => {
        const response = await api.post<Client>(`/clients/onboarding/new`, clientData);
        return response.data;
    },

    cloneClient: async (
        sourceClientId: number,
        newClientData: ClientOnboardingData
    ): Promise<Client> => {
        const response = await api.post<Client>(
            `/clients/onboarding/clone/${sourceClientId}`,
            newClientData
        );
        return response.data;
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

        const response = await api.get<PageResponse<Client>>(`/clients?${params.toString()}`);
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

        const response = await api.get<PageResponse<Client>>(`/clients?${params.toString()}`);
        return response.data;
    }
}; 