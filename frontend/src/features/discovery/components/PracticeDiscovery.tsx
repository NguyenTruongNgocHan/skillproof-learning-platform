import InteractiveCard from "@/features/discovery/components/InteractiveCard"
import { ArrowRight, BrainCircuit, Check, Clock3, Target } from "lucide-react"
import { Link } from "react-router-dom"

export default function PracticeDiscovery() {
  return (
    <InteractiveCard className="practice-experience">
      <div className="practice-experience__copy">
        <span className="practice-experience__label">
          <BrainCircuit size={15} /> QUICK CHALLENGE
        </span>
        <h3>Ready to put your knowledge to work?</h3>
        <p>
          Practice with available quizzes and assessments, then use the result to decide what to
          strengthen next.
        </p>
        <div className="practice-experience__meta">
          <span>
            <Target size={15} /> Skill-focused
          </span>
          <span>
            <Clock3 size={15} /> Short sessions
          </span>
        </div>
        <Link to="/practice">
          Explore practice <ArrowRight size={16} />
        </Link>
      </div>
      <div className="practice-experience__visual" aria-hidden="true">
        <span className="practice-experience__visual-kicker">KNOWLEDGE CHECK</span>
        <div className="practice-experience__rings">
          <span />
          <span />
          <span />
          <span />
          <span />
        </div>
        <strong>
          Small challenge.
          <br />
          Useful signal.
        </strong>
        <div className="practice-experience__checks">
          <span>
            <Check size={13} /> Focused
          </span>
          <span>
            <Check size={13} /> Actionable
          </span>
        </div>
      </div>
    </InteractiveCard>
  )
}
