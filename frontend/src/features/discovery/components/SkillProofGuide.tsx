import { useSessionOnce } from "@/features/discovery/hooks/useDiscoveryMotion"
import mascot from "@/imports/logo_dark.png"
import { ArrowUp, LockKeyhole, Sparkles, X } from "lucide-react"
import { useEffect, useState } from "react"

interface SkillProofGuideProps {
  open?: boolean
  onOpenChange?: (open: boolean) => void
  initialGoal?: string
}

export default function SkillProofGuide({
  open: controlledOpen,
  onOpenChange,
  initialGoal,
}: SkillProofGuideProps) {
  const [internalOpen, setInternalOpen] = useState(false)
  const [hintVisible, setHintVisible] = useState(false)
  const { seen, markSeen } = useSessionOnce("guide-hint")
  const open = controlledOpen ?? internalOpen

  function setOpen(value: boolean) {
    onOpenChange?.(value)
    if (!onOpenChange) setInternalOpen(value)
    if (value) {
      setHintVisible(false)
      markSeen()
    }
  }

  useEffect(() => {
    if (seen || open) return
    const timer = window.setTimeout(() => setHintVisible(true), 4200)
    const hide = window.setTimeout(() => {
      setHintVisible(false)
      markSeen()
    }, 8500)
    return () => {
      window.clearTimeout(timer)
      window.clearTimeout(hide)
    }
  }, [seen, open, markSeen])

  return (
    <div className="skillproof-guide">
      {open && (
        <aside className="skillproof-guide__panel" aria-label="SkillProof Guide">
          <header className="skillproof-guide__header">
            <div className="skillproof-guide__identity">
              <div className="skillproof-guide__avatar">
                <img src={mascot} alt="" />
              </div>
              <div>
                <strong>SkillProof Guide</strong>
                <span>Your learning companion</span>
              </div>
            </div>
            <button
              type="button"
              className="skillproof-guide__close"
              onClick={() => setOpen(false)}
              aria-label="Close SkillProof Guide"
            >
              <X size={18} />
            </button>
          </header>

          <div className="skillproof-guide__body">
            <div className="skillproof-guide__welcome">
              <img src={mascot} alt="" />
              <div>
                <strong>
                  {initialGoal ? "Let's work from your goal." : "What are you curious about today?"}
                </strong>
                <p>
                  {initialGoal
                    ? `You said: “${initialGoal}”`
                    : "Tell me naturally, or keep exploring on your own."}
                </p>
              </div>
            </div>

            <div className="skillproof-guide__suggestions">
              <button type="button">
                <Sparkles size={15} /> Find something for me
              </button>
              <button type="button">Help me reach a goal</button>
              <button type="button">Surprise me</button>
            </div>

            <div className="skillproof-guide__composer">
              <input
                type="text"
                placeholder="Conversational guidance will connect here."
                disabled
              />
              <button type="button" aria-label="Send" disabled>
                <ArrowUp size={17} />
              </button>
            </div>

            <div className="skillproof-guide__privacy">
              <LockKeyhole size={14} />
              <span>You decide what becomes part of your learning profile.</span>
            </div>
          </div>
        </aside>
      )}

      {hintVisible && !open && (
        <div className="skillproof-guide__hint">Have something specific in mind?</div>
      )}

      <button
        type="button"
        className="skillproof-guide__trigger"
        onClick={() => setOpen(!open)}
        aria-expanded={open}
      >
        <span className="skillproof-guide__trigger-mascot">
          <img src={mascot} alt="" />
        </span>
        <span>Ask SkillProof</span>
      </button>
    </div>
  )
}
