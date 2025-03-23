import axios from 'axios';
import { User } from '../types';
import { PageResponse } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const userService = {
    getAllUsers: async (page: number = 0, size: number = 10, sortBy: string = 'username', direction: string = 'asc'): Promise<PageResponse<User>> => {
        const response = await axios.get<PageResponse<User>>(`${API_URL}/users`, {
            params: { page, size, sortBy, direction }
        });
        return response.data;
    },

    searchUsers: async (searchTerm: string, page: number = 0, size: number = 10, sortBy: string = 'username', direction: string = 'asc'): Promise<PageResponse<User>> => {
        const response = await axios.get<PageResponse<User>>(`${API_URL}/users/search`, {
            params: { searchTerm, page, size, sortBy, direction }
        });
        return response.data;
    },

    getUsersByStatus: async (enabled: boolean, page: number = 0, size: number = 10, sortBy: string = 'username', direction: string = 'asc'): Promise<PageResponse<User>> => {
        const response = await axios.get<PageResponse<User>>(`${API_URL}/users/status`, {
            params: { enabled, page, size, sortBy, direction }
        });
        return response.data;
    },

    getLockedUsers: async (page: number = 0, size: number = 10, sortBy: string = 'username', direction: string = 'asc'): Promise<PageResponse<User>> => {
        const response = await axios.get<PageResponse<User>>(`${API_URL}/users/locked`, {
            params: { page, size, sortBy, direction }
        });
        return response.data;
    },

    createUser: async (user: Omit<User, 'id'>): Promise<User> => {
        const response = await axios.post<User>(`${API_URL}/users`, user);
        return response.data;
    },

    updateUser: async (id: number, user: Partial<User>): Promise<User> => {
        const response = await axios.put<User>(`${API_URL}/users/${id}`, user);
        return response.data;
    },

    deleteUser: async (id: number): Promise<void> => {
        await axios.delete(`${API_URL}/users/${id}`);
    },

    changePassword: async (id: number, oldPassword: string, newPassword: string): Promise<void> => {
        await axios.post(`${API_URL}/users/${id}/change-password`, null, {
            params: { oldPassword, newPassword }
        });
    },

    resetPassword: async (email: string): Promise<void> => {
        await axios.post(`${API_URL}/users/reset-password`, null, {
            params: { email }
        });
    },

    unlockAccount: async (id: number): Promise<void> => {
        await axios.post(`${API_URL}/users/${id}/unlock`);
    }
}; 