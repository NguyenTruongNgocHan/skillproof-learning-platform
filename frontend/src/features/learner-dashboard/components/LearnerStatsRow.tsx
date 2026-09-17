import { Target, Clock } from "lucide-react";
import type { CAREER_GOAL, WEEKLY_GOAL } from "@/mocks/dashboard/learnerDashboard";

interface LearnerStatsRowProps {
  careerGoal: typeof CAREER_GOAL;
  weeklyGoal: typeof WEEKLY_GOAL;
}

export default function LearnerStatsRow({ careerGoal, weeklyGoal }: LearnerStatsRowProps) {
  const weeklyPct = Math.round((weeklyGoal.completed / weeklyGoal.target) * 100);

  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
      {/* Learning Goal card */}
      <div
        style={{
          background: "var(--surface)",
          borderRadius: "16px",
          padding: "16px",
          border: "1px solid var(--border)",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "8px",
            marginBottom: "10px",
          }}
        >
          <div
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "8px",
              background: "var(--brand-soft)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Target width={16} height={16} color="var(--brand)" />
          </div>
          <span style={{ fontSize: "0.8rem", color: "var(--fg-muted)", fontWeight: "500" }}>
            Learning Goal
          </span>
        </div>
        <div style={{ fontWeight: "700", fontSize: "1rem", color: "var(--fg)" }}>
          {careerGoal.targetRole}
        </div>
        <div style={{ marginTop: "8px" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              fontSize: "0.75rem",
              color: "var(--fg-muted)",
              marginBottom: "4px",
            }}
          >
            <span>Progress</span>
            <span>68%</span>
          </div>
          <div
            style={{
              height: "4px",
              background: "var(--bg-subtle)",
              borderRadius: "9999px",
            }}
          >
            <div
              style={{
                width: "68%",
                height: "100%",
                background: "var(--brand)",
                borderRadius: "9999px",
              }}
            />
          </div>
        </div>
      </div>

      {/* Weekly hours card */}
      <div
        style={{
          background: "var(--surface)",
          borderRadius: "16px",
          padding: "16px",
          border: "1px solid var(--border)",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: "8px",
            marginBottom: "10px",
          }}
        >
          <div
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "8px",
              background: "var(--brand-soft)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Clock width={16} height={16} color="var(--brand)" />
          </div>
          <span style={{ fontSize: "0.8rem", color: "var(--fg-muted)", fontWeight: "500" }}>
            This Week
          </span>
        </div>
        <div style={{ fontWeight: "700", fontSize: "1rem", color: "var(--fg)" }}>
          {weeklyGoal.completed} / {weeklyGoal.target} {weeklyGoal.unit}
        </div>
        <div style={{ marginTop: "8px" }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              fontSize: "0.75rem",
              color: "var(--fg-muted)",
              marginBottom: "4px",
            }}
          >
            <span>Weekly goal</span>
            <span>{weeklyPct}%</span>
          </div>
          <div
            style={{
              height: "4px",
              background: "var(--bg-subtle)",
              borderRadius: "9999px",
            }}
          >
            <div
              style={{
                width: `${weeklyPct}%`,
                height: "100%",
                background: "var(--brand)",
                borderRadius: "9999px",
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
