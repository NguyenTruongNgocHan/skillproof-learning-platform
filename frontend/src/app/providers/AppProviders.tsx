import { ThemeProvider } from "@/app/providers/ThemeProvider"
import { AuthProvider } from "@/features/auth/providers/AuthProvider"
import { OrganizationProvider } from "@/features/organization/providers/OrganizationProvider"
import { ToastProvider } from "@/shared/ui/Toast"
import { ReactNode } from "react"

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider>
      <AuthProvider>
        <OrganizationProvider>
          <ToastProvider>{children}</ToastProvider>
        </OrganizationProvider>
      </AuthProvider>
    </ThemeProvider>
  )
}
