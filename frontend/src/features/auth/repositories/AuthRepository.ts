import type { User, LoginCredentials, RegisterLearnerData, AuthResult } from '../types/auth.types';

export interface AuthRepository {
  login(credentials: LoginCredentials): Promise<AuthResult>;
  registerLearner(data: RegisterLearnerData): Promise<AuthResult>;
  verifyEmail(code: string): Promise<boolean>;
  resendVerification(email: string): Promise<void>;
  forgotPassword(email: string): Promise<void>;
  resetPassword(token: string, newPassword: string): Promise<void>;
  loginWithGoogle(): Promise<AuthResult>;
  logout(): Promise<void>;
  getSession(): Promise<User | null>;
}
