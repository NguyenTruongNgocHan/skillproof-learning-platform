import { Search } from "lucide-react";
import EmptyState from "@/components/feedback/EmptyState";
import CommunityContentCard from "@/features/community/components/CommunityContentCard";
import type { CommunityContentItem } from "@/mocks/community/communityContent";

interface CommunityGridProps {
  items: CommunityContentItem[];
  isAuthenticated: boolean;
  onProtectedAction: (item: CommunityContentItem) => void;
}

export default function CommunityGrid({ items, isAuthenticated, onProtectedAction }: CommunityGridProps) {
  if (items.length === 0) {
    return (
      <EmptyState
        icon={Search}
        title="No content found"
        description="Try adjusting your filters."
      />
    );
  }

  return (
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
        gap: "16px",
      }}
    >
      {items.map((item) => (
        <CommunityContentCard
          key={item.id}
          item={item}
          isAuthenticated={isAuthenticated}
          onProtectedAction={onProtectedAction}
        />
      ))}
    </div>
  );
}
