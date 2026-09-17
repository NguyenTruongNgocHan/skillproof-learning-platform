import Button from "@/components/ui/Button";
import type { MockLearningPath } from "@/mocks/dashboard/learnerDashboard";

interface ContinueLearningCardProps {
  path: MockLearningPath;
}

export default function ContinueLearningCard({ path }: ContinueLearningCardProps) {
  return (
    <div
      style={{
        background: "var(--surface)",
        borderRadius: "16px",
        padding: "20px",
        boxShadow: "0 1px 3px rgba(0,0,0,0.06)",
        border: "1px solid var(--border)",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "10px",
          marginBottom: "10px",
        }}
      >
        <span
          style={{
            fontWeight: "600",
            color: "var(--fg)",
            fontSize: "0.9375rem",
          }}
        >
          {path.title}
        </span>
        <span
          style={{
            background: "var(--brand-soft)",
            color: "var(--brand)",
            borderRadius: "9999px",
            padding: "2px 10px",
            fontSize: "0.75rem",
            fontWeight: "700",
          }}
        >
          {path.progress}%
        </span>
      </div>
      <div
        style={{
          width: "100%",
          height: "8px",
          background: "var(--bg-subtle)",
          borderRadius: "9999px",
          overflow: "hidden",
          marginBottom: "10px",
        }}
      >
        <div
          style={{
            width: `${path.progress}%`,
            height: "100%",
            background: "var(--brand)",
            borderRadius: "9999px",
          }}
        />
      </div>
      <p
        style={{
          color: "var(--fg-muted)",
          fontSize: "0.85rem",
          margin: "0 0 16px",
        }}
      >
        Next up:{" "}
        <strong style={{ color: "var(--fg)" }}>{path.nextLesson}</strong>
      </p>
      <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
        <Button variant="primary" size="sm">
          Continue Learning
        </Button>
        <Button variant="ghost" size="sm">
          View Path
        </Button>
      </div>
    </div>
  );
}
