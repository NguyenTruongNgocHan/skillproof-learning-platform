import { useState } from "react"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import {
  eligibilityApi,
  type CompletionEvidence,
  type EligibilityResult,
} from "@/features/eligibility/eligibilityApi"
import { useAuth } from "@/features/auth/hooks/useAuth"

interface EvaluationView {
  completion?: CompletionEvidence
  eligibility?: EligibilityResult
}

export default function EligibilityPage() {
  const { user } = useAuth()
  const [enrollmentId, setEnrollmentId] = useState("")
  const [programId, setProgramId] = useState("")
  const [result, setResult] = useState<EvaluationView | null>(null)
  const [error, setError] = useState("")

  async function evaluate() {
    try {
      const completion = await eligibilityApi.evaluateCompletion(enrollmentId)
      const eligibility = programId
        ? await eligibilityApi.evaluateEligibility(programId, enrollmentId)
        : undefined
      setResult({ completion, eligibility })
      setError("")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Evaluation failed")
    }
  }

  async function readLatest() {
    if (!user || !programId) return
    try {
      const eligibility = await eligibilityApi.getLatestEligibility(programId, user.id)
      setResult({ eligibility })
      setError("")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Eligibility not found")
    }
  }

  return (
    <AppShell>
      <div className="v7-wrap">
        <header className="v7-hero">
          <span className="org-eyebrow">COMPLETION & ELIGIBILITY</span>
          <h1>Version-bound completion evidence</h1>
          <p>
            Completion is evaluated from learning progress and official assessment
            results. Battle rating does not contribute to eligibility.
          </p>
        </header>

        <section className="v7-card">
          <label>
            Enrollment ID
            <input
              value={enrollmentId}
              onChange={event => setEnrollmentId(event.target.value)}
            />
          </label>
          <label>
            Certification Program ID
            <input
              value={programId}
              onChange={event => setProgramId(event.target.value)}
            />
          </label>
          <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
            <Button onClick={() => void evaluate()} disabled={!enrollmentId}>
              Evaluate
            </Button>
            <Button
              variant="outline"
              onClick={() => void readLatest()}
              disabled={!programId}
            >
              Read latest eligibility
            </Button>
          </div>
          {error && <div className="org-error">{error}</div>}
          {result && <pre>{JSON.stringify(result, null, 2)}</pre>}
        </section>
      </div>
    </AppShell>
  )
}
