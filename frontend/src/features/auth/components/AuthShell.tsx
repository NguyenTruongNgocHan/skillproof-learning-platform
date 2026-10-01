import type { ReactNode } from "react"
import { Link } from "react-router-dom"
import { ArrowLeft, CheckCircle2, ShieldCheck, Sparkles } from "lucide-react"
import BrandLogo from "@/components/ui/BrandLogo"
import ThemeSwitcher from "@/components/ui/ThemeSwitcher"

interface AuthShellProps {
  children: ReactNode
  mode: "login" | "register"
  backTo?: string
  backLabel?: string
}

export default function AuthShell({
  children,
  mode,
  backTo = "/",
  backLabel = "Back to home",
}: AuthShellProps) {
  const isLogin = mode === "login"

  return (
    <div className="auth-layout">
      <aside className="auth-story" aria-label="About SkillProof">
        <BrandLogo />

        <div className="auth-story__body">
          <span className="auth-story__eyebrow">
            <Sparkles size={14} aria-hidden="true" />
            {isLogin ? "WELCOME BACK" : "YOUR PROOF STARTS HERE"}
          </span>

          <h2>
            {isLogin ? (
              <>
                Keep building.
                <br />
                Keep <span>proving.</span>
              </>
            ) : (
              <>
                Build skills.
                <br />
                Make them <span>count.</span>
              </>
            )}
          </h2>

          <p>
            {isLogin
              ? "Your learning, progress and achievements are waiting where you left them."
              : "Learn with purpose, show what you can do, and carry trusted proof of your progress forward."}
          </p>

          <div className="auth-story__points">
            <span>
              <CheckCircle2 size={18} aria-hidden="true" />
              Structured learning with measurable progress
            </span>

            <span>
              <ShieldCheck size={18} aria-hidden="true" />
              Evidence you can confidently share
            </span>

            <span>
              <Sparkles size={18} aria-hidden="true" />
              One place to learn, practice and grow
            </span>
          </div>
        </div>

        <small>© 2026 SkillProof · Build skills. Prove them.</small>
      </aside>

      <main className="auth-main">
        <div className="auth-main__inner">
          <div className="auth-main__mobile-logo">
            <BrandLogo />
          </div>

          <div className="auth-main__nav">
            <Link to={backTo} className="auth-back">
              <ArrowLeft size={16} aria-hidden="true" />
              {backLabel}
            </Link>

            <ThemeSwitcher compact />
          </div>

          {children}
        </div>
      </main>
    </div>
  )
}