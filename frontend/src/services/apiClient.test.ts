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

  it("surfaces forbidden responses from protected endpoints", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response(JSON.stringify({
      title: "Acesso negado",
      detail: "Seu perfil não tem permissão para executar esta ação.",
      code: "ACCESS_DENIED"
    }), {
      status: 403,
      headers: { "Content-Type": "application/problem+json" }
    })));

    await expect(apiRequest("/api/v1/services", { method: "POST", body: { name: "Consulta" } }))
      .rejects.toMatchObject({
        status: 403,
        message: "Seu perfil não tem permissão para executar esta ação.",
        problem: expect.objectContaining({ code: "ACCESS_DENIED" })
      });
  });

  it("surfaces validation messages returned by the API", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => new Response(JSON.stringify({
      title: "Requisição inválida",
      detail: "Existem campos inválidos.",
      code: "VALIDATION_ERROR",
      errors: [
        { field: "name", message: "Informe o nome." },
        { field: "phone", message: "Informe o telefone." }
      ]
    }), {
      status: 400,
      headers: { "Content-Type": "application/problem+json" }
    })));

    await expect(apiRequest("/api/v1/customers", { method: "POST", body: {} }))
      .rejects.toMatchObject({
        status: 400,
        problem: expect.objectContaining({
          code: "VALIDATION_ERROR",
          errors: expect.arrayContaining([
            expect.objectContaining({ field: "name", message: "Informe o nome." })
          ])
        })
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

  it("returns a friendly message when the API is unreachable", async () => {
    vi.stubGlobal("fetch", vi.fn(async () => {
      throw new TypeError("Failed to fetch");
    }));

    await expect(apiRequest("/api/v1/dashboard/daily")).rejects.toMatchObject({
      status: 0,
      message: "Não foi possível conectar à API. Verifique sua conexão ou tente novamente em instantes."
    });
  });
});
