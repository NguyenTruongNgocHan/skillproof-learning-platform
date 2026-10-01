import { useState } from "react"
import { Link } from "react-router-dom"
import {
  checkPassword,
  getPasswordStrength,
  isPasswordValid,
} from "@/features/auth/validation/passwordPolicy"
import AuthShell from "@/features/auth/components/AuthShell"
import PasswordRequirements from "@/features/auth/components/PasswordRequirements"
import Input from "@/components/ui/Input"
import PasswordInput from "@/components/ui/PasswordInput"
import Button from "@/components/ui/Button"
import Checkbox from "@/components/ui/Checkbox"
import LoadingSpinner from "@/components/feedback/LoadingSpinner"
import type { RegisterLearnerData } from "@/features/auth/types/auth.types"

interface LearnerRegistrationFormProps {
  onSubmit: (data: RegisterLearnerData) => Promise<void>
  onGoogleClick: () => Promise<void>
  onBack: () => void
  loading: boolean
}

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

export default function LearnerRegistrationForm({
  onSubmit,
  onGoogleClick,
  onBack,
  loading,
}: LearnerRegistrationFormProps) {
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [agreedToTerms, setAgreedToTerms] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const passwordChecks = checkPassword(password)
  const passwordValid = isPasswordValid(passwordChecks)
  const passwordStrength = getPasswordStrength(password)

  const passwordsMatch =
    password !== "" &&
    confirmPassword !== "" &&
    password === confirmPassword

  const confirmError =
    confirmPassword !== "" && !passwordsMatch
      ? "Those passwords don't match yet."
      : null

  const formValid =
    fullName.trim() !== "" &&
    email.trim() !== "" &&
    passwordValid &&
    passwordsMatch &&
    agreedToTerms

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault()

    if (!formValid) return

    setError(null)

    try {
      await onSubmit({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
      })
    } catch (err: unknown) {
      setError(
        err instanceof Error
          ? err.message
          : "We couldn't create your account. Please try again.",
      )
    }
  }

  return (
    <AuthShell
      mode="register"
      backTo="/register"
      backLabel="Choose another path"
    >
      <div className="auth-intro auth-intro--compact">
        <h1>Create your learner account</h1>

        <p>
          Build your learning record and turn progress into proof you can carry
          forward.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="auth-form-stack">
        <Input
          label="Full name"
          type="text"
          autoComplete="name"
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          required
        />

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
            autoComplete="new-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          {password !== "" && (
            <PasswordRequirements
              checks={passwordChecks}
              strength={passwordStrength}
              password={password}
            />
          )}
        </div>

        <PasswordInput
          label="Confirm password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          required
          error={confirmError ?? undefined}
        />

        <Checkbox
          checked={agreedToTerms}
          onChange={(event) => setAgreedToTerms(event.target.checked)}
          label={
            <span style={{ color: "var(--fg-muted)" }}>
              I agree to the{" "}
              <Link
                to="/terms"
                target="_blank"
                rel="noopener noreferrer"
                onClick={(event) => event.stopPropagation()}
              >
                Terms of Service
              </Link>{" "}
              and{" "}
              <Link
                to="/privacy"
                target="_blank"
                rel="noopener noreferrer"
                onClick={(event) => event.stopPropagation()}
              >
                Privacy Policy
              </Link>
              .
            </span>
          }
        />

        {error && (
          <div className="auth-message auth-message--error" role="alert">
            {error}
          </div>
        )}

        <Button
          type="submit"
          variant="primary"
          className="w-full"
          disabled={!formValid || loading}
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <LoadingSpinner size={16} />
              Creating your account…
            </span>
          ) : (
            "Create my account"
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
        onClick={onGoogleClick}
        disabled={loading}
      >
        <GoogleIcon />
        Continue with Google
      </Button>

      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </AuthShell>
  )
}