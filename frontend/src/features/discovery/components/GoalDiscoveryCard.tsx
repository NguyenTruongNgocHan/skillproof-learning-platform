import { useEffect, useState } from "react"
import { ArrowRight, Sparkles } from "lucide-react"

const prompts = [
  "I want to learn Japanese...",
  "I want to become a backend developer...",
  "I want to improve my communication...",
]

const examples = ["Speak Japanese", "Become a backend developer", "Improve communication"]

interface GoalDiscoveryCardProps {
  onStart: (goal?: string) => void
}

export default function GoalDiscoveryCard({ onStart }: GoalDiscoveryCardProps) {
  const [goal, setGoal] = useState("")
  const [promptIndex, setPromptIndex] = useState(0)

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return
    const timer = window.setInterval(() => setPromptIndex((value) => (value + 1) % prompts.length), 4800)
    return () => window.clearInterval(timer)
  }, [])

  function submit() {
    onStart(goal.trim() || undefined)
  }

  return (
    <div className="goal-prompt">
      <div className="goal-prompt__mark"><Sparkles size={22} /></div>
      <div className="goal-prompt__content">
        <h3>Where do you want to go next?</h3>
        <p>You do not need to know the exact course yet. Start with the outcome you want.</p>

        <div className="goal-prompt__input-wrap">
          <input
            value={goal}
            onChange={(event) => setGoal(event.target.value)}
            onKeyDown={(event) => event.key === "Enter" && submit()}
            placeholder={prompts[promptIndex]}
            aria-label="Learning goal"
          />
          <button type="button" onClick={submit} aria-label="Tell SkillProof about this goal"><ArrowRight size={18} /></button>
        </div>

        <div className="goal-prompt__examples">
          <span>Try</span>
          {examples.map((example) => (
            <button key={example} type="button" onClick={() => setGoal(example)}>{example}</button>
          ))}
        </div>
      </div>
    </div>
  )
}
