import type { ReactNode } from "react"

import { useAuth } from "@/features/auth/hooks/useAuth"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import PublicFooter from "@/shared/components/layout/PublicFooter"
import PublicHeader from "@/shared/components/layout/PublicHeader"

interface LearnerSurfaceProps {
  children: ReactNode
  publicClassName?: string
}

export default function LearnerSurface({
  children,
  publicClassName = "public-page",
}: LearnerSurfaceProps) {
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
