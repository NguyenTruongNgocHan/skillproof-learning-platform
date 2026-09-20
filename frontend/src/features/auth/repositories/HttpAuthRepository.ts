import type { AuthRepository } from "./AuthRepository";
import type { AuthResult, LoginCredentials, RegisterLearnerData, User } from "../types/auth.types";
import { apiClient, refreshAccessToken, setAccessToken, type SessionResponse } from "@/services/api/apiClient";

function toUser(session: SessionResponse): User {
  return { id: session.user.id, email: session.user.email, fullName: session.user.displayName,
    role: session.user.role, emailVerificationStatus: session.user.status === "PENDING_VERIFICATION" ? "UNVERIFIED" : "VERIFIED", onboardingStatus: "COMPLETED" };
}

export const httpAuthRepository: AuthRepository = {
  async login(credentials: LoginCredentials): Promise<AuthResult> {
    const session = await apiClient.post<SessionResponse>("/auth/login", credentials);
    setAccessToken(session.accessToken);
    return { user: toUser(session) };
  },
  async registerLearner(data: RegisterLearnerData): Promise<AuthResult> {
    const account = await apiClient.post<{ id: string; email: string; displayName: string; role: User["role"] }>(
      "/auth/register", { email: data.email, displayName: data.fullName, password: data.password, role: data.role ?? "LEARNER" });
    return { user: { id: account.id, email: account.email, fullName: account.displayName,
      role: account.role, emailVerificationStatus: "UNVERIFIED", onboardingStatus: "COMPLETED" } };
  },
  async verifyEmail(token: string): Promise<boolean> {
    await apiClient.post("/auth/verify-email", { token });
    return true;
  },
  async resendVerification(email: string): Promise<void> {
    await apiClient.post("/auth/resend-verification", { email });
  },
  async forgotPassword(email: string): Promise<void> {
    await apiClient.post("/auth/forgot-password", { email });
  },
  async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiClient.post("/auth/reset-password", { token, newPassword });
    setAccessToken(null);
  },
  async loginWithGoogle(): Promise<AuthResult> {
    window.location.assign(`${import.meta.env.VITE_BACKEND_URL ?? "http://localhost:8080"}/oauth2/authorization/google`);
    return new Promise(() => undefined);
  },
  async logout(): Promise<void> { await apiClient.post("/auth/logout"); setAccessToken(null); },
  async getSession(): Promise<User | null> {
    try { return toUser(await refreshAccessToken()); } catch { setAccessToken(null); return null; }
  },
};
