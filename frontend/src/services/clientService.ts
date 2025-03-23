import axios from 'axios';
import { Client, Interface, PageResponse } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const clientService = {
  getAllClients: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    nameFilter?: string,
    statusFilter?: string
  ): Promise<PageResponse<Client>> => {
    // Build query parameters
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
    return response.data;
  },

  getClientById: async (id: number): Promise<Client> => {
    const response = await axios.get<Client>(`${API_URL}/clients/${id}`);
    return response.data;
  },

  createClient: async (clientData: Omit<Client, 'id'>): Promise<Client> => {
    const response = await axios.post<Client>(`${API_URL}/clients`, clientData);
    return response.data;
  },

  updateClient: async (id: number, clientData: Partial<Client>): Promise<Client> => {
    const response = await axios.put<Client>(`${API_URL}/clients/${id}`, clientData);
    return response.data;
  },

  deleteClient: async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/clients/${id}`);
  },

  getClientInterfaces: async (id: number): Promise<Interface[]> => {
    const response = await axios.get<Interface[]>(`${API_URL}/clients/${id}/interfaces`);
    return response.data;
  },

  onboardNewClient: async (clientData: Omit<Client, 'id'>): Promise<Client> => {
    const response = await axios.post<Client>(`${API_URL}/client-onboarding`, clientData);
    return response.data;
  },

  cloneClientConfiguration: async (sourceClientId: number, newClientData: Omit<Client, 'id'>): Promise<Client> => {
    const response = await axios.post<Client>(
      `${API_URL}/client-onboarding/clone/${sourceClientId}`, 
      newClientData
    );
    return response.data;
  },

  searchClients: async (
    name: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Client>> => {
    let params = new URLSearchParams();
    params.append('name', name);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Client>>(`${API_URL}/clients/search?${params.toString()}`);
    return response.data;
  },

  getClientsByStatus: async (
    status: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Client>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Client>>(`${API_URL}/clients/status/${status}?${params.toString()}`);
    return response.data;
  }
}; 