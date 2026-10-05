import {
  createContext,
  useState,
  useEffect,
  useCallback,
  ReactNode,
} from "react"
import type {
  User,
  LoginCredentials,
  RegisterLearnerData,
  LearnerPreferences,
} from "@/features/auth/types/auth.types"
import { httpAuthRepository } from "@/features/auth/repositories/HttpAuthRepository"

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (creds: LoginCredentials) => Promise<User>
  logout: () => Promise<void>
  register: (data: RegisterLearnerData) => Promise<User>
  verifyEmail: (otp: string) => Promise<boolean>
  resendVerification: (email: string) => Promise<void>
  forgotPassword: (email: string) => Promise<void>
  resetPassword: (token: string, newPassword: string) => Promise<void>
  loginWithGoogle: () => Promise<User>
  completeLearnerOnboarding: (data: LearnerPreferences) => Promise<void>
  refreshSession: () => Promise<User | null>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const refreshSession = useCallback(async () => {
    const stored = await httpAuthRepository.getSession()
    setUser(stored)
    return stored
  }, [])

  useEffect(() => {
    httpAuthRepository
      .getSession()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setIsLoading(false))
  }, [])

  const login = async (creds: LoginCredentials): Promise<User> => {
    const { user: u } = await httpAuthRepository.login(creds)
    setUser(u)
    return u
  }

  const logout = async () => {
    await httpAuthRepository.logout()
    localStorage.removeItem("skillproof.organizer.organization")
    setUser(null)
  }

  const register = async (data: RegisterLearnerData): Promise<User> => {
    const { user: u } = await httpAuthRepository.registerLearner(data)
    // Registration does not establish a session. Verification is followed by sign-in.
    return u
  }

  const verifyEmail = async (otp: string): Promise<boolean> => {
    return httpAuthRepository.verifyEmail(otp)
  }

  const resendVerification = async (email: string) => {
    await httpAuthRepository.resendVerification(email)
  }

  const forgotPassword = async (email: string) => {
    await httpAuthRepository.forgotPassword(email)
  }

  const resetPassword = async (token: string, newPassword: string) => {
    await httpAuthRepository.resetPassword(token, newPassword)
    setUser(null)
  }

  const loginWithGoogle = async (): Promise<User> => {
    const { user: u } = await httpAuthRepository.loginWithGoogle()
    setUser(u)
    return u
  }

  const completeLearnerOnboarding = async (data: LearnerPreferences) => {
    await httpAuthRepository.saveLearnerPreferences(data)
    setUser((prev) =>
      prev ? { ...prev, onboardingStatus: "COMPLETED" } : null,
    )
  }

  if (isLoading) {
    return (
      <div
        className="min-h-screen flex items-center justify-center"
        style={{ background: "var(--bg)" }}
      >
        <div
          className="w-8 h-8 border-2 rounded-full animate-spin"
          style={{
            borderColor: "var(--border)",
            borderTopColor: "var(--brand)",
          }}
        />
      </div>
    )
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user && user.emailVerificationStatus === "VERIFIED",
        isLoading,
        login,
        logout,
        register,
        verifyEmail,
        resendVerification,
        forgotPassword,
        resetPassword,
        loginWithGoogle,
        completeLearnerOnboarding,
        refreshSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
