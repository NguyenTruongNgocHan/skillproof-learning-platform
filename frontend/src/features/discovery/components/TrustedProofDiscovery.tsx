import { ArrowRight, BadgeCheck, ShieldCheck, Sparkles } from "lucide-react"
import { Link } from "react-router-dom"
import InteractiveCard from "./InteractiveCard"

export default function TrustedProofDiscovery() {
  return (
    <InteractiveCard className="proof-showcase">
      <div className="proof-showcase__credential" aria-hidden="true">
        <span className="proof-showcase__seal"><BadgeCheck size={26} /></span>
        <span className="proof-showcase__kicker">SKILLPROOF</span>
        <strong>Verified achievement</strong>
        <span>Learning → Assessment → Proof</span>
        <Sparkles className="proof-showcase__spark" size={18} />
      </div>

      <div className="proof-showcase__copy">
        <span className="proof-showcase__eyebrow"><ShieldCheck size={15} /> TRUSTED PROOF</span>
        <h3>Learning is only half the story.</h3>
        <p>See the completion and assessment requirements behind achievements before working toward them.</p>
        <Link to="/app/eligibility">Explore eligibility <ArrowRight size={16} /></Link>
      </div>
    </InteractiveCard>
  )
}
