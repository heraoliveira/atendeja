import { beforeEach, describe, expect, it, vi } from "vitest";
import { apiRequest, clearStoredSession, getStoredSession, setStoredSession } from "./apiClient";
import { ApiError } from "../utils/errors";

describe("apiRequest", () => {
  beforeEach(() => {
    clearStoredSession();
    vi.restoreAllMocks();
  });

  it("sends bearer token from the stored session", async () => {
    setStoredSession({
      accessToken: "token-123",
      tokenType: "Bearer",
      expiresAt: "2099-01-01T00:00:00Z",
      email: "admin@atendeja.local",
      role: "ADMIN"
    });
    const fetchMock = vi.fn(async (_input: RequestInfo | URL, _init?: RequestInit) => new Response(JSON.stringify({ content: [] }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    }));
    vi.stubGlobal("fetch", fetchMock);

    await apiRequest("/api/v1/customers", { params: { page: 0, size: 10 } });

    const [url, options] = fetchMock.mock.calls[0] as [RequestInfo | URL, RequestInit?];
    const headers = options?.headers as Headers;
    expect(url).toBe("http://localhost:8080/api/v1/customers?page=0&size=10");
    expect(headers.get("Authorization")).toBe("Bearer token-123");
  });

  it("raises API errors with problem details", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response(JSON.stringify({
      title: "Conflito de agenda",
      detail: "Já existe um agendamento para este profissional neste horário.",
      code: "APPOINTMENT_TIME_CONFLICT"
    }), {
      status: 409,
      headers: { "Content-Type": "application/problem+json" }
    })));

    await expect(apiRequest("/api/v1/appointments")).rejects.toMatchObject({
      status: 409,
      message: "Já existe um agendamento para este profissional neste horário."
    });
  });

  it("clears stored session after unauthorized response", async () => {
    setStoredSession({
      accessToken: "expired-token",
      tokenType: "Bearer",
      expiresAt: "2099-01-01T00:00:00Z",
      email: "attendant@atendeja.local",
      role: "ATTENDANT"
    });
    vi.stubGlobal("fetch", vi.fn(async () => new Response(JSON.stringify({
      title: "Autenticação necessária",
      detail: "Informe um Bearer Token válido para acessar este recurso.",
      code: "UNAUTHORIZED"
    }), {
      status: 401,
      headers: { "Content-Type": "application/problem+json" }
    })));

    await expect(apiRequest("/api/v1/appointments")).rejects.toBeInstanceOf(ApiError);

    expect(getStoredSession()).toBeNull();
  });
});
