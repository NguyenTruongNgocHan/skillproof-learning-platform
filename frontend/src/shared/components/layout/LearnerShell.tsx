import type { ReactNode } from "react"
import { useAuth } from "@/features/auth/hooks/useAuth"
import AppShell from "./AppShell"
import PublicHeader from "./PublicHeader"
export default function LearnerShell({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  return user ? (
    <AppShell>
      <div className="learner-shell__content">{children}</div>
    </AppShell>
  ) : (
    <>
      <PublicHeader />
      {children}
    </>
  )
}
