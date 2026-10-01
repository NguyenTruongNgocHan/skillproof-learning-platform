import { useEffect, useRef, useState } from "react"
import type { FormEvent, ReactNode } from "react"
import {
  Link,
  useLocation,
  useNavigate,
  useSearchParams,
} from "react-router-dom"
import { AlertCircle, CheckCircle2, MailCheck } from "lucide-react"

import { useAuth } from "@/features/auth/hooks/useAuth"
import { useToast } from "@/components/ui/Toast"
import Button from "@/components/ui/Button"
import Input from "@/components/ui/Input"
import LoadingSpinner from "@/components/feedback/LoadingSpinner"

type VerificationState = "waiting" | "verifying" | "success" | "error"

interface VerifyEmailLocationState {
  email?: string
}

export default function VerifyEmailPage() {
  const { user, verifyEmail, resendVerification } = useAuth()
  const { toast } = useToast()

  const location = useLocation()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()

  const token = searchParams.get("token")
  const handledToken = useRef<string | null>(null)

  const locationState = location.state as VerifyEmailLocationState | null

  const registrationEmail = locationState?.email?.trim() ?? ""
  const authenticatedEmail = user?.email?.trim() ?? ""

  const knownEmail = registrationEmail || authenticatedEmail
  const hasKnownEmail = Boolean(knownEmail)

  const [state, setState] = useState<VerificationState>(
    token ? "verifying" : "waiting",
  )
  const [email, setEmail] = useState(knownEmail)
  const [error, setError] = useState("")
  const [resending, setResending] = useState(false)

  useEffect(() => {
    if (!email && knownEmail) {
      setEmail(knownEmail)
    }
  }, [email, knownEmail])

  useEffect(() => {
    if (!token || handledToken.current === token) return

    handledToken.current = token
    setState("verifying")
    setError("")

    verifyEmail(token)
      .then(() => {
        setState("success")

        toast(
          "success",
          "Your email has been verified. You can now sign in.",
        )
      })
      .catch((reason: unknown) => {
        setState("error")

        setError(
          reason instanceof Error
            ? reason.message
            : "This verification link is invalid or has expired.",
        )
      })
  }, [token, toast, verifyEmail])

  async function handleResend(event: FormEvent) {
    event.preventDefault()

    const normalizedEmail = email.trim()

    if (!normalizedEmail || resending) return

    setResending(true)

    try {
      await resendVerification(normalizedEmail)

      toast(
        "success",
        "If the account is eligible, a new verification email has been sent.",
      )
    } catch (reason: unknown) {
      toast(
        "error",
        reason instanceof Error
          ? reason.message
          : "The verification email could not be sent.",
      )
    } finally {
      setResending(false)
    }
  }

  return (
    <main
      className="flex min-h-screen items-center justify-center px-4 py-12"
      style={{ background: "var(--bg)" }}
    >
      <section
        className="w-full max-w-lg rounded-2xl border p-8 text-center"
        style={{
          background: "var(--surface)",
          borderColor: "var(--border)",
          boxShadow: "var(--shadow-lg)",
        }}
      >
        {state === "verifying" && (
          <StatePanel
            icon={<LoadingSpinner size={42} />}
            title="Verifying your email"
            description="Please wait while SkillProof validates your secure verification link."
          />
        )}

        {state === "success" && (
          <>
            <StatePanel
              icon={<CheckCircle2 size={48} color="var(--success)" />}
              title="Email verified"
              description="Your account is active and ready to use."
            />

            <Button
              className="mt-7 w-full"
              size="lg"
              onClick={() =>
                navigate("/login?verified=true", { replace: true })
              }
            >
              Continue to sign in
            </Button>
          </>
        )}

        {state === "error" && (
          <>
            <StatePanel
              icon={<AlertCircle size={48} color="var(--error)" />}
              title="That link didn't work"
              description={
                error ||
                "This verification link may be invalid, expired, or already used."
              }
            />

            {hasKnownEmail ? (
              <>
                <KnownEmail email={email} />

                <form onSubmit={handleResend} className="mt-7">
                  <Button
                    type="submit"
                    variant="outline"
                    className="w-full"
                    disabled={resending}
                  >
                    {resending
                      ? "Sending…"
                      : "Send me a new verification link"}
                  </Button>
                </form>
              </>
            ) : (
              <ResendForm
                email={email}
                onEmailChange={setEmail}
                onSubmit={handleResend}
                loading={resending}
                buttonLabel="Send me a new verification link"
              />
            )}
          </>
        )}

        {state === "waiting" && (
          <>
            <StatePanel
              icon={<MailCheck size={48} color="var(--brand)" />}
              title="Check your email"
              description={
                hasKnownEmail
                  ? "We sent a secure verification link to:"
                  : "Open the secure link in your email to activate your SkillProof account."
              }
            />

            {hasKnownEmail ? (
              <>
                <KnownEmail email={email} />

                <p
                  className="mt-3 text-sm leading-6"
                  style={{ color: "var(--fg-muted)" }}
                >
                  The link expires after 30 minutes and can only be used once.
                </p>

                <form onSubmit={handleResend} className="mt-7">
                  <Button
                    type="submit"
                    variant="outline"
                    className="w-full"
                    disabled={resending}
                  >
                    {resending ? "Sending…" : "Resend verification email"}
                  </Button>
                </form>
              </>
            ) : (
              <>
                <p
                  className="mt-3 text-sm leading-6"
                  style={{ color: "var(--fg-muted)" }}
                >
                  If you need another verification email, enter the address you
                  used to create your account.
                </p>

                <ResendForm
                  email={email}
                  onEmailChange={setEmail}
                  onSubmit={handleResend}
                  loading={resending}
                  buttonLabel="Send verification email"
                />
              </>
            )}
          </>
        )}

        {state !== "verifying" && state !== "success" && (
          <Link
            to="/login"
            className="mt-5 inline-block text-sm font-medium"
            style={{ color: "var(--brand)" }}
          >
            Back to sign in
          </Link>
        )}
      </section>
    </main>
  )
}

function StatePanel({
  icon,
  title,
  description,
}: {
  icon: ReactNode
  title: string
  description: string
}) {
  return (
    <div className="flex flex-col items-center gap-3">
      {icon}

      <h1 className="text-2xl font-bold" style={{ color: "var(--fg)" }}>
        {title}
      </h1>

      <p
        className="text-sm leading-6"
        style={{ color: "var(--fg-muted)" }}
      >
        {description}
      </p>
    </div>
  )
}

function KnownEmail({ email }: { email: string }) {
  return (
    <div
      className="mt-4 rounded-xl border px-4 py-3"
      style={{
        background: "var(--bg)",
        borderColor: "var(--border)",
      }}
    >
      <p
        className="break-all text-sm font-semibold"
        style={{ color: "var(--fg)" }}
      >
        {email}
      </p>
    </div>
  )
}

function ResendForm({
  email,
  onEmailChange,
  onSubmit,
  loading,
  buttonLabel = "Resend verification email",
}: {
  email: string
  onEmailChange: (value: string) => void
  onSubmit: (event: FormEvent) => void
  loading: boolean
  buttonLabel?: string
}) {
  return (
    <form onSubmit={onSubmit} className="mt-7 space-y-4 text-left">
      <Input
        label="Email address"
        type="email"
        value={email}
        onChange={(event) => onEmailChange(event.target.value)}
        autoComplete="email"
        required
      />

      <Button
        type="submit"
        variant="outline"
        className="w-full"
        disabled={loading || !email.trim()}
      >
        {loading ? "Sending…" : buttonLabel}
      </Button>
    </form>
  )
}