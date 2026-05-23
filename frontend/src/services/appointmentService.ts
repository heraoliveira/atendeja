import { apiRequest } from "./apiClient";
import type {
  AppointmentCancelRequest,
  AppointmentCreateRequest,
  AppointmentNoShowRequest,
  AppointmentResponse,
  AppointmentRescheduleRequest,
  AppointmentStatus,
  PageResponse
} from "../types/api";

export interface AppointmentFilters {
  date?: string;
  professionalId?: number | "";
  customerId?: number | "";
  serviceId?: number | "";
  status?: AppointmentStatus | "";
  page?: number;
  size?: number;
  sort?: string;
}

export function listAppointments(filters: AppointmentFilters = {}): Promise<PageResponse<AppointmentResponse>> {
  return apiRequest<PageResponse<AppointmentResponse>>("/api/v1/appointments", { params: { ...filters } });
}

export function createAppointment(request: AppointmentCreateRequest): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>("/api/v1/appointments", { method: "POST", body: request });
}

export function rescheduleAppointment(
  id: number,
  request: AppointmentRescheduleRequest
): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/reschedule`, {
    method: "PATCH",
    body: request
  });
}

export function cancelAppointment(id: number, request: AppointmentCancelRequest): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/cancel`, {
    method: "PATCH",
    body: request
  });
}

export function confirmAppointment(id: number): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/confirm`, { method: "PATCH" });
}

export function checkInAppointment(id: number): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/check-in`, { method: "PATCH" });
}

export function completeAppointment(id: number): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/complete`, { method: "PATCH" });
}

export function markAppointmentNoShow(id: number, request: AppointmentNoShowRequest): Promise<AppointmentResponse> {
  return apiRequest<AppointmentResponse>(`/api/v1/appointments/${id}/no-show`, {
    method: "PATCH",
    body: request
  });
}
