import { ApiError } from "../utils/errors";
import type { ProblemResponse } from "../types/api";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");
const STORAGE_KEY = "atendeja.session";

export interface StoredSession {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  email: string;
  role: string;
}

export function getStoredSession(): StoredSession | null {
  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as StoredSession;
  } catch {
    window.localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function setStoredSession(session: StoredSession): void {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearStoredSession(): void {
  window.localStorage.removeItem(STORAGE_KEY);
}

export function isSessionExpired(session: StoredSession | null): boolean {
  if (!session) {
    return true;
  }
  return new Date(session.expiresAt).getTime() <= Date.now();
}

export type QueryParams = Record<string, string | number | boolean | null | undefined>;

export function buildQuery(params: QueryParams = {}): string {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, String(value));
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
  params?: QueryParams;
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json") || contentType.includes("application/problem+json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const problem = typeof payload === "object" ? (payload as ProblemResponse) : undefined;
    if (response.status === 401) {
      clearStoredSession();
      window.dispatchEvent(new Event("atendeja:unauthorized"));
    }
    const message = problem?.detail ?? problem?.title ?? "Não foi possível concluir a operação.";
    throw new ApiError(message, response.status, problem);
  }

  return payload as T;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const session = getStoredSession();
  const headers = new Headers(options.headers);

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  if (session && !isSessionExpired(session)) {
    headers.set("Authorization", `${session.tokenType} ${session.accessToken}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}${buildQuery(options.params)}`, {
    ...options,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });

  return parseResponse<T>(response);
}

export const apiBaseUrl = API_BASE_URL;
