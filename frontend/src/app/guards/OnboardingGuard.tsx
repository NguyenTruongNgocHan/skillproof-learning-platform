import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { ReactNode } from 'react';

export function OnboardingGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.emailVerificationStatus === 'UNVERIFIED') return <Navigate to="/verify-email" replace />;
  if (user.onboardingStatus !== 'COMPLETED') {
    return <Navigate to={user.role === 'ORGANIZER' ? '/onboarding/organizer' : '/onboarding/learner'} replace />;
  }
  return <>{children}</>;
}
