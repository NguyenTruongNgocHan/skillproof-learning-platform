import { ArrowRight, Search, Sparkles } from "lucide-react"
import mascot from "@/imports/logo_dark.png"

interface DiscoveryHeroProps {
  firstName: string
  onExplore: () => void
  onGuideOpen: () => void
}

export default function DiscoveryHero({ firstName, onExplore, onGuideOpen }: DiscoveryHeroProps) {
  return (
    <section className="discovery-hero">
      <div className="discovery-hero__inner">
        <div className="discovery-hero__copy">
          <span className="discovery-hero__welcome">Welcome back, {firstName}</span>
          <h1>
            What would you like<br />to <span>discover?</span>
          </h1>
          <p>
            Learn something new, work toward a goal, or simply follow what catches your interest.
          </p>

          <button type="button" className="discovery-search" onClick={onExplore}>
            <Search size={20} />
            <span>Search skills, topics, and learning paths...</span>
            <strong>Explore <ArrowRight size={16} /></strong>
          </button>
        </div>

        <button type="button" className="discovery-hero__mascot" onClick={onGuideOpen} aria-label="Open SkillProof Guide">
          <span className="discovery-hero__spark discovery-hero__spark--one"><Sparkles size={18} /></span>
          <span className="discovery-hero__spark discovery-hero__spark--two"><Sparkles size={12} /></span>
          <img src={mascot} alt="" />
          <span className="discovery-hero__mascot-note">Need a little direction?</span>
        </button>
      </div>
    </section>
  )
}
