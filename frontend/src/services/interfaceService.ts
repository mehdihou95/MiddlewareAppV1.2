import axios from 'axios';
import { Interface, PageResponse } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const interfaceService = {
  getAllInterfaces: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    nameFilter?: string,
    typeFilter?: string,
    isActiveFilter?: boolean
  ): Promise<PageResponse<Interface>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    if (nameFilter) {
      params.append('nameFilter', nameFilter);
    }
    
    if (typeFilter) {
      params.append('typeFilter', typeFilter);
    }
    
    if (isActiveFilter !== undefined) {
      params.append('isActiveFilter', isActiveFilter.toString());
    }
    
    const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces?${params.toString()}`);
    return response.data;
  },

  getInterfaceById: async (id: number): Promise<Interface> => {
    const response = await axios.get<Interface>(`${API_URL}/interfaces/${id}`);
    return response.data;
  },

  createInterface: async (interfaceData: Omit<Interface, 'id'>): Promise<Interface> => {
    const response = await axios.post<Interface>(`${API_URL}/interfaces`, interfaceData);
    return response.data;
  },

  updateInterface: async (id: number, interfaceData: Partial<Interface>): Promise<Interface> => {
    const response = await axios.put<Interface>(`${API_URL}/interfaces/${id}`, interfaceData);
    return response.data;
  },

  deleteInterface: async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/interfaces/${id}`);
  },

  getInterfacesByClient: async (
    clientId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Interface>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/client/${clientId}?${params.toString()}`);
    return response.data;
  },

  searchInterfaces: async (
    name: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Interface>> => {
    let params = new URLSearchParams();
    params.append('name', name);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/search?${params.toString()}`);
    return response.data;
  },

  getInterfacesByType: async (
    type: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Interface>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/type/${type}?${params.toString()}`);
    return response.data;
  },

  getInterfacesByStatus: async (
    isActive: boolean,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Interface>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/status/${isActive}?${params.toString()}`);
    return response.data;
  },

  getInterfaceMappings: async (id: number): Promise<any[]> => {
    const response = await axios.get<any[]>(`${API_URL}/interfaces/${id}/mappings`);
    return response.data;
  },

  updateInterfaceMappings: async (id: number, mappings: any[]): Promise<any[]> => {
    const response = await axios.put<any[]>(
      `${API_URL}/interfaces/${id}/mappings`,
      mappings,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    return response.data;
  },
  
  getInterfacesByClientId: async (clientId: number): Promise<Interface[]> => {
    const response = await axios.get<Interface[]>(
      `${API_URL}/clients/${clientId}/interfaces`,
      {
        headers: {
          'X-Client-ID': clientId.toString()
        }
      }
    );
    return response.data;
  }
}; 