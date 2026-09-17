import { Search } from "lucide-react";
import { TOPICS } from "@/mocks/community/communityContent";

type TypeFilter = "all" | "quiz" | "mock-test";

interface CommunityFiltersProps {
  search: string;
  typeFilter: string;
  topicFilter: string;
  onSearchChange: (v: string) => void;
  onTypeChange: (v: string) => void;
  onTopicChange: (v: string) => void;
}

function pillClass(active: boolean) {
  return [
    "px-3 py-1.5 rounded-full text-sm font-medium cursor-pointer border transition-colors",
    active ? "border-transparent text-white" : "border-transparent",
  ].join(" ");
}

export default function CommunityFilters({
  search,
  typeFilter,
  topicFilter,
  onSearchChange,
  onTypeChange,
  onTopicChange,
}: CommunityFiltersProps) {
  return (
    <div
      style={{
        position: "sticky",
        top: "var(--header-height, 60px)",
        zIndex: 9,
        background: "var(--bg)",
        borderBottom: "1px solid var(--border)",
        padding: "12px 16px",
      }}
    >
      <div
        style={{
          maxWidth: "72rem",
          margin: "0 auto",
          display: "flex",
          flexWrap: "wrap",
          gap: "12px",
          alignItems: "center",
        }}
      >
        {/* Search */}
        <div style={{ position: "relative", width: "100%", maxWidth: "320px" }}>
          <Search
            size={16}
            style={{
              position: "absolute",
              left: "10px",
              top: "50%",
              transform: "translateY(-50%)",
              color: "var(--fg-muted)",
              pointerEvents: "none",
            }}
          />
          <input
            type="text"
            placeholder="Search content..."
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            style={{
              width: "100%",
              paddingLeft: "32px",
              paddingRight: "12px",
              paddingTop: "8px",
              paddingBottom: "8px",
              borderRadius: "8px",
              border: "1px solid var(--border)",
              background: "var(--surface)",
              color: "var(--fg)",
              fontSize: "0.875rem",
              outline: "none",
            }}
          />
        </div>

        {/* Type filters */}
        <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
          {(["all", "quiz", "mock-test"] as TypeFilter[]).map((t) => {
            const label = t === "all" ? "All" : t === "quiz" ? "Quiz" : "Mock Test";
            const active = typeFilter === t;
            return (
              <button
                key={t}
                onClick={() => onTypeChange(t)}
                className={pillClass(active)}
                style={
                  active
                    ? { background: "var(--brand)", color: "#fff" }
                    : { background: "var(--bg-subtle)", color: "var(--fg-muted)" }
                }
              >
                {label}
              </button>
            );
          })}
        </div>

        {/* Topic filters */}
        <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
          {TOPICS.map((topic) => {
            const value = topic === "All" ? "all" : topic;
            const active = topicFilter === value;
            return (
              <button
                key={topic}
                onClick={() => onTopicChange(value)}
                className={pillClass(active)}
                style={
                  active
                    ? { background: "var(--brand)", color: "#fff" }
                    : { background: "var(--bg-subtle)", color: "var(--fg-muted)" }
                }
              >
                {topic}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
