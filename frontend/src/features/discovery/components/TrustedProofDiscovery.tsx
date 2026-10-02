import { ArrowRight, BadgeCheck, Fingerprint, ShieldCheck, Sparkles } from "lucide-react"
import { Link } from "react-router-dom"
import InteractiveCard from "./InteractiveCard"

export default function TrustedProofDiscovery() {
  return (
    <InteractiveCard className="proof-experience">
      <div className="proof-experience__copy">
        <span className="proof-experience__eyebrow"><ShieldCheck size={15} /> TRUSTED PROOF</span>
        <h3>Learning is only half the story.</h3>
        <p>Understand the completion and assessment requirements behind achievements before working toward evidence you can share.</p>
        <Link to="/app/eligibility">Explore eligibility <ArrowRight size={16} /></Link>
      </div>
      <div className="proof-experience__stage" aria-hidden="true">
        <div className="proof-experience__credential">
          <div className="proof-experience__credential-top"><span className="proof-experience__seal"><BadgeCheck size={24} /></span><Fingerprint size={24} /></div>
          <span className="proof-experience__kicker">SKILLPROOF · VERIFIED</span>
          <strong>Verified achievement</strong>
          <span className="proof-experience__flow">Learning <i /> Assessment <i /> Proof</span>
          <div className="proof-experience__credential-foot"><span>Evidence-backed</span><Sparkles size={17} /></div>
        </div>
      </div>
    </InteractiveCard>
  )
}
