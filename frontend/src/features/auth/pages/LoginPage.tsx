import AuthShell from "@/features/auth/components/AuthShell"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { safeDestination } from "@/features/auth/utils/authDestination"
import { getNextRouteAfterLogin } from "@/features/auth/utils/authFlow"
import LoadingSpinner from "@/shared/components/feedback/LoadingSpinner"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"
import PasswordInput from "@/shared/ui/PasswordInput"
import { useToast } from "@/shared/ui/Toast"
import { useEffect, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"

function GoogleIcon() {
  return (
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
  )
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { login, loginWithGoogle } = useAuth()
  const { toast } = useToast()

  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (searchParams.get("verified") === "true") {
      toast("success", "Your email is verified. You can sign in now.")
    }

    if (searchParams.get("passwordReset") === "true") {
      toast("success", "Your password has been updated. Sign in to continue.")
    }

    if (searchParams.get("oauth") === "failed") {
      toast("error", "We couldn't complete Google sign-in. Please try again.")
    }
  }, [searchParams, toast])

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setLoading(true)

    try {
      const user = await login({ email, password })

      toast("success", "Welcome back. Your learning is ready when you are.")

      const next = getNextRouteAfterLogin(user)
      const intended = searchParams.get("returnTo")
      const destination =
        safeDestination(intended) ??
        safeDestination(sessionStorage.getItem("skillproof.auth.returnTo"))
      if (user.emailVerificationStatus === "UNVERIFIED" && destination)
        sessionStorage.setItem("skillproof.auth.returnTo", destination)
      else sessionStorage.removeItem("skillproof.auth.returnTo")

      navigate(user.emailVerificationStatus === "UNVERIFIED" ? next : (destination ?? next))
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : "We couldn't sign you in. Check your email and password and try again.",
      )
    } finally {
      setLoading(false)
    }
  }

  async function handleGoogleSignIn() {
    setError(null)

    try {
      const destination = safeDestination(searchParams.get("returnTo"))
      if (destination) sessionStorage.setItem("skillproof.auth.returnTo", destination)
      const user = await loginWithGoogle()
      const intended = searchParams.get("returnTo")
      navigate(
        user.emailVerificationStatus === "UNVERIFIED"
          ? getNextRouteAfterLogin(user)
          : (safeDestination(intended) ?? getNextRouteAfterLogin(user)),
      )
    } catch {
      setError("We couldn't complete Google sign-in. Please try again.")
    }
  }

  return (
    <AuthShell mode="login">
      <div className="auth-intro">
        <span className="auth-intro__eyebrow">CONTINUE YOUR JOURNEY</span>

        <h1>Welcome back</h1>

        <p>Sign in to continue learning, practicing and building proof of your skills.</p>
      </div>

      <form onSubmit={handleSubmit} className="auth-form-stack">
        <Input
          label="Email address"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />

        <div>
          <PasswordInput
            label="Password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          <div className="auth-password-row">
            <span>Your session is protected by SkillProof.</span>

            <Link to="/forgot-password">Forgot password?</Link>
          </div>
        </div>

        {error && (
          <div className="auth-message auth-message--error" role="alert">
            {error}
          </div>
        )}

        <Button type="submit" variant="primary" className="w-full" disabled={loading}>
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <LoadingSpinner size={16} />
              Signing you in…
            </span>
          ) : (
            "Sign in"
          )}
        </Button>
      </form>

      <div className="auth-divider" aria-hidden="true">
        <span>or</span>
      </div>

      <Button
        type="button"
        variant="outline"
        className="w-full flex items-center justify-center gap-3"
        onClick={handleGoogleSignIn}
        disabled={loading}
      >
        <GoogleIcon />
        Continue with Google
      </Button>

      <p className="auth-switch">
        New to SkillProof? <Link to="/register">Create your account</Link>
      </p>
    </AuthShell>
  )
}
