import { useEffect, useState } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import { BookOpen, Target, Award } from "lucide-react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getNextRouteAfterLogin } from "@/utils/authFlow";
import Input from "@/components/ui/Input";
import PasswordInput from "@/components/ui/PasswordInput";
import Button from "@/components/ui/Button";
import Checkbox from "@/components/ui/Checkbox";
import ThemeSwitcher from "@/components/ui/ThemeSwitcher";
import LoadingSpinner from "@/components/feedback/LoadingSpinner";
import { useToast } from "@/components/ui/Toast";

const credentials = [
  {
    icon: BookOpen,
    title: "Structured Learning Paths",
    description: "Follow curated tracks that build real, demonstrable expertise step by step.",
  },
  {
    icon: Target,
    title: "Practice With Purpose",
    description: "Hands-on challenges that mirror real-world scenarios — not just theory.",
  },
  {
    icon: Award,
    title: "Verifiable Credentials",
    description: "Earn certificates backed by proof that employers and institutions trust.",
  },
];

export default function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login, loginWithGoogle } = useAuth();
  const { toast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (searchParams.get("verified") === "true") {
      toast("success", "Email verified successfully. Please sign in.");
    }
    if (searchParams.get("passwordReset") === "true") {
      toast("success", "Password updated successfully. Please sign in.");
    }
  }, [searchParams, toast]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const user = await login({ email, password });
      toast("success", "Welcome back to SkillProof.");
      navigate(getNextRouteAfterLogin(user));
    } catch (err: unknown) {
      setError(
        err instanceof Error ? err.message : "Invalid email or password. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen w-full" style={{ background: "var(--bg)" }}>
      {/* Left column — desktop only */}
      <div
        className="hidden md:flex flex-col justify-between w-1/2 p-10"
        style={{ background: "var(--surface-dark)" }}
      >
        {/* Wordmark */}
        <div>
          <span className="font-bold text-2xl text-white">SkillProof</span>
        </div>

        {/* Hero heading + credential items */}
        <div className="flex flex-col gap-10">
          <h1 className="text-3xl font-bold text-white leading-tight">
            Build skills that
            <br />
            can be proven.
          </h1>

          <div className="flex flex-col gap-6">
            {credentials.map(({ icon: Icon, title, description }) => (
              <div key={title} className="flex items-start gap-4">
                <div
                  className="flex-shrink-0 w-10 h-10 rounded-lg flex items-center justify-center"
                  style={{ background: "rgba(255,255,255,0.1)" }}
                >
                  <Icon size={20} style={{ color: "var(--brand)" }} />
                </div>
                <div>
                  <p className="font-semibold text-white text-sm">{title}</p>
                  <p className="text-sm mt-0.5" style={{ color: "rgba(255,255,255,0.6)" }}>
                    {description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom row */}
        <div className="flex items-center justify-between">
          <ThemeSwitcher compact />
          <span className="text-xs" style={{ color: "rgba(255,255,255,0.4)" }}>
            © 2026 SkillProof
          </span>
        </div>
      </div>

      {/* Right column */}
      <div
        className="flex flex-1 items-center justify-center p-6"
        style={{ background: "var(--bg)" }}
      >
        <div className="w-full max-w-md flex flex-col gap-6">
          {/* Heading */}
          <div>
            <h2 className="text-2xl font-bold" style={{ color: "var(--fg)" }}>
              Welcome back
            </h2>
            <p className="text-sm mt-1" style={{ color: "var(--fg-muted)" }}>
              Sign in to your account
            </p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              label="Email address"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <PasswordInput
              label="Password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            {/* Remember me + Forgot password */}
            <div className="flex items-center justify-between">
              <Checkbox
                label="Remember me"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <Link
                to="/forgot-password"
                className="text-sm font-medium"
                style={{ color: "var(--brand)" }}
              >
                Forgot password?
              </Link>
            </div>

            {/* Error message */}
            {error && (
              <div
                className="text-sm rounded-lg px-3 py-2"
                style={{
                  color: "var(--error)",
                  background: "color-mix(in srgb, var(--error) 10%, transparent)",
                }}
              >
                {error}
              </div>
            )}

            {/* Submit button */}
            <Button type="submit" variant="primary" className="w-full" disabled={loading}>
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <LoadingSpinner size={16} />
                  Signing in…
                </span>
              ) : (
                "Sign In"
              )}
            </Button>
          </form>

          {/* OR divider */}
          <div className="flex items-center gap-3">
            <div className="flex-1 h-px" style={{ background: "var(--border)" }} />
            <span className="text-xs" style={{ color: "var(--fg-muted)" }}>
              or
            </span>
            <div className="flex-1 h-px" style={{ background: "var(--border)" }} />
          </div>

          {/* Google OAuth button */}
          <Button
            type="button"
            variant="outline"
            className="w-full flex items-center justify-center gap-3"
            onClick={async () => {
              try {
                const u = await loginWithGoogle();
                navigate(getNextRouteAfterLogin(u));
              } catch {
                setError("Google sign-in failed. Please try again.");
              }
            }}
          >
            {/* Colorful Google G SVG */}
            <svg
              width="18"
              height="18"
              viewBox="0 0 18 18"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              aria-hidden="true"
            >
              <path
                d="M17.64 9.2045C17.64 8.5664 17.5827 7.9527 17.4764 7.3636H9V10.845H13.8436C13.635 11.97 13.0009 12.9231 12.0477 13.5613V15.8195H14.9564C16.6582 14.2527 17.64 11.9454 17.64 9.2045Z"
                fill="#4285F4"
              />
              <path
                d="M9 18C11.43 18 13.4673 17.1941 14.9564 15.8195L12.0477 13.5613C11.2418 14.1013 10.2109 14.4204 9 14.4204C6.65591 14.4204 4.67182 12.8372 3.96409 10.71H0.957275V13.0418C2.43818 15.9831 5.48182 18 9 18Z"
                fill="#34A853"
              />
              <path
                d="M3.96409 10.71C3.78409 10.17 3.68182 9.5931 3.68182 9C3.68182 8.4068 3.78409 7.8299 3.96409 7.29V4.9581H0.957275C0.347727 6.1731 0 7.5477 0 9C0 10.4522 0.347727 11.8268 0.957275 13.0418L3.96409 10.71Z"
                fill="#FBBC05"
              />
              <path
                d="M9 3.5795C10.3214 3.5795 11.5077 4.0336 12.4405 4.9254L15.0218 2.3441C13.4632 0.8918 11.4259 0 9 0C5.48182 0 2.43818 2.0168 0.957275 4.9581L3.96409 7.29C4.67182 5.1627 6.65591 3.5795 9 3.5795Z"
                fill="#EA4335"
              />
            </svg>
            Continue with Google
          </Button>

          {/* Register link */}
          <p className="text-center text-sm" style={{ color: "var(--fg-muted)" }}>
            {"Don't have an account?"}{" "}
            <Link to="/register" className="font-medium" style={{ color: "var(--brand)" }}>
              Create account
            </Link>
          </p>

        </div>
      </div>
    </div>
  );
}
