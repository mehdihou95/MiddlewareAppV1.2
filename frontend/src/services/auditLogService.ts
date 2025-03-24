import axios from 'axios';
import { PageResponse, AuditLog } from '../types';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export interface AuditLogQueryParams {
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: 'ASC' | 'DESC';
  startDate?: string;
  endDate?: string;
}

class AuditLogService {
  private getQueryString(params: AuditLogQueryParams): string {
    const searchParams = new URLSearchParams();
    
    if (params.page !== undefined && params.page !== null) searchParams.append('page', params.page.toString());
    if (params.size !== undefined && params.size !== null) searchParams.append('size', params.size.toString());
    if (params.sortBy) searchParams.append('sortBy', params.sortBy);
    if (params.direction) searchParams.append('direction', params.direction);
    if (params.startDate) searchParams.append('startDate', params.startDate);
    if (params.endDate) searchParams.append('endDate', params.endDate);

    return searchParams.toString();
  }

  async getAllAuditLogs(params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString(params);
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs?${queryString}`);
    return response.data;
  }

  async getAuditLogsByUsername(username: string, params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString(params);
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/username/${username}?${queryString}`);
    return response.data;
  }

  async getAuditLogsByClientId(clientId: number, params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString(params);
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/client/${clientId}?${queryString}`);
    return response.data;
  }

  async getAuditLogsByAction(action: string, params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString(params);
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/action/${action}?${queryString}`);
    return response.data;
  }

  async getAuditLogsByDateRange(startDate: string, endDate: string, params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString({ ...params, startDate, endDate });
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/date-range?${queryString}`);
    return response.data;
  }

  async getAuditLogsByUsernameAndDateRange(
    username: string,
    startDate: string,
    endDate: string,
    params: AuditLogQueryParams = {}
  ): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString({ ...params, startDate, endDate });
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/username/${username}/date-range?${queryString}`);
    return response.data;
  }

  async getAuditLogsByClientIdAndDateRange(
    clientId: number,
    startDate: string,
    endDate: string,
    params: AuditLogQueryParams = {}
  ): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString({ ...params, startDate, endDate });
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/client/${clientId}/date-range?${queryString}`);
    return response.data;
  }

  async getAuditLogsByResponseStatus(status: number, params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLog>> {
    const queryString = this.getQueryString(params);
    const response = await axios.get<PageResponse<AuditLog>>(`${API_URL}/audit-logs/status/${status}?${queryString}`);
    return response.data;
  }
}

export default new AuditLogService(); 