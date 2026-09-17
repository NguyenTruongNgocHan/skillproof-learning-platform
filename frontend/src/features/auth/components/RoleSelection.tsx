import { Link } from "react-router-dom";
import { GraduationCap, Building2, ArrowRight } from "lucide-react";
import Button from "@/components/ui/Button";

type Role = "LEARNER" | "ORGANIZER";

interface RoleSelectionProps {
  selected: Role | null;
  onSelect: (role: Role) => void;
  onContinue: () => void;
}

export default function RoleSelection({ selected, onSelect, onContinue }: RoleSelectionProps) {
  return (
    <div
      className="min-h-screen flex items-center justify-center p-6"
      style={{ background: "var(--bg)" }}
    >
      <div className="w-full max-w-lg flex flex-col gap-8">
        <div className="text-center">
          <h1 className="text-2xl font-bold" style={{ color: "var(--fg)" }}>
            Create your account
          </h1>
          <p className="text-sm mt-2" style={{ color: "var(--fg-muted)" }}>
            Choose how you want to use SkillProof
          </p>
        </div>

        <div className="flex flex-col sm:flex-row gap-4">
          <button
            type="button"
            onClick={() => onSelect("LEARNER")}
            className="flex-1 flex flex-col items-start gap-3 rounded-xl p-6 text-left cursor-pointer border transition-all"
            style={{
              borderColor: selected === "LEARNER" ? "var(--brand)" : "var(--border)",
              background:
                selected === "LEARNER"
                  ? "color-mix(in srgb, var(--brand) 8%, var(--bg))"
                  : "var(--surface)",
            }}
          >
            <GraduationCap size={32} style={{ color: "var(--brand)" }} />
            <div>
              <p className="font-semibold text-base" style={{ color: "var(--fg)" }}>
                {"I'm a Learner"}
              </p>
              <p className="text-sm mt-1" style={{ color: "var(--fg-muted)" }}>
                I want to learn, practice and earn verifiable credentials.
              </p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => onSelect("ORGANIZER")}
            className="flex-1 flex flex-col items-start gap-3 rounded-xl p-6 text-left cursor-pointer border transition-all"
            style={{
              borderColor: selected === "ORGANIZER" ? "var(--brand)" : "var(--border)",
              background:
                selected === "ORGANIZER"
                  ? "color-mix(in srgb, var(--brand) 8%, var(--bg))"
                  : "var(--surface)",
            }}
          >
            <Building2 size={32} style={{ color: "var(--brand)" }} />
            <div>
              <p className="font-semibold text-base" style={{ color: "var(--fg)" }}>
                {"I'm an Organizer"}
              </p>
              <p className="text-sm mt-1" style={{ color: "var(--fg-muted)" }}>
                I represent an organization that provides learning or certification.
              </p>
            </div>
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
          <ArrowRight size={16} />
        </Button>

        <p className="text-center text-sm" style={{ color: "var(--fg-muted)" }}>
          Already have an account?{" "}
          <Link to="/login" className="font-medium" style={{ color: "var(--brand)" }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
