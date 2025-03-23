import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const interfaceService = {
    getAllInterfaces: async () => {
        const response = await axios.get(`${API_URL}/interfaces`);
        return response.data;
    },

    getInterfaceById: async (id) => {
        const response = await axios.get(`${API_URL}/interfaces/${id}`);
        return response.data;
    },

    createInterface: async (interfaceData) => {
        const response = await axios.post(`${API_URL}/interfaces`, interfaceData);
        return response.data;
    },

    updateInterface: async (id, interfaceData) => {
        const response = await axios.put(`${API_URL}/interfaces/${id}`, interfaceData);
        return response.data;
    },

    deleteInterface: async (id) => {
        await axios.delete(`${API_URL}/interfaces/${id}`);
    },

    getInterfaceMappings: async (id) => {
        const response = await axios.get(`${API_URL}/interfaces/${id}/mappings`);
        return response.data;
    },

    updateInterfaceMappings: async (id, mappings) => {
        const response = await axios.put(`${API_URL}/interfaces/${id}/mappings`, mappings);
        return response.data;
    }
}; 