import InteractiveCard from "@/features/discovery/components/InteractiveCard"
import type { Path } from "@/features/learning/types/learning.types"
import { ArrowRight, BookOpen, Compass, Sparkles } from "lucide-react"
import { Link } from "react-router-dom"

interface ExploreDiscoveryProps {
  paths: Path[]
}

export default function ExploreDiscovery({ paths }: ExploreDiscoveryProps) {
  if (paths.length === 0) {
    return (
      <InteractiveCard className="discovery-empty-surface">
        <span className="discovery-empty-surface__icon">
          <Compass size={23} />
        </span>
        <div>
          <strong>See what SkillProof has to offer</strong>
          <p>Browse published learning paths and follow whatever catches your attention.</p>
        </div>
        <Link to="/learning-paths" className="discovery-inline-action">
          Browse learning paths <ArrowRight size={16} />
        </Link>
      </InteractiveCard>
    )
  }

  return (
    <div className="explore-rail">
      {paths.slice(0, 3).map((path, index) => (
        <InteractiveCard key={path.id} className="explore-card">
          <Link to={`/learning-paths/${path.id}`} className="explore-card__link">
            <div className="explore-card__top">
              <span className="explore-card__icon">
                {index === 0 ? <Sparkles size={20} /> : <BookOpen size={20} />}
              </span>
              <ArrowRight className="explore-card__arrow" size={17} />
            </div>
            <strong>{path.title}</strong>
            <p>{path.summary || "Explore this learning path on SkillProof."}</p>
            <span className="explore-card__meta">Explore path</span>
          </Link>
        </InteractiveCard>
      ))}
    </div>
  )
}
