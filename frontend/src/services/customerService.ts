import { apiRequest } from "./apiClient";
import type {
  CustomerCreateRequest,
  CustomerResponse,
  CustomerUpdateRequest,
  PageResponse
} from "../types/api";

export interface CustomerFilters {
  search?: string;
  active?: boolean | "";
  page?: number;
  size?: number;
}

export function listCustomers(filters: CustomerFilters = {}): Promise<PageResponse<CustomerResponse>> {
  return apiRequest<PageResponse<CustomerResponse>>("/api/v1/customers", { params: { ...filters } });
}

export function createCustomer(request: CustomerCreateRequest): Promise<CustomerResponse> {
  return apiRequest<CustomerResponse>("/api/v1/customers", { method: "POST", body: request });
}

export function updateCustomer(id: number, request: CustomerUpdateRequest): Promise<CustomerResponse> {
  return apiRequest<CustomerResponse>(`/api/v1/customers/${id}`, { method: "PUT", body: request });
}

export function deactivateCustomer(id: number): Promise<void> {
  return apiRequest<void>(`/api/v1/customers/${id}`, { method: "DELETE" });
}
