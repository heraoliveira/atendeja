import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it } from "vitest";
import { AuthProvider } from "../contexts/AuthContext";
import { ProtectedRoute } from "../routes/ProtectedRoute";
import { clearStoredSession, getStoredSession, setStoredSession } from "../services/apiClient";
import { AppLayout } from "./AppLayout";

describe("AppLayout", () => {
  beforeEach(() => {
    clearStoredSession();
  });

  it("logs out the current user and lets protected routes redirect to login", async () => {
    setStoredSession({
      accessToken: "valid-token",
      tokenType: "Bearer",
      expiresAt: "2999-01-01T00:00:00Z",
      email: "admin@atendeja.local",
      role: "ADMIN"
    });

    render(
      <MemoryRouter initialEntries={["/dashboard"]}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>Tela de login</div>} />
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                <Route path="/dashboard" element={<div>Conteúdo protegido</div>} />
              </Route>
            </Route>
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    expect(await screen.findByText("Conteúdo protegido")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /sair/i }));

    await waitFor(() => expect(getStoredSession()).toBeNull());
    expect(await screen.findByText("Tela de login")).toBeInTheDocument();
  });
});
