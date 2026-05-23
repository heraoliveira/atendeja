import { apiRequest } from "./apiClient";
import type {
  PageResponse,
  ServiceCreateRequest,
  ServiceResponse,
  ServiceUpdateRequest
} from "../types/api";

export interface ServiceFilters {
  search?: string;
  active?: boolean | "";
  page?: number;
  size?: number;
}

export function listServices(filters: ServiceFilters = {}): Promise<PageResponse<ServiceResponse>> {
  return apiRequest<PageResponse<ServiceResponse>>("/api/v1/services", { params: { ...filters } });
}

export function createService(request: ServiceCreateRequest): Promise<ServiceResponse> {
  return apiRequest<ServiceResponse>("/api/v1/services", { method: "POST", body: request });
}

export function updateService(id: number, request: ServiceUpdateRequest): Promise<ServiceResponse> {
  return apiRequest<ServiceResponse>(`/api/v1/services/${id}`, { method: "PUT", body: request });
}

export function deactivateService(id: number): Promise<void> {
  return apiRequest<void>(`/api/v1/services/${id}`, { method: "DELETE" });
}
