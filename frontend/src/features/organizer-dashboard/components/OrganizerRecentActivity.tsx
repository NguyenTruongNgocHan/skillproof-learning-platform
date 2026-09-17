import type { ActivityItem } from "@/mocks/dashboard/organizerDashboard";

interface OrganizerRecentActivityProps {
  items: ActivityItem[];
}

export default function OrganizerRecentActivity({ items }: OrganizerRecentActivityProps) {
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
        Recent Activity
      </h2>
      <div
        style={{
          background: "var(--surface)",
          borderRadius: "12px",
          border: "1px solid var(--border)",
          padding: "4px 0",
        }}
      >
        {items.map((item, i) => (
          <div
            key={i}
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              padding: "12px 16px",
              borderBottom: i < items.length - 1 ? "1px solid var(--border)" : "none",
              gap: "12px",
            }}
          >
            <span
              style={{
                fontSize: "0.875rem",
                color: "var(--fg)",
                lineHeight: "1.4",
              }}
            >
              {item.text}
            </span>
            <span
              style={{
                fontSize: "0.75rem",
                color: "var(--fg-muted)",
                whiteSpace: "nowrap",
                flexShrink: 0,
              }}
            >
              {item.time}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
