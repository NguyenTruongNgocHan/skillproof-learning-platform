import { ArrowRight, BookOpen } from "lucide-react"
import { useEffect, useState } from "react"
import { Link, useNavigate } from "react-router-dom"

import { useAuth } from "@/features/auth/hooks/useAuth"
import { learnerDiscoveryApi } from "@/features/discovery/api/learnerDiscoveryApi"
import DiscoveryHero from "@/features/discovery/components/DiscoveryHero"
import DiscoverySection from "@/features/discovery/components/DiscoverySection"
import ExploreDiscovery from "@/features/discovery/components/ExploreDiscovery"
import GoalDiscoveryCard from "@/features/discovery/components/GoalDiscoveryCard"
import InteractiveCard from "@/features/discovery/components/InteractiveCard"
import PracticeDiscovery from "@/features/discovery/components/PracticeDiscovery"
import SkillExplorer from "@/features/discovery/components/SkillExplorer"
import SkillProofGuide from "@/features/discovery/components/SkillProofGuide"
import TrustedProofDiscovery from "@/features/discovery/components/TrustedProofDiscovery"
import type { LearnerDiscoveryProfile } from "@/features/discovery/types/learnerDiscovery.types"
import { learningApi } from "@/features/learning/api/learningApi"
import type { Enrollment, Path } from "@/features/learning/types/learning.types"
import LearnerShell from "@/shared/components/layout/LearnerShell"

export default function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [profile, setProfile] = useState<LearnerDiscoveryProfile | null>(null)
  const [enrollments, setEnrollments] = useState<Enrollment[]>([])
  const [paths, setPaths] = useState<Path[]>([])
  const [guideOpen, setGuideOpen] = useState(false)
  const [guideGoal, setGuideGoal] = useState<string | undefined>()

  useEffect(() => {
    let active = true

    async function load() {
      const [profileResult, enrollmentResult, pathResult] = await Promise.allSettled([
        learnerDiscoveryApi.get(),
        learningApi.mine(),
        learningApi.discover(),
      ])
      if (!active) return
      if (profileResult.status === "fulfilled") setProfile(profileResult.value)
      if (enrollmentResult.status === "fulfilled") setEnrollments(enrollmentResult.value)
      if (pathResult.status === "fulfilled") setPaths(pathResult.value)
    }

    void load()
    return () => {
      active = false
    }
  }, [])

  const firstName = user?.fullName?.trim().split(/\s+/).at(-1) ?? "there"
  const activeEnrollments = enrollments.filter((item) => item.status === "ACTIVE").slice(0, 3)

  function openGuide(goal?: string) {
    setGuideGoal(goal)
    setGuideOpen(true)
  }

  return (
    <LearnerShell>
      <div className="discovery-home">
        <DiscoveryHero
          firstName={firstName}
          onExplore={() => navigate("/learning-paths")}
          onGuideOpen={() => openGuide()}
        />

        <div className="discovery-home__content">
          {activeEnrollments.length > 0 && (
            <DiscoverySection
              title="Pick up where you left off."
              actionLabel="My learning"
              actionTo="/app/learning"
            >
              <div className="discovery-card-grid">
                {activeEnrollments.map((enrollment) => (
                  <InteractiveCard key={enrollment.id} className="discovery-content-card-wrap">
                    <Link to={`/app/learning/${enrollment.id}`} className="discovery-content-card">
                      <span className="discovery-content-card__icon">
                        <BookOpen size={21} />
                      </span>
                      <div>
                        <strong>{enrollment.title}</strong>
                        <p>{enrollment.summary || "Continue your learning journey."}</p>
                      </div>
                      <ArrowRight size={17} />
                    </Link>
                  </InteractiveCard>
                ))}
              </div>
            </DiscoverySection>
          )}

          <DiscoverySection
            title="Something worth learning today."
            description="No plan required. Start anywhere and follow what catches your attention."
            actionLabel="Explore more"
            actionTo="/learning-paths"
          >
            <ExploreDiscovery paths={paths} />
          </DiscoverySection>

          <DiscoverySection
            title="What are you curious about?"
            description={
              profile?.interests.length
                ? "Keep exploring areas you've shown interest in, or branch out into something new."
                : "Open a category to peek inside before deciding where to go."
            }
          >
            {profile?.interests.length ? (
              <div className="discovery-interest-row">
                {profile.interests.slice(0, 6).map((interest) => (
                  <button
                    key={interest.label}
                    type="button"
                    onClick={() =>
                      navigate(`/learning-paths?search=${encodeURIComponent(interest.label)}`)
                    }
                  >
                    {interest.label}
                  </button>
                ))}
              </div>
            ) : (
              <SkillExplorer
                onSelect={(category) =>
                  navigate(`/learning-paths?search=${encodeURIComponent(category)}`)
                }
                onMore={() => navigate("/learning-paths")}
              />
            )}
          </DiscoverySection>

          <DiscoverySection
            title="Start with where you want to go."
            description="Tell SkillProof what you want to achieve. Start with the outcome; you do not need to know the exact path yet."
          >
            <GoalDiscoveryCard onStart={openGuide} />
          </DiscoverySection>

          <DiscoverySection
            title="Turn learning into practice."
            description="Challenge what you know and discover what to strengthen next."
            actionLabel="Explore practice"
            actionTo="/practice"
          >
            <PracticeDiscovery />
          </DiscoverySection>

          <DiscoverySection
            title="Make progress mean something."
            description="Understand the requirements behind achievements and work toward evidence you can share."
            actionLabel="View eligibility"
            actionTo="/app/eligibility"
          >
            <TrustedProofDiscovery />
          </DiscoverySection>
        </div>

        <SkillProofGuide open={guideOpen} onOpenChange={setGuideOpen} initialGoal={guideGoal} />
      </div>
    </LearnerShell>
  )
}
