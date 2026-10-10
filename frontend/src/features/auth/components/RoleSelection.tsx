import AuthShell from "@/features/auth/components/AuthShell"
import Button from "@/shared/ui/Button"
import { ArrowRight, Building2, Check, GraduationCap } from "lucide-react"
import { Link } from "react-router-dom"

type Role = "LEARNER" | "ORGANIZER"

interface RoleSelectionProps {
  selected: Role | null
  onSelect: (role: Role) => void
  onContinue: () => void
}

export default function RoleSelection({ selected, onSelect, onContinue }: RoleSelectionProps) {
  return (
    <AuthShell mode="register">
      <div className="auth-intro">
        <span className="auth-intro__eyebrow">CREATE YOUR ACCOUNT</span>

        <h1>How will you use SkillProof?</h1>

        <p>
          Choose the experience that fits you now. This helps us take you to the right place after
          you join.
        </p>
      </div>

      <div className="auth-role-grid">
        <button
          type="button"
          className={`auth-role-card ${selected === "LEARNER" ? "auth-role-card--selected" : ""}`}
          onClick={() => onSelect("LEARNER")}
          aria-pressed={selected === "LEARNER"}
        >
          <div className="auth-role-card__top">
            <span className="auth-role-card__icon">
              <GraduationCap size={22} aria-hidden="true" />
            </span>

            {selected === "LEARNER" && (
              <span className="auth-role-card__check">
                <Check size={14} aria-hidden="true" />
              </span>
            )}
          </div>

          <strong>I'm here to learn</strong>

          <p>Follow learning paths, practice your skills and build evidence of what you can do.</p>
        </button>

        <button
          type="button"
          className={`auth-role-card ${selected === "ORGANIZER" ? "auth-role-card--selected" : ""}`}
          onClick={() => onSelect("ORGANIZER")}
          aria-pressed={selected === "ORGANIZER"}
        >
          <div className="auth-role-card__top">
            <span className="auth-role-card__icon">
              <Building2 size={22} aria-hidden="true" />
            </span>

            {selected === "ORGANIZER" && (
              <span className="auth-role-card__check">
                <Check size={14} aria-hidden="true" />
              </span>
            )}
          </div>

          <strong>I'm here to organize</strong>

          <p>Create trusted learning experiences for your organization and its learners.</p>
        </button>
      </div>

      <Button
        type="button"
        variant="primary"
        className="w-full flex items-center justify-center gap-2"
        disabled={selected === null}
        onClick={onContinue}
      >
        Continue
        <ArrowRight size={16} aria-hidden="true" />
      </Button>

      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </AuthShell>
  )
}
