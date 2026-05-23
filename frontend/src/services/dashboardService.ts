import { apiRequest } from "./apiClient";
import type { DailyDashboardResponse } from "../types/api";

export interface DashboardFilters {
  date?: string;
  professionalId?: number | "";
}

export function getDailyDashboard(filters: DashboardFilters = {}): Promise<DailyDashboardResponse> {
  return apiRequest<DailyDashboardResponse>("/api/v1/dashboard/daily", { params: { ...filters } });
}
