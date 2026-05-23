import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { login as requestLogin } from "../services/authService";
import {
  clearStoredSession,
  getStoredSession,
  isSessionExpired,
  setStoredSession,
  type StoredSession
} from "../services/apiClient";
import type { LoginRequest, UserRole } from "../types/api";

export interface AuthUser {
  email: string;
  role: UserRole;
  expiresAt: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => void;
  hasRole: (roles: UserRole[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function toUser(session: StoredSession): AuthUser {
  return {
    email: session.email,
    role: session.role as UserRole,
    expiresAt: session.expiresAt
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<StoredSession | null>(() => {
    const stored = getStoredSession();
    if (isSessionExpired(stored)) {
      clearStoredSession();
      return null;
    }
    return stored;
  });

  const logout = useCallback(() => {
    clearStoredSession();
    setSession(null);
  }, []);

  useEffect(() => {
    window.addEventListener("atendeja:unauthorized", logout);
    return () => window.removeEventListener("atendeja:unauthorized", logout);
  }, [logout]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await requestLogin(request);
    const nextSession: StoredSession = {
      accessToken: response.accessToken,
      tokenType: response.tokenType,
      expiresAt: response.expiresAt,
      email: response.email,
      role: response.role
    };
    setStoredSession(nextSession);
    setSession(nextSession);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user: session ? toUser(session) : null,
    token: session?.accessToken ?? null,
    isAuthenticated: Boolean(session) && !isSessionExpired(session),
    login,
    logout,
    hasRole: (roles) => session ? roles.includes(session.role as UserRole) : false
  }), [login, logout, session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
