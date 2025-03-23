import axios from 'axios';
import { MappingRule, PageResponse } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const mappingRuleService = {
  getAllMappingRules: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<MappingRule>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);

    const response = await axios.get<PageResponse<MappingRule>>(`${API_URL}/mapping-rules?${params.toString()}`);
    return response.data;
  },

  getMappingRuleById: async (id: number): Promise<MappingRule> => {
    const response = await axios.get<MappingRule>(`${API_URL}/mapping-rules/${id}`);
    return response.data;
  },

  createMappingRule: async (mappingRuleData: Omit<MappingRule, 'id'>): Promise<MappingRule> => {
    const response = await axios.post<MappingRule>(`${API_URL}/mapping-rules`, mappingRuleData);
    return response.data;
  },

  updateMappingRule: async (id: number, mappingRuleData: Partial<MappingRule>): Promise<MappingRule> => {
    const response = await axios.put<MappingRule>(`${API_URL}/mapping-rules/${id}`, mappingRuleData);
    return response.data;
  },

  deleteMappingRule: async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/mapping-rules/${id}`);
  },

  getMappingRulesByInterface: async (
    interfaceId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'asc'
  ): Promise<PageResponse<MappingRule>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);

    const response = await axios.get<PageResponse<MappingRule>>(
      `${API_URL}/interfaces/${interfaceId}/mapping-rules?${params.toString()}`
    );
    return response.data;
  }
}; 