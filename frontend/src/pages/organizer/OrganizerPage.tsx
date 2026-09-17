import { BadgeCheck } from "lucide-react";
import AppShell from "@/components/layout/AppShell";
import OrganizerStatsRow from "@/features/organizer-dashboard/components/OrganizerStatsRow";
import OrganizerQuickActions from "@/features/organizer-dashboard/components/OrganizerQuickActions";
import LearningPathsTable from "@/features/organizer-dashboard/components/LearningPathsTable";
import OrganizerRecentActivity from "@/features/organizer-dashboard/components/OrganizerRecentActivity";
import {
  ORGANIZER_STATS,
  PATH_ROWS,
  RECENT_ACTIVITY,
} from "@/mocks/dashboard/organizerDashboard";

export default function OrganizerPage() {
  return (
    <AppShell>
      <div
        style={{
          maxWidth: "960px",
          margin: "0 auto",
          display: "flex",
          flexDirection: "column",
          gap: "24px",
        }}
      >
        {/* Header row */}
        <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
          <h1
            style={{
              fontSize: "1.5rem",
              fontWeight: "700",
              color: "var(--fg)",
              margin: 0,
            }}
          >
            SkillProof Academy
          </h1>
          <span
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: "4px",
              background: "var(--success-bg)",
              color: "var(--success)",
              borderRadius: "9999px",
              padding: "4px 12px",
              fontSize: "0.75rem",
              fontWeight: "500",
            }}
          >
            <BadgeCheck width={12} height={12} />
            Verified Organization
          </span>
        </div>

        <OrganizerStatsRow stats={ORGANIZER_STATS} />
        <OrganizerQuickActions />
        <LearningPathsTable rows={PATH_ROWS} />
        <OrganizerRecentActivity items={RECENT_ACTIVITY} />
      </div>
    </AppShell>
  );
}
