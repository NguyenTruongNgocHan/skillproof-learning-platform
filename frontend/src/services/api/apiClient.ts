const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api/v1";

export interface ApiProblem { code: string; message: string; fieldErrors?: Record<string, string>; }
export class ApiError extends Error {
  constructor(public status: number, public problem: ApiProblem) { super(problem.message); }
}

let accessToken: string | null = null;
let refreshRequest: Promise<SessionResponse> | null = null;
export function setAccessToken(token: string | null) { accessToken = token; }

async function request<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set("Content-Type", "application/json");
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);
  const response = await fetch(`${API_URL}${path}`, { ...init, headers, credentials: "include" });
  if (response.status === 401 && retry && accessToken && path !== "/auth/refresh") {
    refreshRequest ??= refreshAccessToken().finally(() => {
      refreshRequest = null;
    });
    try {
      await refreshRequest;
      return request<T>(path, init, false);
    } catch (error) {
      setAccessToken(null);
      throw error;
    }
  }
  if (!response.ok) {
    const fallback = { code: "REQUEST_FAILED", message: "The request could not be completed." };
    throw new ApiError(response.status, await response.json().catch(() => fallback));
  }
  if (response.status === 204) return undefined as T;

  const contentType = response.headers.get("content-type");
  const contentLength = response.headers.get("content-length");

  if (
    contentLength === "0" ||
    !contentType?.includes("application/json")
  ) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export interface SessionResponse {
  accessToken: string;
  expiresIn: number;
  user: { id: string; email: string; displayName: string; role: "LEARNER" | "ORGANIZER" | "ADMIN"; status: "PENDING_VERIFICATION" | "ACTIVE" | "DISABLED" };
}

export async function refreshAccessToken(): Promise<SessionResponse> {
  const session = await request<SessionResponse>("/auth/refresh", { method: "POST" }, false);
  setAccessToken(session.accessToken);
  return session;
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) => request<T>(path, {
    method: "POST",
    body: body === undefined ? undefined : JSON.stringify(body),
  }),
  patch: <T>(path: string, body: unknown) => request<T>(path, {
    method: "PATCH",
    body: JSON.stringify(body),
  }),
};
