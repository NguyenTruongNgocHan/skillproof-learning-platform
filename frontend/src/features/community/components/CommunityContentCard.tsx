import Button from "@/components/ui/Button";
import type { CommunityContentItem } from "@/mocks/community/communityContent";

function difficultyStyle(difficulty: CommunityContentItem["difficulty"]): { background: string; color: string } {
  switch (difficulty) {
    case "Beginner":
      return { background: "var(--success-bg)", color: "var(--success)" };
    case "Intermediate":
      return { background: "#FFFBEB", color: "#B45309" };
    case "Advanced":
      return { background: "#FEF2F2", color: "#DC2626" };
  }
}

interface CommunityContentCardProps {
  item: CommunityContentItem;
  isAuthenticated: boolean;
  onProtectedAction: (item: CommunityContentItem) => void;
}

export default function CommunityContentCard({
  item,
  isAuthenticated,
  onProtectedAction,
}: CommunityContentCardProps) {
  const typeBadge =
    item.type === "Quiz"
      ? { background: "#EFF6FF", color: "#2563EB" }
      : { background: "#F5F3FF", color: "#7C3AED" };

  const diffStyle = difficultyStyle(item.difficulty);

  return (
    <div
      style={{
        background: "var(--surface)",
        borderRadius: "12px",
        padding: "20px",
        border: "1px solid var(--border)",
        display: "flex",
        flexDirection: "column",
        gap: "12px",
      }}
    >
      {/* Top row: type + free/paid badges */}
      <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
        <span
          style={{
            ...typeBadge,
            fontSize: "0.75rem",
            fontWeight: 600,
            padding: "2px 10px",
            borderRadius: "999px",
          }}
        >
          {item.type}
        </span>
        <span
          style={{
            fontSize: "0.75rem",
            fontWeight: 500,
            padding: "2px 10px",
            borderRadius: "999px",
            ...(item.free
              ? { background: "var(--success-bg)", color: "var(--success)" }
              : { background: "var(--bg-subtle)", color: "var(--fg-muted)" }),
          }}
        >
          {item.free ? "Free" : "Paid"}
        </span>
      </div>

      {/* Title */}
      <p style={{ fontWeight: 600, color: "var(--fg)", fontSize: "1rem", lineHeight: 1.4, margin: 0 }}>
        {item.title}
      </p>

      {/* Creator */}
      <p style={{ fontSize: "0.875rem", color: "var(--fg-muted)", margin: 0 }}>
        by {item.creator}
      </p>

      {/* Topic + Difficulty */}
      <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
        <span
          style={{
            fontSize: "0.75rem",
            fontWeight: 500,
            padding: "2px 10px",
            borderRadius: "999px",
            background: "var(--bg-subtle)",
            color: "var(--fg-muted)",
          }}
        >
          {item.topic}
        </span>
        <span
          style={{
            fontSize: "0.75rem",
            fontWeight: 500,
            padding: "2px 10px",
            borderRadius: "999px",
            ...diffStyle,
          }}
        >
          {item.difficulty}
        </span>
      </div>

      {/* Stats */}
      <p style={{ fontSize: "0.875rem", color: "var(--fg-muted)", margin: 0 }}>
        {item.questions} questions&nbsp;&middot;&nbsp;&#9733; {item.rating} ({item.reviewCount})
      </p>

      {/* CTA */}
      <div style={{ marginTop: "auto" }}>
        <Button
          variant="primary"
          size="sm"
          style={{ width: "100%" }}
          onClick={() => onProtectedAction(item)}
        >
          Start Practice
        </Button>
      </div>
    </div>
  );
}
