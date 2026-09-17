import { useAuth } from '@/features/auth/hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { Clock, LogOut, CheckCircle2 } from 'lucide-react';
import { primaryLight } from '@/assets/brand';
import Button from '@/components/ui/Button';
import { storage, KEYS } from '@/services/storage/storage';
import type { OrganizerProfile } from '@/features/auth/types/auth.types';

export default function OrganizerVerificationPendingPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const profile = storage.get<OrganizerProfile>(KEYS.ORGANIZER_PROFILE);

  const handleSignOut = async () => {
    await logout();
    navigate('/login');
  };

  const handleDemoContinue = () => {
    if (profile) {
      storage.set(KEYS.ORGANIZER_PROFILE, { ...profile, verificationStatus: 'VERIFIED' });
    }
    navigate('/organizer');
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-4" style={{ background: 'var(--bg)' }}>
      <img src={primaryLight} alt="SkillProof" style={{ width: 140, height: 'auto', objectFit: 'contain', marginBottom: 40 }} />
      <div className="w-full max-w-md rounded-2xl p-8 text-center" style={{ background: 'var(--surface)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-md)' }}>
        <div className="w-14 h-14 rounded-2xl flex items-center justify-center mx-auto mb-4" style={{ background: 'var(--warning-bg)' }}>
          <Clock style={{ width: 28, height: 28, color: 'var(--warning)' }} />
        </div>
        <h1 className="text-xl font-bold mb-2" style={{ color: 'var(--fg)' }}>Verification Under Review</h1>
        <p className="text-sm mb-6" style={{ color: 'var(--fg-muted)' }}>
          Your organization verification request has been submitted and is currently under review. Our team typically responds within 2–3 business days.
        </p>
        {profile?.organizationName && (
          <div className="rounded-xl p-4 mb-6 text-left" style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border)' }}>
            <p className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: 'var(--fg-muted)' }}>Submitted Organization</p>
            <div className="flex flex-col gap-1.5">
              <p className="text-sm font-medium" style={{ color: 'var(--fg)' }}>{profile.organizationName}</p>
              {profile.jobTitle && <p className="text-sm" style={{ color: 'var(--fg-muted)' }}>{profile.jobTitle} — {user?.fullName}</p>}
              {user?.email && <p className="text-sm" style={{ color: 'var(--fg-muted)' }}>{user.email}</p>}
            </div>
          </div>
        )}
        <div className="flex flex-col gap-2">
          <button
            onClick={handleSignOut}
            className="flex items-center justify-center gap-2 w-full py-2.5 text-sm font-medium rounded-lg transition-colors"
            style={{ color: 'var(--fg-muted)', border: '1px solid var(--border)' }}
          >
            <LogOut style={{ width: 15, height: 15 }} />
            Sign Out
          </button>
        </div>
        {/* DEMO ONLY — would not appear in production */}
        <div className="mt-6 pt-5" style={{ borderTop: '1px dashed var(--border)' }}>
          <p className="text-xs mb-3" style={{ color: 'var(--fg-muted)' }}>Demo only — bypass verification for testing:</p>
          <Button variant="outline" size="sm" onClick={handleDemoContinue} className="w-full">
            <CheckCircle2 style={{ width: 14, height: 14, marginRight: 6 }} />
            Continue as Verified Demo Organizer
          </Button>
        </div>
      </div>
    </div>
  );
}
