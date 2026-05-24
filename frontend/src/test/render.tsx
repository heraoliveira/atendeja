import { render } from "@testing-library/react";
import type { ReactElement } from "react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../contexts/AuthContext";
import { clearStoredSession, setStoredSession } from "../services/apiClient";
import type { UserRole } from "../types/api";

interface RenderOptions {
  route?: string;
  role?: UserRole | null;
}

export function renderWithProviders(ui: ReactElement, options: RenderOptions = {}) {
  const { route = "/", role = "ADMIN" } = options;
  clearStoredSession();

  if (role) {
    setStoredSession({
      accessToken: `${role.toLowerCase()}-token`,
      tokenType: "Bearer",
      expiresAt: "2999-01-01T00:00:00Z",
      email: role === "ADMIN" ? "admin@atendeja.local" : "attendant@atendeja.local",
      role
    });
  }

  return render(
    <MemoryRouter initialEntries={[route]}>
      <AuthProvider>
        {ui}
      </AuthProvider>
    </MemoryRouter>
  );
}
