import type { StatCard } from "@/mocks/dashboard/organizerDashboard";

interface OrganizerStatsRowProps {
  stats: StatCard[];
}

export default function OrganizerStatsRow({ stats }: OrganizerStatsRowProps) {
  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(2, 1fr)",
        gap: "12px",
      }}
      className="md:grid-cols-4"
    >
      {stats.map(({ icon: Icon, iconColor, iconBg, label, value }) => (
        <div
          key={label}
          style={{
            background: "var(--surface)",
            borderRadius: "12px",
            padding: "16px",
            border: "1px solid var(--border)",
            display: "flex",
            flexDirection: "column",
            gap: "10px",
          }}
        >
          <div
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "8px",
              background: iconBg,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Icon width={16} height={16} color={iconColor} />
          </div>
          <div>
            <div
              style={{
                fontSize: "1.5rem",
                fontWeight: "700",
                color: "var(--fg)",
                lineHeight: 1,
              }}
            >
              {value}
            </div>
            <div
              style={{
                fontSize: "0.8rem",
                color: "var(--fg-muted)",
                marginTop: "4px",
              }}
            >
              {label}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
