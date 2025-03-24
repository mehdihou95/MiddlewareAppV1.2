import axios from 'axios';
import { Client, Interface, PageResponse } from '../types';
import { handleApiError } from '../utils/errorHandler';
import { setClientContext } from '../utils/clientContext';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Add axios interceptors for logging
axios.interceptors.request.use(request => {
  console.log('Starting Request:', {
    url: request.url,
    method: request.method,
    data: request.data,
    headers: request.headers
  });
  return request;
});

axios.interceptors.response.use(
  response => {
    console.log('Response:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    });
    return response;
  },
  error => {
    console.error('API Error:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });
    return Promise.reject(error);
  }
);

export const clientService = {
  getAllClients: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    nameFilter?: string,
    statusFilter?: string
  ): Promise<PageResponse<Client>> => {
    try {
      let params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sortBy', sortBy);
      params.append('direction', direction);
      
      if (nameFilter) {
        params.append('nameFilter', nameFilter);
      }
      
      if (statusFilter) {
        params.append('statusFilter', statusFilter);
      }
      
      const response = await axios.get<PageResponse<Client>>(`${API_URL}/clients?${params.toString()}`);
      const data = response.data;
      
      return {
        content: data.content || [],
        pageable: {
          sort: data.sort || { sorted: false, unsorted: true, empty: true },
          pageNumber: data.number || 0,
          pageSize: data.size || size,
          offset: (data.number || 0) * (data.size || size),
          paged: true,
          unpaged: false
        },
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        last: data.last || false,
        first: data.first || false,
        sort: data.sort || { sorted: false, unsorted: true, empty: true },
        numberOfElements: data.numberOfElements || 0,
        size: data.size || size,
        number: data.number || 0,
        empty: data.empty || false
      };
    } catch (error) {
      console.error('Error fetching clients:', error);
      throw handleApiError(error);
    }
  },

  getClientById: async (id: number): Promise<Client> => {
    try {
      setClientContext(id);
      const response = await axios.get<Client>(`${API_URL}/clients/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createClient: async (clientData: Omit<Client, 'id'>): Promise<Client> => {
    try {
      const response = await axios.post<Client>(`${API_URL}/clients`, clientData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateClient: async (id: number, clientData: Partial<Client>): Promise<Client> => {
    try {
      setClientContext(id);
      const response = await axios.put<Client>(`${API_URL}/clients/${id}`, clientData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteClient: async (id: number): Promise<void> => {
    try {
      setClientContext(id);
      await axios.delete(`${API_URL}/clients/${id}`);
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getClientInterfaces: async (id: number): Promise<Interface[]> => {
    try {
      setClientContext(id);
      const response = await axios.get<Interface[]>(`${API_URL}/clients/${id}/interfaces`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  onboardNewClient: async (clientData: Omit<Client, 'id'>): Promise<Client> => {
    try {
      const response = await axios.post<Client>(`${API_URL}/client-onboarding/new`, clientData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  cloneClientConfiguration: async (sourceClientId: number, newClientData: Omit<Client, 'id'>): Promise<Client> => {
    try {
      setClientContext(sourceClientId);
      const response = await axios.post<Client>(
        `${API_URL}/client-onboarding/clone/${sourceClientId}`, 
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
      
      const response = await axios.get<PageResponse<Client>>(`${API_URL}/clients?${params.toString()}`);
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
      
      const response = await axios.get<PageResponse<Client>>(`${API_URL}/clients?${params.toString()}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  }
}; 