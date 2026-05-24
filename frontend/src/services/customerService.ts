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

export interface CustomerSearchOptions {
  page?: number;
  size?: number;
  signal?: AbortSignal;
}

export function listCustomers(filters: CustomerFilters = {}): Promise<PageResponse<CustomerResponse>> {
  return apiRequest<PageResponse<CustomerResponse>>("/api/v1/customers", { params: { ...filters } });
}

export function searchActiveCustomers(
  query: string,
  options: CustomerSearchOptions = {}
): Promise<PageResponse<CustomerResponse>> {
  const { page = 0, size = 10, signal } = options;
  return apiRequest<PageResponse<CustomerResponse>>("/api/v1/customers/search", {
    params: { q: query, page, size },
    signal
  });
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
