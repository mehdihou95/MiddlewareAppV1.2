import axios from 'axios';
import { ProcessedFile } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

class FileUploadService {
  validateFileSize(file: File): boolean {
    return file.size <= MAX_FILE_SIZE;
  }

  validateFileType(file: File): boolean {
    return file.type === 'text/xml' || file.name.toLowerCase().endsWith('.xml');
  }

  async uploadFile(file: File, clientId: number, interfaceId: number): Promise<ProcessedFile> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('clientId', clientId.toString());
    formData.append('interfaceId', interfaceId.toString());

    const response = await axios.post<ProcessedFile>(
      `${API_URL}/files/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );

    return response.data;
  }
}

export const fileUploadService = new FileUploadService(); 