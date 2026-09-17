import type { ActivityRow } from "@/mocks/dashboard/adminDashboard";

interface SystemActivityProps {
  rows: ActivityRow[];
}

function statusStyle(status: string): { background: string; color: string } {
  switch (status) {
    case "Success":
      return { background: "var(--success-bg)", color: "var(--success)" };
    case "Failed":
      return { background: "rgba(239,68,68,0.12)", color: "#ef4444" };
    case "Pending":
    case "Review":
    default:
      return { background: "rgba(245,158,11,0.12)", color: "var(--warning)" };
  }
}

export default function SystemActivity({ rows }: SystemActivityProps) {
  return (
    <div>
      <h2
        style={{
          fontSize: "1rem",
          fontWeight: "700",
          color: "var(--fg)",
          margin: "0 0 14px",
        }}
      >
        Recent System Activity
      </h2>
      <div
        style={{
          background: "var(--surface)",
          borderRadius: "12px",
          border: "1px solid var(--border)",
          overflow: "hidden",
        }}
      >
        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              {["Action", "User", "Timestamp", "Status"].map((h) => (
                <th
                  key={h}
                  style={{
                    padding: "10px 16px",
                    textAlign: "left",
                    fontSize: "0.7rem",
                    fontWeight: "600",
                    textTransform: "uppercase",
                    letterSpacing: "0.05em",
                    color: "var(--fg-muted)",
                    borderBottom: "1px solid var(--border)",
                  }}
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, i) => (
              <tr
                key={i}
                style={{
                  borderBottom: i < rows.length - 1 ? "1px solid var(--border)" : "none",
                }}
              >
                <td
                  style={{
                    padding: "11px 16px",
                    fontSize: "0.875rem",
                    color: "var(--fg)",
                    fontWeight: "500",
                  }}
                >
                  {row.action}
                </td>
                <td
                  style={{
                    padding: "11px 16px",
                    fontSize: "0.8rem",
                    color: "var(--fg-muted)",
                  }}
                >
                  {row.user}
                </td>
                <td
                  style={{
                    padding: "11px 16px",
                    fontSize: "0.8rem",
                    color: "var(--fg-muted)",
                    whiteSpace: "nowrap",
                  }}
                >
                  {row.time}
                </td>
                <td style={{ padding: "11px 16px" }}>
                  <span
                    style={{
                      borderRadius: "9999px",
                      padding: "2px 8px",
                      fontSize: "0.72rem",
                      fontWeight: "600",
                      ...statusStyle(row.status),
                    }}
                  >
                    {row.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
