import { ArrowRight, Sparkles } from "lucide-react"
import { useEffect, useState } from "react"

import mascot from "@/imports/logo_dark.png"

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
    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches

    if (prefersReducedMotion) return

    const timer = window.setInterval(() => {
      setPromptIndex((current) => (current + 1) % prompts.length)
    }, 4800)

    return () => window.clearInterval(timer)
  }, [])

  const submit = () => {
    onStart(goal.trim() || undefined)
  }

  const openGuide = () => {
    onStart(goal.trim() || undefined)
  }

  return (
    <div className="goal-experience">
      {/* Goal input */}
      <div className="goal-experience__conversation">
        <h3>Tell us where you want to go.</h3>

        <p>
          You do not need to know the course, path, or exact skill yet. A goal is enough to start
          exploring.
        </p>

        <div className="goal-experience__input-wrap">
          <Sparkles size={17} aria-hidden="true" />

          <input
            value={goal}
            onChange={(event) => setGoal(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === "Enter") {
                submit()
              }
            }}
            placeholder={prompts[promptIndex]}
            aria-label="Learning goal"
          />

          <button type="button" onClick={submit} aria-label="Explore this learning goal">
            <ArrowRight size={18} />
          </button>
        </div>

        <div className="goal-experience__examples">
          <span>Try</span>

          {examples.map((example) => (
            <button key={example} type="button" onClick={() => setGoal(example)}>
              {example}
            </button>
          ))}
        </div>
      </div>

      {/* SkillProof Guide */}
      <button
        type="button"
        className="goal-experience__guide"
        onClick={openGuide}
        aria-label="Ask SkillProof Guide for help with your learning goal"
      >
        <span className="goal-experience__guide-glow" aria-hidden="true" />

        <div
          aria-hidden="true"
          style={{
            position: "relative",
            width: "100%",
            height: "116px",
            flexShrink: 0,
          }}
        >
          <img
            src={mascot}
            alt=""
            style={{
              position: "absolute",
              width: "104px",
              height: "104px",
              objectFit: "contain",

              /*
               * Local positioning for THIS mascot only.
               * It intentionally does not depend on the shared
               * .goal-experience__guide img positioning.
               */
              top: "50%",
              left: "50%",
              right: "auto",
              transform: "translate(-50%, -50%)",

              filter: "drop-shadow(0 12px 22px rgba(30, 20, 35, 0.14))",
            }}
          />
        </div>

        <div
          style={{
            position: "relative",
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-start",
            width: "100%",
          }}
        >
          <strong>Not sure where to start?</strong>

          <span>
            Tell SkillProof what you have in mind. We&apos;ll help you shape it into a learning
            direction.
          </span>

          <em>
            Ask SkillProof
            <ArrowRight size={14} />
          </em>
        </div>
      </button>
    </div>
  )
}
