export type UserRole = "ADMIN" | "ATTENDANT";

export type AppointmentStatus =
  | "SCHEDULED"
  | "CONFIRMED"
  | "CHECKED_IN"
  | "COMPLETED"
  | "CANCELED"
  | "NO_SHOW";

export type ScheduleExceptionType = "AVAILABLE" | "BLOCKED";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProblemResponse {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  code?: string;
  errors?: Array<{
    field: string;
    message: string;
  }>;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  email: string;
  role: UserRole;
}

export interface CustomerResponse {
  id: number;
  name: string;
  phone: string;
  email: string | null;
  document: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerCreateRequest {
  name: string;
  phone: string;
  email?: string | null;
  document?: string | null;
}

export interface CustomerUpdateRequest extends CustomerCreateRequest {
  active: boolean;
}

export interface ProfessionalResponse {
  id: number;
  name: string;
  phone: string;
  email: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProfessionalCreateRequest {
  name: string;
  phone: string;
  email?: string | null;
}

export interface ProfessionalUpdateRequest extends ProfessionalCreateRequest {
  active: boolean;
}

export interface ServiceResponse {
  id: number;
  name: string;
  description: string | null;
  durationMinutes: number;
  bufferMinutes: number;
  price: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceCreateRequest {
  name: string;
  description?: string | null;
  durationMinutes: number;
  bufferMinutes: number;
  price: number;
}

export interface ServiceUpdateRequest extends ServiceCreateRequest {
  active: boolean;
}

export interface AppointmentResponse {
  id: number;
  customerId: number;
  customerName: string;
  professionalId: number;
  professionalName: string;
  serviceId: number;
  serviceName: string;
  startAt: string;
  endAt: string;
  status: AppointmentStatus;
  cancelReason: string | null;
  noShowReason: string | null;
  checkedInAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AppointmentCreateRequest {
  customerId: number;
  professionalId: number;
  serviceId: number;
  startAt: string;
}

export interface AppointmentRescheduleRequest {
  startAt: string;
}

export interface AppointmentCancelRequest {
  cancelReason: string;
}

export interface AppointmentNoShowRequest {
  noShowReason?: string | null;
}

export interface DashboardProfessionalResponse {
  id: number;
  name: string;
}

export interface DailyDashboardResponse {
  date: string;
  professional: DashboardProfessionalResponse | null;
  totalAppointments: number;
  appointmentsByStatus: Record<AppointmentStatus, number>;
  cancellations: number;
  noShows: number;
  availableMinutes: number;
  occupiedMinutes: number;
  occupancyPercentage: number;
}

export interface AvailabilityRuleResponse {
  id: number;
  professionalId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ScheduleExceptionResponse {
  id: number;
  professionalId: number;
  date: string;
  startTime: string;
  endTime: string;
  type: ScheduleExceptionType;
  reason: string | null;
  createdAt: string;
  updatedAt: string;
}
