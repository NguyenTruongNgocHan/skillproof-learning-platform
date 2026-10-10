import AuthShell from "@/features/auth/components/AuthShell"
import PasswordRequirements from "@/features/auth/components/PasswordRequirements"
import type { RegisterOrganizerData } from "@/features/auth/types/auth.types"
import {
  checkPassword,
  getPasswordStrength,
  isPasswordValid,
} from "@/features/auth/validation/passwordPolicy"
import LoadingSpinner from "@/shared/components/feedback/LoadingSpinner"
import Button from "@/shared/ui/Button"
import Checkbox from "@/shared/ui/Checkbox"
import Input from "@/shared/ui/Input"
import PasswordInput from "@/shared/ui/PasswordInput"
import { ArrowRight } from "lucide-react"
import { useState } from "react"
import { Link } from "react-router-dom"

interface OrganizerRegistrationFormProps {
  onSubmit: (data: RegisterOrganizerData) => Promise<void>
  onBack: () => void
  loading: boolean
}

export default function OrganizerRegistrationForm({
  onSubmit,
  onBack,
  loading,
}: OrganizerRegistrationFormProps) {
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [agreedToTerms, setAgreedToTerms] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const passwordChecks = checkPassword(password)
  const passwordValid = isPasswordValid(passwordChecks)
  const passwordStrength = getPasswordStrength(password)

  const passwordsMatch = password !== "" && confirmPassword !== "" && password === confirmPassword

  const confirmError =
    confirmPassword !== "" && !passwordsMatch ? "Those passwords don't match yet." : null

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
        err instanceof Error ? err.message : "We couldn't create your account. Please try again.",
      )
    }
  }

  return (
    <AuthShell
      mode="register"
      backTo="/register"
      backAction={onBack}
      backLabel="Choose another path"
    >
      <div className="auth-intro auth-intro--compact">
        <h1>Start with your account</h1>

        <p>
          Create your personal sign-in first. After verifying your email, you'll be guided through
          your organization application.
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
          label="Work email"
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

        <div className="auth-context-note">
          After verifying your email, you'll tell us about your organization before it is submitted
          for review.
        </div>

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
          className="w-full flex items-center justify-center gap-2"
          disabled={!formValid || loading}
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <LoadingSpinner size={16} />
              Creating your account…
            </span>
          ) : (
            <>
              Create account
              <ArrowRight size={16} aria-hidden="true" />
            </>
          )}
        </Button>
      </form>

      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </AuthShell>
  )
}
