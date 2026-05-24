import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../contexts/AuthContext";
import { clearStoredSession, getStoredSession } from "../services/apiClient";
import { login as requestLogin } from "../services/authService";
import { loginResponse } from "../test/factories";
import { ApiError } from "../utils/errors";
import { LoginPage } from "./LoginPage";

vi.mock("../services/authService", () => ({
  login: vi.fn()
}));

const mockedLogin = vi.mocked(requestLogin);

function renderLoginPage(initialPath = "/login") {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/dashboard" element={<div>Dashboard protegido</div>} />
          <Route path="/appointments" element={<div>Agenda protegida</div>} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    clearStoredSession();
    mockedLogin.mockReset();
  });

  it("stores the returned token and redirects after successful login", async () => {
    mockedLogin.mockResolvedValue(loginResponse());

    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: "admin@atendeja.local" } });
    fireEvent.change(screen.getByLabelText(/senha/i), { target: { value: "admin-pass" } });
    fireEvent.click(screen.getByRole("button", { name: /^entrar$/i }));

    expect(await screen.findByText("Dashboard protegido")).toBeInTheDocument();
    expect(mockedLogin).toHaveBeenCalledWith({
      email: "admin@atendeja.local",
      password: "admin-pass"
    });
    expect(getStoredSession()).toMatchObject({
      accessToken: "jwt-token",
      tokenType: "Bearer",
      email: "admin@atendeja.local",
      role: "ADMIN"
    });
  });

  it("preserves the protected target requested before authentication", async () => {
    mockedLogin.mockResolvedValue(loginResponse({ role: "ATTENDANT", email: "attendant@atendeja.local" }));

    render(
      <MemoryRouter
        initialEntries={[{
          pathname: "/login",
          state: { from: { pathname: "/appointments" } }
        }]}
      >
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>Dashboard protegido</div>} />
            <Route path="/appointments" element={<div>Agenda protegida</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: "attendant@atendeja.local" } });
    fireEvent.change(screen.getByLabelText(/senha/i), { target: { value: "attendant-pass" } });
    fireEvent.click(screen.getByRole("button", { name: /^entrar$/i }));

    expect(await screen.findByText("Agenda protegida")).toBeInTheDocument();
  });

  it("shows the API error message when credentials are rejected", async () => {
    mockedLogin.mockRejectedValue(new ApiError("E-mail ou senha inválidos.", 401, {
      status: 401,
      detail: "E-mail ou senha inválidos.",
      code: "INVALID_CREDENTIALS"
    }));

    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/e-mail/i), { target: { value: "admin@atendeja.local" } });
    fireEvent.change(screen.getByLabelText(/senha/i), { target: { value: "wrong-pass" } });
    fireEvent.click(screen.getByRole("button", { name: /^entrar$/i }));

    expect(await screen.findByText("E-mail ou senha inválidos.")).toBeInTheDocument();
    await waitFor(() => expect(getStoredSession()).toBeNull());
  });
});
