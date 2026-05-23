import { apiRequest } from "./apiClient";
import type { LoginRequest, LoginResponse } from "../types/api";

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: request
  });
}
