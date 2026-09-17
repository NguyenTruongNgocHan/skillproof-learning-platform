import type { MetricCard } from "@/mocks/dashboard/adminDashboard";

interface AdminMetricsRowProps {
  metrics: MetricCard[];
}

export default function AdminMetricsRow({ metrics }: AdminMetricsRowProps) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "12px",
      }}
      className="md:grid-cols-3 lg:grid-cols-6"
    >
      {metrics.map(({ icon: Icon, label, value, color, bg, highlight }) => (
        <div
          key={label}
          style={{
            background: highlight ? "rgba(245,158,11,0.06)" : "var(--surface)",
            borderRadius: "12px",
            padding: "14px",
            border: highlight
              ? "1px solid rgba(245,158,11,0.3)"
              : "1px solid var(--border)",
            display: "flex",
            flexDirection: "column",
            gap: "8px",
          }}
        >
          <div
            style={{
              width: "28px",
              height: "28px",
              borderRadius: "7px",
              background: bg,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Icon width={14} height={14} color={color} />
          </div>
          <div
            style={{
              fontSize: "1.25rem",
              fontWeight: "700",
              color: color,
              lineHeight: 1,
            }}
          >
            {value}
          </div>
          <div style={{ fontSize: "0.72rem", color: "var(--fg-muted)", lineHeight: "1.3" }}>
            {label}
          </div>
        </div>
      ))}
    </div>
  );
}
