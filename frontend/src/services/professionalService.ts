import { apiRequest } from "./apiClient";
import type {
  PageResponse,
  ProfessionalCreateRequest,
  ProfessionalResponse,
  ProfessionalUpdateRequest
} from "../types/api";

export interface ProfessionalFilters {
  search?: string;
  active?: boolean | "";
  page?: number;
  size?: number;
}

export function listProfessionals(filters: ProfessionalFilters = {}): Promise<PageResponse<ProfessionalResponse>> {
  return apiRequest<PageResponse<ProfessionalResponse>>("/api/v1/professionals", { params: { ...filters } });
}

export function createProfessional(request: ProfessionalCreateRequest): Promise<ProfessionalResponse> {
  return apiRequest<ProfessionalResponse>("/api/v1/professionals", { method: "POST", body: request });
}

export function updateProfessional(id: number, request: ProfessionalUpdateRequest): Promise<ProfessionalResponse> {
  return apiRequest<ProfessionalResponse>(`/api/v1/professionals/${id}`, { method: "PUT", body: request });
}

export function deactivateProfessional(id: number): Promise<void> {
  return apiRequest<void>(`/api/v1/professionals/${id}`, { method: "DELETE" });
}
