import { MessageCircleQuestion, Users } from "lucide-react"
import { useState } from "react"

import AuthGateModal from "@/features/auth/components/AuthGateModal"
import { useAuth } from "@/features/auth/hooks/useAuth"
import CommunityFilters from "@/features/community/components/CommunityFilters"
import CommunityGrid from "@/features/community/components/CommunityGrid"
import {
  COMMUNITY_CONTENT,
  type CommunityContentItem,
} from "@/features/community/data/communityContent"
import LearnerSurface from "@/shared/components/layout/LearnerSurface"
import { useToast } from "@/shared/ui/Toast"

type TypeFilter = "all" | "quiz" | "mock-test"

export default function CommunityPage() {
  const { isAuthenticated } = useAuth()
  const { toast } = useToast()
  const [search, setSearch] = useState("")
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("all")
  const [topicFilter, setTopicFilter] = useState("all")
  const [authGateOpen, setAuthGateOpen] = useState(false)

  const filtered = COMMUNITY_CONTENT.filter((item) => {
    if (typeFilter === "quiz" && item.type !== "Quiz") return false
    if (typeFilter === "mock-test" && item.type !== "Mock Test") return false
    if (topicFilter !== "all" && item.topic !== topicFilter) return false
    return !search || item.title.toLowerCase().includes(search.toLowerCase())
  })

  function handleStartPractice(_item: CommunityContentItem) {
    if (!isAuthenticated) setAuthGateOpen(true)
    else
      toast("info", "Community practice is still a preview. Live attempts are not connected yet.")
  }

  return (
    <LearnerSurface>
      <main className="learner-page learner-page--community">
        <header className="learner-page__hero community-hero">
          <div>
            <span className="learner-page__intro">
              <Users size={15} /> Learn with others
            </span>
            <h1>
              Practice beyond your
              <br />
              own learning path.
            </h1>
            <p>
              Discover learner-created quizzes and mock tests without confusing community practice
              with official certification evidence.
            </p>
          </div>
          <div className="community-hero__note">
            <MessageCircleQuestion size={21} />
            <span>
              <strong>Community supports practice.</strong>
              <small>It does not create official completion or certificate eligibility.</small>
            </span>
          </div>
        </header>

        <section className="learner-content-section community-browser">
          <div className="learner-content-section__heading">
            <div>
              <h2>Explore community practice</h2>
              <p>Use topic and format filters to find something useful to try.</p>
            </div>
            <span>
              {filtered.length} preview{filtered.length === 1 ? "" : "s"}
            </span>
          </div>
          <CommunityFilters
            search={search}
            typeFilter={typeFilter}
            topicFilter={topicFilter}
            onSearchChange={setSearch}
            onTypeChange={(value) => setTypeFilter(value as TypeFilter)}
            onTopicChange={setTopicFilter}
          />
          <CommunityGrid
            items={filtered}
            isAuthenticated={isAuthenticated}
            onProtectedAction={handleStartPractice}
          />
        </section>
      </main>
      <AuthGateModal open={authGateOpen} onClose={() => setAuthGateOpen(false)} />
    </LearnerSurface>
  )
}
