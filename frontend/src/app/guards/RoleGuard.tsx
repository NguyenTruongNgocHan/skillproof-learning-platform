import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { getDefaultRouteForRole } from '@/utils/authFlow';
import type { UserRole } from '@/features/auth/types/auth.types';
import { ReactNode } from 'react';

export function RoleGuard({ allowedRole, children }: { allowedRole: UserRole; children: ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== allowedRole) return <Navigate to={getDefaultRouteForRole(user.role)} replace />;
  return <>{children}</>;
}
