import AppShell from "@/components/layout/AppShell";
import AdminMetricsRow from "@/features/admin-dashboard/components/AdminMetricsRow";
import AttentionRequired from "@/features/admin-dashboard/components/AttentionRequired";
import SystemActivity from "@/features/admin-dashboard/components/SystemActivity";
import {
  ADMIN_METRICS,
  PENDING_ORGS,
  PENDING_PATHS,
  REPORTED_CONTENT,
  ACTIVITY_ROWS,
} from "@/mocks/dashboard/adminDashboard";

export default function AdminPage() {
  return (
    <AppShell>
      <div
        style={{
          maxWidth: "1024px",
          margin: "0 auto",
          display: "flex",
          flexDirection: "column",
          gap: "24px",
        }}
      >
        {/* Header */}
        <div>
          <h1
            style={{
              fontSize: "1.5rem",
              fontWeight: "700",
              color: "var(--fg)",
              margin: 0,
            }}
          >
            Admin Overview
          </h1>
          <p style={{ color: "var(--fg-muted)", margin: "4px 0 0", fontSize: "0.9rem" }}>
            SkillProof Platform
          </p>
        </div>

        <AdminMetricsRow metrics={ADMIN_METRICS} />
        <AttentionRequired
          pendingOrgs={PENDING_ORGS}
          pendingPaths={PENDING_PATHS}
          reportedContent={REPORTED_CONTENT}
        />
        <SystemActivity rows={ACTIVITY_ROWS} />
      </div>
    </AppShell>
  );
}
