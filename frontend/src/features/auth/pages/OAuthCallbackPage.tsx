import { useAuth } from "@/features/auth/hooks/useAuth"
import { safeDestination } from "@/features/auth/utils/authDestination"
import { getNextRouteAfterLogin } from "@/features/auth/utils/authFlow"
import LoadingSpinner from "@/shared/components/feedback/LoadingSpinner"
import BrandLogo from "@/shared/ui/BrandLogo"
import { useToast } from "@/shared/ui/Toast"
import { useEffect } from "react"
import { useNavigate } from "react-router-dom"

export default function OAuthCallbackPage() {
  const navigate = useNavigate()
  const { refreshSession } = useAuth()
  const { toast } = useToast()

  useEffect(() => {
    refreshSession()
      .then((user) => {
        if (!user) throw new Error("OAuth session was not created.")
        toast("success", "Google sign-in completed successfully.")
        const destination = safeDestination(sessionStorage.getItem("skillproof.auth.returnTo"))
        if (user.emailVerificationStatus !== "UNVERIFIED")
          sessionStorage.removeItem("skillproof.auth.returnTo")
        navigate(
          user.emailVerificationStatus === "UNVERIFIED"
            ? getNextRouteAfterLogin(user)
            : (destination ?? getNextRouteAfterLogin(user)),
          { replace: true },
        )
      })
      .catch(() => navigate("/login?oauth=failed", { replace: true }))
  }, [navigate, refreshSession, toast])

  return (
    <main
      className="min-h-screen flex items-center justify-center"
      style={{ background: "var(--bg)" }}
    >
      <div className="oauth-callback-card">
        <BrandLogo />
        <div className="oauth-loader">
          <LoadingSpinner size={24} />
        </div>
        <h1>Securing your SkillProof session</h1>
        <p>
          Google has confirmed your identity. We are preparing the correct workspace for your role.
        </p>
      </div>
    </main>
  )
}
