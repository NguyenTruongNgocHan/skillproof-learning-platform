import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import LoadingSpinner from "@/components/feedback/LoadingSpinner";
import BrandLogo from '@/components/ui/BrandLogo';
import { getNextRouteAfterLogin } from '@/utils/authFlow';
import { useToast } from '@/components/ui/Toast';

export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const { refreshSession } = useAuth();
  const { toast } = useToast();

  useEffect(() => {
    refreshSession().then((user) => {
      if (!user) throw new Error('OAuth session was not created.');
      toast('success', 'Google sign-in completed successfully.');
      navigate(getNextRouteAfterLogin(user), { replace: true });
    }).catch(() => navigate("/login?oauth=failed", { replace: true }));
  }, [navigate, refreshSession, toast]);

  return (
    <main className="min-h-screen flex items-center justify-center" style={{ background: "var(--bg)" }}>
      <div className="oauth-callback-card">
        <BrandLogo />
        <div className="oauth-loader"><LoadingSpinner size={24} /></div>
        <h1>Securing your SkillProof session</h1>
        <p>Google has confirmed your identity. We are preparing the correct workspace for your role.</p>
      </div>
    </main>
  );
}
