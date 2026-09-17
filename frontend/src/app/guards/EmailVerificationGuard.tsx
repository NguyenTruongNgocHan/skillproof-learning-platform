import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { ReactNode } from 'react';

export function EmailVerificationGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.emailVerificationStatus === 'VERIFIED') {
    if (user.onboardingStatus !== 'COMPLETED') {
      return <Navigate to={user.role === 'ORGANIZER' ? '/onboarding/organizer' : '/onboarding/learner'} replace />;
    }
    return <Navigate to={user.role === 'ORGANIZER' ? '/organizer' : user.role === 'ADMIN' ? '/admin' : '/app'} replace />;
  }
  return <>{children}</>;
}
