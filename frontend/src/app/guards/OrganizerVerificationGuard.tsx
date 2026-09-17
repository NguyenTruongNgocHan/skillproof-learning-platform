import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { storage, KEYS } from '@/services/storage/storage';
import type { OrganizerProfile } from '@/features/auth/types/auth.types';
import { ReactNode } from 'react';

export function OrganizerVerificationGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  const profile = storage.get<OrganizerProfile>(KEYS.ORGANIZER_PROFILE);
  if (profile && (profile.verificationStatus === 'PENDING' || profile.verificationStatus === 'NOT_SUBMITTED')) {
    return <Navigate to="/organizer/verification-pending" replace />;
  }
  return <>{children}</>;
}
