import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import LoadingSpinner from "@/components/feedback/LoadingSpinner";

export default function OAuthCallbackPage() {
  const navigate = useNavigate();
  const { refreshSession } = useAuth();

  useEffect(() => {
    refreshSession()
      .then(() => navigate("/app", { replace: true }))
      .catch(() => navigate("/login?oauth=failed", { replace: true }));
  }, [navigate, refreshSession]);

  return (
    <main className="min-h-screen flex items-center justify-center" style={{ background: "var(--bg)" }}>
      <div className="flex items-center gap-3" style={{ color: "var(--fg)" }}>
        <LoadingSpinner size={20} />
        <span>Completing secure sign-in…</span>
      </div>
    </main>
  );
}
