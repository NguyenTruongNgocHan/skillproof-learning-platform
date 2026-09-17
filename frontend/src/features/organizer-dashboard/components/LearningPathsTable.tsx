import type { PathRow } from "@/mocks/dashboard/organizerDashboard";

interface LearningPathsTableProps {
  rows: PathRow[];
}

export default function LearningPathsTable({ rows }: LearningPathsTableProps) {
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
        Learning Path Performance
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
              {["Path Name", "Status", "Enrolled", "Completion Rate"].map((h) => (
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
                key={row.name}
                style={{
                  borderBottom: i < rows.length - 1 ? "1px solid var(--border)" : "none",
                }}
              >
                <td
                  style={{
                    padding: "12px 16px",
                    fontSize: "0.875rem",
                    fontWeight: "500",
                    color: "var(--fg)",
                  }}
                >
                  {row.name}
                </td>
                <td style={{ padding: "12px 16px" }}>
                  <span
                    style={{
                      borderRadius: "9999px",
                      padding: "2px 8px",
                      fontSize: "0.72rem",
                      fontWeight: "600",
                      background:
                        row.status === "Published"
                          ? "var(--success-bg)"
                          : "rgba(245,158,11,0.12)",
                      color:
                        row.status === "Published" ? "var(--success)" : "var(--warning)",
                    }}
                  >
                    {row.status}
                  </span>
                </td>
                <td
                  style={{
                    padding: "12px 16px",
                    fontSize: "0.875rem",
                    color: "var(--fg)",
                  }}
                >
                  {row.enrolled !== null ? row.enrolled : "—"}
                </td>
                <td
                  style={{
                    padding: "12px 16px",
                    fontSize: "0.875rem",
                    color: "var(--fg)",
                  }}
                >
                  {row.completion !== null ? row.completion : "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
