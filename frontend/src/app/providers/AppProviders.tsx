import { ReactNode } from "react"
import { ThemeProvider } from "./ThemeProvider"
import { AuthProvider } from "./AuthProvider"
import { ToastProvider } from "@/components/ui/Toast"
import { OrganizationProvider } from "./OrganizationProvider"

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
