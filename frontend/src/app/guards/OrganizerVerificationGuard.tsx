import { ReactNode, useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { organizationApi, type Organization } from '@/features/organization/organizationApi';
import { ApiError } from '@/services/api/apiClient';
export function OrganizerVerificationGuard({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [result, setResult] = useState<{ organization: Organization | null; error: string | null } | null>(null);
  useEffect(() => {
    let mounted = true; if (!user || user.role !== 'ORGANIZER') return;
    organizationApi.mine().then(organization => { if (mounted) setResult({ organization, error: null }); }).catch(e => { if (mounted) setResult({ organization: null, error: e instanceof ApiError && e.status === 404 ? null : 'Could not confirm organization status. Please retry.' }); });
    return () => { mounted = false; };
  }, [user?.id, user?.role]);
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== 'ORGANIZER') return <Navigate to="/" replace />;
  if (!result) return <main className="org-state">Checking organization access…</main>;
  if (result.error) return <main className="org-state" role="alert">{result.error} <button onClick={() => window.location.reload()}>Retry</button></main>;
  if (result.organization?.status !== 'APPROVED') return <Navigate to={result.organization ? '/organizer/verification-pending' : '/onboarding/organizer'} replace />;
  return <>{children}</>;
}
