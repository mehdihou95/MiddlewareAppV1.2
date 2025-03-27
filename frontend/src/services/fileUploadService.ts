import axios from 'axios';
import { ProcessedFile } from '../types';
import { handleApiError } from '../utils/errorHandler';
import { clientService } from './clientService';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

class FileUploadService {
  validateFileSize(file: File): boolean {
    return file.size <= MAX_FILE_SIZE;
  }

  validateFileType(file: File): boolean {
    return file.type === 'text/xml' || file.name.toLowerCase().endsWith('.xml');
  }

  async verifyClientAndInterface(clientId: number, interfaceId: number): Promise<boolean> {
    try {
      // Verify client exists and is active
      const client = await clientService.getClientById(clientId);
      if (!client || client.status !== 'ACTIVE') {
        throw new Error('Invalid or inactive client');
      }

      // Verify interface exists and belongs to the client
      const interfaces = await clientService.getClientInterfaces(clientId);
      const targetInterface = interfaces.find(i => i.id === interfaceId);
      if (!targetInterface || !targetInterface.isActive) {
        throw new Error('Invalid or inactive interface for this client');
      }

      return true;
    } catch (error) {
      throw handleApiError(error);
    }
  }

  async uploadFile(file: File, clientId: number, interfaceId: number): Promise<ProcessedFile> {
    try {
      // First verify client and interface
      await this.verifyClientAndInterface(clientId, interfaceId);

      // Validate file
      if (!this.validateFileSize(file)) {
        throw new Error('File size exceeds maximum limit of 10MB');
      }
      if (!this.validateFileType(file)) {
        throw new Error('Invalid file type. Only XML files are allowed');
      }

      const formData = new FormData();
      formData.append('file', file);
      formData.append('clientId', clientId.toString());
      formData.append('interfaceId', interfaceId.toString());

      const response = await axios.post<ProcessedFile>(
        `${API_URL}/files/upload/${interfaceId}`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  }
}

export const fileUploadService = new FileUploadService(); 