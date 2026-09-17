import Button from "@/components/ui/Button";
import type { MockRecommendedPath } from "@/mocks/dashboard/learnerDashboard";

interface RecommendedPathsSectionProps {
  paths: MockRecommendedPath[];
}

export default function RecommendedPathsSection({ paths }: RecommendedPathsSectionProps) {
  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "14px",
        }}
      >
        <h2
          style={{
            fontSize: "1rem",
            fontWeight: "700",
            color: "var(--fg)",
            margin: 0,
          }}
        >
          Recommended for You
        </h2>
        <a
          href="#"
          style={{
            fontSize: "0.85rem",
            color: "var(--brand)",
            textDecoration: "none",
            fontWeight: "500",
          }}
        >
          View all
        </a>
      </div>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))",
          gap: "16px",
        }}
      >
        {paths.map((path) => (
          <div
            key={path.title}
            style={{
              background: "var(--surface)",
              borderRadius: "12px",
              padding: "16px",
              border: "1px solid var(--border)",
              display: "flex",
              flexDirection: "column",
              gap: "8px",
            }}
          >
            <span
              style={{
                background: "var(--brand-soft)",
                color: "var(--brand)",
                borderRadius: "6px",
                padding: "2px 8px",
                fontSize: "0.7rem",
                fontWeight: "600",
                alignSelf: "flex-start",
              }}
            >
              {path.category}
            </span>
            <div style={{ fontWeight: "600", color: "var(--fg)", fontSize: "0.9rem" }}>
              {path.title}
            </div>
            <div style={{ fontSize: "0.8rem", color: "var(--fg-muted)" }}>{path.org}</div>
            <div style={{ fontSize: "0.75rem", color: "var(--fg-muted)" }}>{path.meta}</div>
            <div style={{ marginTop: "4px" }}>
              <Button variant="outline" size="sm">
                Enroll
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
