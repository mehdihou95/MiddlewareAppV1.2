import axios from 'axios';
import { ProcessedFile, PageResponse } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const processedFileService = {
  getAllProcessedFiles: async (
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc',
    fileNameFilter?: string,
    statusFilter?: string,
    startDate?: string,
    endDate?: string
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    if (fileNameFilter) {
      params.append('fileNameFilter', fileNameFilter);
    }
    
    if (statusFilter) {
      params.append('statusFilter', statusFilter);
    }
    
    if (startDate) {
      params.append('startDate', startDate);
    }
    
    if (endDate) {
      params.append('endDate', endDate);
    }
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files?${params.toString()}`);
    return response.data;
  },

  getProcessedFileById: async (id: number): Promise<ProcessedFile> => {
    const response = await axios.get<ProcessedFile>(`${API_URL}/processed-files/${id}`);
    return response.data;
  },

  createProcessedFile: async (processedFileData: Omit<ProcessedFile, 'id'>): Promise<ProcessedFile> => {
    const response = await axios.post<ProcessedFile>(`${API_URL}/processed-files`, processedFileData);
    return response.data;
  },

  updateProcessedFile: async (id: number, processedFileData: Partial<ProcessedFile>): Promise<ProcessedFile> => {
    const response = await axios.put<ProcessedFile>(`${API_URL}/processed-files/${id}`, processedFileData);
    return response.data;
  },

  deleteProcessedFile: async (id: number): Promise<void> => {
    await axios.delete(`${API_URL}/processed-files/${id}`);
  },

  getProcessedFilesByClient: async (
    clientId: number,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/client/${clientId}?${params.toString()}`);
    return response.data;
  },

  searchProcessedFiles: async (
    fileName: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('fileName', fileName);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/search?${params.toString()}`);
    return response.data;
  },

  getProcessedFilesByStatus: async (
    status: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/status/${status}?${params.toString()}`);
    return response.data;
  },

  getProcessedFilesByDateRange: async (
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('startDate', startDate);
    params.append('endDate', endDate);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/date-range?${params.toString()}`);
    return response.data;
  },

  getProcessedFilesByClientAndStatus: async (
    clientId: number,
    status: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/client/${clientId}/status/${status}?${params.toString()}`);
    return response.data;
  },

  getProcessedFilesByClientAndDateRange: async (
    clientId: number,
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'processedDate',
    direction: string = 'desc'
  ): Promise<PageResponse<ProcessedFile>> => {
    let params = new URLSearchParams();
    params.append('startDate', startDate);
    params.append('endDate', endDate);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sortBy', sortBy);
    params.append('direction', direction);
    
    const response = await axios.get<PageResponse<ProcessedFile>>(`${API_URL}/processed-files/client/${clientId}/date-range?${params.toString()}`);
    return response.data;
  }
}; 