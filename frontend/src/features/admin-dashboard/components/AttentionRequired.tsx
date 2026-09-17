import { AlertTriangle } from "lucide-react";
import Button from "@/components/ui/Button";
import type { PendingOrg, PendingPath, ReportedItem } from "@/mocks/dashboard/adminDashboard";

interface AttentionRequiredProps {
  pendingOrgs: PendingOrg[];
  pendingPaths: PendingPath[];
  reportedContent: ReportedItem[];
}

export default function AttentionRequired({
  pendingOrgs,
  pendingPaths,
  reportedContent,
}: AttentionRequiredProps) {
  return (
    <div
      style={{
        background: "var(--surface)",
        borderRadius: "12px",
        border: "1px solid var(--border)",
        padding: "20px",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "8px",
          marginBottom: "18px",
        }}
      >
        <AlertTriangle width={16} height={16} color="var(--warning)" />
        <h2
          style={{
            fontSize: "1rem",
            fontWeight: "700",
            color: "var(--fg)",
            margin: 0,
          }}
        >
          Attention Required
        </h2>
      </div>

      {/* Pending org approvals */}
      <div style={{ marginBottom: "20px" }}>
        <div
          style={{
            fontWeight: "600",
            color: "var(--fg-muted)",
            marginBottom: "10px",
            textTransform: "uppercase",
            letterSpacing: "0.04em",
            fontSize: "0.7rem",
          }}
        >
          Pending Organization Approvals ({pendingOrgs.length})
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
          {pendingOrgs.map((org) => (
            <div
              key={org.name}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "10px",
                padding: "10px 12px",
                background: "var(--bg-subtle)",
                borderRadius: "8px",
                flexWrap: "wrap",
              }}
            >
              <div style={{ flex: 1, minWidth: "150px" }}>
                <div style={{ fontWeight: "600", fontSize: "0.875rem", color: "var(--fg)" }}>
                  {org.name}
                </div>
                <div style={{ fontSize: "0.75rem", color: "var(--fg-muted)" }}>
                  Submitted {org.submitted}
                </div>
              </div>
              <span
                style={{
                  background: "rgba(245,158,11,0.12)",
                  color: "var(--warning)",
                  borderRadius: "9999px",
                  padding: "2px 8px",
                  fontSize: "0.72rem",
                  fontWeight: "600",
                }}
              >
                Pending
              </span>
              <Button variant="outline" size="sm">
                Review
              </Button>
            </div>
          ))}
        </div>
      </div>

      {/* Paths awaiting review */}
      <div style={{ marginBottom: "20px" }}>
        <div
          style={{
            fontSize: "0.7rem",
            fontWeight: "600",
            color: "var(--fg-muted)",
            marginBottom: "10px",
            textTransform: "uppercase",
            letterSpacing: "0.04em",
          }}
        >
          Paths Awaiting Review ({pendingPaths.length})
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
          {pendingPaths.map((path) => (
            <div
              key={path.name}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "10px",
                padding: "10px 12px",
                background: "var(--bg-subtle)",
                borderRadius: "8px",
                flexWrap: "wrap",
              }}
            >
              <div style={{ flex: 1, minWidth: "150px" }}>
                <div style={{ fontWeight: "600", fontSize: "0.875rem", color: "var(--fg)" }}>
                  {path.name}
                </div>
                <div style={{ fontSize: "0.75rem", color: "var(--fg-muted)" }}>
                  Submitted by {path.org}
                </div>
              </div>
              <span
                style={{
                  background: "rgba(245,158,11,0.12)",
                  color: "var(--warning)",
                  borderRadius: "9999px",
                  padding: "2px 8px",
                  fontSize: "0.72rem",
                  fontWeight: "600",
                }}
              >
                Pending
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Reported community content */}
      <div>
        <div
          style={{
            fontSize: "0.7rem",
            fontWeight: "600",
            color: "var(--fg-muted)",
            marginBottom: "10px",
            textTransform: "uppercase",
            letterSpacing: "0.04em",
          }}
        >
          Reported Community Content ({reportedContent.length})
        </div>
        {reportedContent.map((item) => (
          <div
            key={item.name}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "10px",
              padding: "10px 12px",
              background: "var(--bg-subtle)",
              borderRadius: "8px",
              flexWrap: "wrap",
            }}
          >
            <div style={{ flex: 1, minWidth: "150px" }}>
              <div style={{ fontWeight: "600", fontSize: "0.875rem", color: "var(--fg)" }}>
                {item.name}
              </div>
              <div style={{ fontSize: "0.75rem", color: "var(--fg-muted)" }}>{item.reason}</div>
            </div>
            <span
              style={{
                background: "rgba(239,68,68,0.12)",
                color: "#ef4444",
                borderRadius: "9999px",
                padding: "2px 8px",
                fontSize: "0.72rem",
                fontWeight: "600",
              }}
            >
              Reported
            </span>
            <Button variant="outline" size="sm">
              Review
            </Button>
          </div>
        ))}
      </div>
    </div>
  );
}
