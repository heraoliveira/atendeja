import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it } from "vitest";
import { AuthProvider } from "../contexts/AuthContext";
import { clearStoredSession, getStoredSession, setStoredSession } from "../services/apiClient";
import { ProtectedRoute } from "./ProtectedRoute";

function renderRoutes() {
  return render(
    <MemoryRouter initialEntries={["/dashboard"]}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Tela de login</div>} />
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<div>Dashboard autenticado</div>} />
          </Route>
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  );
}

describe("ProtectedRoute", () => {
  beforeEach(() => {
    clearStoredSession();
  });

  it("redirects unauthenticated users to login", async () => {
    renderRoutes();

    expect(await screen.findByText("Tela de login")).toBeInTheDocument();
  });

  it("renders protected content when a valid session exists", async () => {
    setStoredSession({
      accessToken: "valid-token",
      tokenType: "Bearer",
      expiresAt: "2999-01-01T00:00:00Z",
      email: "admin@atendeja.local",
      role: "ADMIN"
    });

    renderRoutes();

    expect(await screen.findByText("Dashboard autenticado")).toBeInTheDocument();
  });

  it("clears expired sessions before rendering protected routes", async () => {
    setStoredSession({
      accessToken: "expired-token",
      tokenType: "Bearer",
      expiresAt: "2000-01-01T00:00:00Z",
      email: "admin@atendeja.local",
      role: "ADMIN"
    });

    renderRoutes();

    expect(await screen.findByText("Tela de login")).toBeInTheDocument();
    expect(getStoredSession()).toBeNull();
  });
});
