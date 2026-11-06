import { useEffect, useState } from "react"
import { adminOrganizationApi, type ReviewItem } from "../api/adminOrganizationApi"
const criteria = [
  {
    key: "legal_identity",
    label: "Legal identity",
    hint: "Registration details match the submitted organization",
  },
  {
    key: "representative",
    label: "Representative authorization",
    hint: "The contact has authority to represent this organization",
  },
  {
    key: "expiry",
    label: "Document validity",
    hint: "Documents are current, legible and authentic",
  },
  {
    key: "company_images",
    label: "Company images",
    hint: "At least three distinct, relevant company images",
  },
  {
    key: "evidence_decisions",
    label: "Evidence decisions",
    hint: "Required evidence was inspected and reviewed individually",
  },
  {
    key: "final_consistency",
    label: "Application consistency",
    hint: "Contact information matches the submission snapshot",
  },
]
export default function ReviewChecklist({
  type,
  id,
  onComplete,
  onFeedback,
}: {
  type: string
  id: string
  onComplete?: (done: boolean) => void
  onFeedback?: (feedback: string) => void
}) {
  const [items, setItems] = useState<ReviewItem[]>([])
  const [busy, setBusy] = useState(false)
  const [dirty, setDirty] = useState(false)
  const [error, setError] = useState("")
  useEffect(() => {
    let active = true
    setItems([])
    setDirty(false)
    adminOrganizationApi
      .reviewChecklist(id)
      .then((r) => {
        if (active)
          setItems(
            criteria.map(
              (c) =>
                r.items.find((i) => i.key === c.key) ?? { key: c.key, checked: false, note: "" },
            ),
          )
      })
      .catch((e) => {
        if (active) setError(e instanceof Error ? e.message : "Could not load review")
      })
    return () => {
      active = false
    }
  }, [id, type])
  const complete = items.length === criteria.length && items.every((i) => i.checked)
  const feedback = criteria
    .flatMap((c) => {
      const i = items.find((v) => v.key === c.key)
      return i?.note.trim() ? [`${c.label}: ${i.note.trim()}`] : []
    })
    .join("\n")
  useEffect(() => onComplete?.(complete && !dirty), [complete, dirty, onComplete])
  useEffect(() => onFeedback?.(feedback), [feedback, onFeedback])
  async function save() {
    setBusy(true)
    setError("")
    try {
      const r = await adminOrganizationApi.saveReviewChecklist(id, items)
      setItems(
        criteria.map(
          (c) => r.items.find((i) => i.key === c.key) ?? { key: c.key, checked: false, note: "" },
        ),
      )
      setDirty(false)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Save failed")
    } finally {
      setBusy(false)
    }
  }
  return (
    <section>
      <div className="adm-panel-head">
        <h3>Review criteria</h3>
        <span className="adm-pill">
          {items.filter((i) => i.checked).length}/{criteria.length} verified
        </span>
      </div>
      {error && (
        <p role="alert" className="adm-error">
          {error}
        </p>
      )}
      {!items.length && !error && <p className="adm-muted">Loading saved review…</p>}
      {criteria.map((c) => {
        const i = items.find((v) => v.key === c.key)
        return (
          <div className="adm-review-item" key={c.key}>
            <label>
              <input
                type="checkbox"
                checked={i?.checked ?? false}
                disabled={!i || busy}
                onChange={(e) => {
                  setItems((prev) =>
                    prev.map((x) => (x.key === c.key ? { ...x, checked: e.target.checked } : x)),
                  )
                  setDirty(true)
                }}
              />
              {c.label}
            </label>
            <p className="adm-muted">{c.hint}</p>
            <textarea
              aria-label={`Comment: ${c.label}`}
              placeholder="Comment for applicant or review notes…"
              value={i?.note ?? ""}
              disabled={!i || busy}
              maxLength={500}
              onChange={(e) => {
                setItems((prev) =>
                  prev.map((x) => (x.key === c.key ? { ...x, note: e.target.value } : x)),
                )
                setDirty(true)
              }}
            />
          </div>
        )
      })}
      <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
        <button
          className="adm-btn adm-btn-primary"
          disabled={busy || !dirty || !items.length}
          onClick={() => void save()}
        >
          {busy ? "Saving…" : "Save review progress"}
        </button>
        <span className="adm-muted">
          {dirty
            ? "Unsaved changes"
            : complete
              ? "All criteria verified and saved"
              : "You can return to this review later"}
        </span>
      </div>
    </section>
  )
}
