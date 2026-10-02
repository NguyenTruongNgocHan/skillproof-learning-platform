import type { ReactNode } from "react"

import LearnerShell from "@/components/layout/LearnerShell"
import PublicFooter from "@/components/layout/PublicFooter"
import PublicHeader from "@/components/layout/PublicHeader"
import { useAuth } from "@/features/auth/hooks/useAuth"

interface LearnerSurfaceProps {
  children: ReactNode
  publicClassName?: string
}

export default function LearnerSurface({ children, publicClassName = "public-page" }: LearnerSurfaceProps) {
  const { user } = useAuth()

  if (user?.role === "LEARNER") return <LearnerShell>{children}</LearnerShell>

  return (
    <div className={publicClassName}>
      <PublicHeader />
      {children}
      <PublicFooter />
    </div>
  )
}
