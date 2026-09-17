import { HelpCircle, ClipboardList, Zap } from "lucide-react";
import Button from "@/components/ui/Button";

const PRACTICE_ITEMS = [
  {
    icon: HelpCircle,
    title: "Practice Quiz",
    desc: "Sharpen your knowledge with topic quizzes",
    btn: "Start Quiz",
  },
  {
    icon: ClipboardList,
    title: "Mock Test",
    desc: "Simulate real interview conditions",
    btn: "Take Test",
  },
  {
    icon: Zap,
    title: "Challenge",
    desc: "Real-time competitive coding problem",
    btn: "Join Challenge",
  },
];

export default function PracticeSection() {
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
        Practice
      </h2>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, 1fr)",
          gap: "16px",
        }}
      >
        {PRACTICE_ITEMS.map(({ icon: Icon, title, desc, btn }) => (
          <div
            key={title}
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
            <div
              style={{
                width: "36px",
                height: "36px",
                borderRadius: "10px",
                background: "var(--brand-soft)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <Icon width={18} height={18} color="var(--brand)" />
            </div>
            <div style={{ fontWeight: "600", color: "var(--fg)", fontSize: "0.875rem" }}>
              {title}
            </div>
            <div
              style={{
                fontSize: "0.78rem",
                color: "var(--fg-muted)",
                lineHeight: "1.4",
              }}
            >
              {desc}
            </div>
            <div style={{ marginTop: "4px" }}>
              <Button variant="primary" size="sm">
                {btn}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
