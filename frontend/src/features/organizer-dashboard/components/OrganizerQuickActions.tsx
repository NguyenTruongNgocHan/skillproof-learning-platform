import { Plus, HelpCircle, Award, BadgeCheck } from "lucide-react";
import Button from "@/components/ui/Button";

const QUICK_ACTIONS = [
  { icon: Plus, label: "Create Learning Path" },
  { icon: HelpCircle, label: "Create Quiz" },
  { icon: Award, label: "Create Certification" },
  { icon: BadgeCheck, label: "Issue Certificate" },
];

export default function OrganizerQuickActions() {
  return (
    <div>
      <h2
        style={{
          fontSize: "1rem",
          fontWeight: "700",
          color: "var(--fg)",
          margin: "0 0 12px",
        }}
      >
        Quick Actions
      </h2>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(2, 1fr)",
          gap: "12px",
        }}
        className="md:grid-cols-4"
      >
        {QUICK_ACTIONS.map(({ icon: Icon, label }) => (
          <div
            key={label}
            style={{
              background: "var(--surface)",
              borderRadius: "12px",
              padding: "16px",
              border: "1px solid var(--border)",
              cursor: "pointer",
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: "10px",
              textAlign: "center",
            }}
          >
            <div
              style={{
                width: "36px",
                height: "36px",
                borderRadius: "10px",
                background: "var(--brand-soft)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Icon width={18} height={18} color="var(--brand)" />
            </div>
            <span
              style={{
                fontSize: "0.8rem",
                fontWeight: "600",
                color: "var(--fg)",
                lineHeight: "1.3",
              }}
            >
              {label}
            </span>
            <Button variant="outline" size="sm">
              Create
            </Button>
          </div>
        ))}
      </div>
    </div>
  );
}
