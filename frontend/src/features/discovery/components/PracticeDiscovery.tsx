import { ArrowRight, BrainCircuit, Clock3, Target } from "lucide-react"
import { Link } from "react-router-dom"
import InteractiveCard from "./InteractiveCard"

export default function PracticeDiscovery() {
  return (
    <InteractiveCard className="practice-showcase">
      <div className="practice-showcase__visual"><BrainCircuit size={30} /></div>
      <div className="practice-showcase__copy">
        <span className="practice-showcase__label">QUICK CHALLENGE</span>
        <h3>Ready to put your knowledge to work?</h3>
        <p>Practice with available quizzes and assessments, then use the result to decide what to strengthen next.</p>
        <div className="practice-showcase__meta">
          <span><Target size={15} /> Skill-focused</span>
          <span><Clock3 size={15} /> Short sessions</span>
        </div>
      </div>
      <Link to="/practice" className="practice-showcase__action">Explore practice <ArrowRight size={16} /></Link>
    </InteractiveCard>
  )
}
