import axios from 'axios';
import { Interface, PageResponse } from '../types';
import { handleApiError } from '../utils/errorHandler';
import { setClientContext } from '../utils/clientContext';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const interfaceService = {
  getAllInterfaces: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    searchTerm?: string,
    isActive?: boolean,
    clientId?: number
  ): Promise<PageResponse<Interface>> => {
    try {
      let params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sortBy', sortBy);
      params.append('direction', direction);
      
      if (searchTerm) {
        params.append('searchTerm', searchTerm);
      }
      
      if (isActive !== undefined) {
        params.append('isActive', isActive.toString());
      }

      // Set client context if provided
      if (clientId) {
        setClientContext(clientId);
      }
      
      const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces`, { params });
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
      throw handleApiError(error);
    }
  },

  getInterfaceById: async (id: number, clientId?: number): Promise<Interface> => {
    try {
      if (clientId) {
        setClientContext(clientId);
      }
      const response = await axios.get<Interface>(`${API_URL}/interfaces/${id}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  createInterface: async (interfaceData: Omit<Interface, 'id'>, clientId?: number): Promise<Interface> => {
    try {
      if (clientId) {
        setClientContext(clientId);
      }
      const response = await axios.post<Interface>(`${API_URL}/interfaces`, interfaceData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  updateInterface: async (id: number, interfaceData: Partial<Interface>, clientId?: number): Promise<Interface> => {
    try {
      if (clientId) {
        setClientContext(clientId);
      }
      const response = await axios.put<Interface>(`${API_URL}/interfaces/${id}`, interfaceData);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  deleteInterface: async (id: number, clientId?: number): Promise<void> => {
    try {
      if (clientId) {
        setClientContext(clientId);
      }
      await axios.delete(`${API_URL}/interfaces/${id}`);
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getInterfacesByClient: async (
    clientId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<Interface>> => {
    try {
      setClientContext(clientId);
      let params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sortBy', sortBy);
      params.append('sortDirection', direction);
      
      const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/client?${params.toString()}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  getInterfacesByStatus: async (
    isActive: boolean,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc',
    clientId?: number
  ): Promise<PageResponse<Interface>> => {
    try {
      if (clientId) {
        setClientContext(clientId);
      }
      let params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', size.toString());
      params.append('sortBy', sortBy);
      params.append('sortDirection', direction);
      
      const response = await axios.get<PageResponse<Interface>>(`${API_URL}/interfaces/status/${isActive}?${params.toString()}`);
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
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
    const response = await axios.get<Interface[]>(`${API_URL}/interfaces/client`);
    return response.data;
  }
}; 