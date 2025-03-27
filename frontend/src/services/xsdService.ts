import { api } from './apiService';
import { handleApiError } from '../utils/errorHandler';
import { MappingRule } from '../types';

export interface XsdElement {
    name: string;
    type: string;
    required: boolean;
    children?: XsdElement[];
    attributes?: XsdAttribute[];
}

export interface XsdAttribute {
    name: string;
    type: string;
    required: boolean;
}

export const xsdService = {
    getXsdStructure: async (xsdPath: string): Promise<XsdElement[]> => {
        try {
            const response = await api.get<XsdElement[]>(`/mapping/xsd-structure?xsdPath=${encodeURIComponent(xsdPath)}`);
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    },

    getAllMappingRules: async (
        page: number = 0,
        size: number = 10,
        sortBy: string = 'id',
        direction: string = 'asc'
    ): Promise<{ content: MappingRule[]; totalElements: number; totalPages: number }> => {
        try {
            const response = await api.get<{ content: MappingRule[]; totalElements: number; totalPages: number }>(
                `/mapping/rules?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`
            );
            return response.data;
        } catch (error) {
            throw handleApiError(error);
        }
    }
}; 