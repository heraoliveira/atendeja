import type {
  AppointmentResponse,
  AppointmentStatus,
  CustomerResponse,
  DailyDashboardResponse,
  LoginResponse,
  PageResponse,
  ProfessionalResponse,
  ServiceResponse
} from "../types/api";

export function pageResponse<T>(content: T[], overrides: Partial<PageResponse<T>> = {}): PageResponse<T> {
  return {
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: content.length > 0 ? 1 : 0,
    first: true,
    last: true,
    ...overrides
  };
}

export function loginResponse(overrides: Partial<LoginResponse> = {}): LoginResponse {
  return {
    accessToken: "jwt-token",
    tokenType: "Bearer",
    expiresAt: "2999-01-01T00:00:00Z",
    email: "admin@atendeja.local",
    role: "ADMIN",
    ...overrides
  };
}

export function customerResponse(overrides: Partial<CustomerResponse> = {}): CustomerResponse {
  return {
    id: 1,
    name: "Maria Cliente",
    phone: "11999990000",
    email: "maria@example.com",
    document: "12345678900",
    active: true,
    createdAt: "2030-01-01T10:00:00Z",
    updatedAt: "2030-01-01T10:00:00Z",
    ...overrides
  };
}

export function professionalResponse(overrides: Partial<ProfessionalResponse> = {}): ProfessionalResponse {
  return {
    id: 2,
    name: "Ana Profissional",
    phone: "11988880000",
    email: "ana@example.com",
    active: true,
    createdAt: "2030-01-01T10:00:00Z",
    updatedAt: "2030-01-01T10:00:00Z",
    ...overrides
  };
}

export function serviceResponse(overrides: Partial<ServiceResponse> = {}): ServiceResponse {
  return {
    id: 3,
    name: "Consulta inicial",
    description: "Atendimento de avaliação",
    durationMinutes: 45,
    bufferMinutes: 15,
    price: 150,
    active: true,
    createdAt: "2030-01-01T10:00:00Z",
    updatedAt: "2030-01-01T10:00:00Z",
    ...overrides
  };
}

export function appointmentResponse(overrides: Partial<AppointmentResponse> = {}): AppointmentResponse {
  return {
    id: 10,
    customerId: 1,
    customerName: "Maria Cliente",
    professionalId: 2,
    professionalName: "Ana Profissional",
    serviceId: 3,
    serviceName: "Consulta inicial",
    startAt: "2030-01-20T12:00:00Z",
    endAt: "2030-01-20T13:00:00Z",
    status: "SCHEDULED",
    cancelReason: null,
    noShowReason: null,
    checkedInAt: null,
    createdAt: "2030-01-01T10:00:00Z",
    updatedAt: "2030-01-01T10:00:00Z",
    ...overrides
  };
}

export function dailyDashboardResponse(overrides: Partial<DailyDashboardResponse> = {}): DailyDashboardResponse {
  const appointmentsByStatus: Record<AppointmentStatus, number> = {
    SCHEDULED: 2,
    CONFIRMED: 1,
    CHECKED_IN: 1,
    COMPLETED: 1,
    CANCELED: 1,
    NO_SHOW: 1
  };

  return {
    date: "2030-01-20",
    professional: null,
    totalAppointments: 7,
    appointmentsByStatus,
    cancellations: 1,
    noShows: 1,
    availableMinutes: 480,
    occupiedMinutes: 300,
    occupancyPercentage: 62.5,
    ...overrides
  };
}
