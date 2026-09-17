import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { getDefaultRouteForRole } from '@/utils/authFlow';
import { ReactNode } from 'react';

export function GuestGuard({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading, user } = useAuth();
  if (isLoading) return null;
  if (isAuthenticated && user) return <Navigate to={getDefaultRouteForRole(user.role)} replace />;
  return <>{children}</>;
}
