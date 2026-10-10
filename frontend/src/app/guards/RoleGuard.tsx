import { useAuth } from "@/features/auth/hooks/useAuth"
import type { UserRole } from "@/features/auth/types/auth.types"
import { getNextRouteAfterLogin } from "@/features/auth/utils/authFlow"
import { ReactNode } from "react"
import { Navigate } from "react-router-dom"

export function RoleGuard({
  allowedRole,
  children,
}: {
  allowedRole: UserRole
  children: ReactNode
}) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (user.role !== allowedRole) return <Navigate to={getNextRouteAfterLogin(user)} replace />
  return <>{children}</>
}
