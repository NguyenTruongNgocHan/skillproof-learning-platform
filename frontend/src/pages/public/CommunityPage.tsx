import { useState } from "react";
import { useToast } from "@/components/ui/Toast";
import { useAuth } from "@/features/auth/hooks/useAuth";
import AuthGateModal from "@/components/auth/AuthGateModal";
import PublicHeader from "@/components/layout/PublicHeader";
import PublicFooter from "@/components/layout/PublicFooter";
import CommunityFilters from "@/features/community/components/CommunityFilters";
import CommunityGrid from "@/features/community/components/CommunityGrid";
import { COMMUNITY_CONTENT } from "@/mocks/community/communityContent";
import type { CommunityContentItem } from "@/mocks/community/communityContent";

type TypeFilter = "all" | "quiz" | "mock-test";

export default function CommunityPage() {
  const { isAuthenticated } = useAuth();
  const { toast } = useToast();

  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("all");
  const [topicFilter, setTopicFilter] = useState("all");
  const [authGateOpen, setAuthGateOpen] = useState(false);
  const [, setSelectedItem] = useState<CommunityContentItem | null>(null);

  const filtered = COMMUNITY_CONTENT.filter((item) => {
    if (typeFilter === "quiz" && item.type !== "Quiz") return false;
    if (typeFilter === "mock-test" && item.type !== "Mock Test") return false;
    if (topicFilter !== "all" && item.topic !== topicFilter) return false;
    if (search && !item.title.toLowerCase().includes(search.toLowerCase()))
      return false;
    return true;
  });

  function handleStartPractice(item: CommunityContentItem) {
    if (!isAuthenticated) {
      setSelectedItem(item);
      setAuthGateOpen(true);
    } else {
      toast(
        "info",
        "Community practice is in preview. Attempts are not available yet.",
      );
    }
  }

  return (
    <div
      style={{
        background: "var(--bg)",
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <div style={{ position: "sticky", top: 0, zIndex: 10 }}>
        <PublicHeader />
      </div>

      {/* Hero */}
      <section style={{ textAlign: "center", padding: "48px 16px 40px" }}>
        <h1 className="text-3xl font-bold mb-3" style={{ color: "var(--fg)" }}>
          Community Practice Content
        </h1>
        <p style={{ color: "var(--fg-muted)", fontSize: "1rem" }}>
          Sample community cards show how discovery will work. Practice and
          ratings are not live yet.
        </p>
      </section>

      <CommunityFilters
        search={search}
        typeFilter={typeFilter}
        topicFilter={topicFilter}
        onSearchChange={setSearch}
        onTypeChange={(v) => setTypeFilter(v as TypeFilter)}
        onTopicChange={setTopicFilter}
      />

      <main
        style={{
          maxWidth: "72rem",
          margin: "0 auto",
          padding: "24px 16px",
          flex: 1,
          width: "100%",
        }}
      >
        <p
          style={{
            color: "var(--fg-muted)",
            fontSize: "0.875rem",
            marginBottom: "16px",
          }}
        >
          Previewing {filtered.length} result{filtered.length !== 1 ? "s" : ""}
        </p>
        <CommunityGrid
          items={filtered}
          isAuthenticated={isAuthenticated}
          onProtectedAction={handleStartPractice}
        />
      </main>

      <PublicFooter />

      <AuthGateModal
        open={authGateOpen}
        onClose={() => {
          setAuthGateOpen(false);
          setSelectedItem(null);
        }}
      />
    </div>
  );
}
