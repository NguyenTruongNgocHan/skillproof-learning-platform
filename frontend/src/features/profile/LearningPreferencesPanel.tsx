import { useEffect, useState } from "react"
import { Check, Plus, Sparkles, X } from "lucide-react"

import Button from "@/components/ui/Button"
import { learnerDiscoveryApi, type LearnerDiscoveryProfile } from "@/features/discovery/learnerDiscoveryApi"

const SUGGESTIONS = ["Backend", "Cloud", "Japanese", "Communication", "Design", "Business"]

export default function LearningPreferencesPanel() {
  const [profile, setProfile] = useState<LearnerDiscoveryProfile | null>(null)
  const [goal, setGoal] = useState("")
  const [interests, setInterests] = useState<string[]>([])
  const [draft, setDraft] = useState("")
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    learnerDiscoveryApi.get().then((value) => {
      setProfile(value)
      setGoal(value.goalText ?? "")
      setInterests(value.interests.map((item) => item.label))
    }).catch((cause) => setError(cause instanceof Error ? cause.message : "We couldn't load your learning preferences."))
  }, [])

  function addInterest(value: string) {
    const label = value.trim()
    if (!label || interests.some((item) => item.toLowerCase() === label.toLowerCase())) return
    setInterests((current) => [...current, label])
    setDraft("")
  }

  async function save() {
    setBusy(true); setError(""); setMessage("")
    try {
      const next = await learnerDiscoveryApi.update({
        goalText: goal.trim() || null,
        interests,
        personalizationEnabled: profile?.personalizationEnabled ?? true,
        explorationMode: profile?.explorationMode ?? true,
      })
      setProfile(next)
      setMessage("Your discovery preferences are up to date.")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "We couldn't save your learning preferences.")
    } finally { setBusy(false) }
  }

  if (!profile && !error) return <div className="learner-state" role="status">Loading your learning preferences…</div>

  return (
    <div className="preference-editor">
      <div className="preference-editor__intro"><Sparkles size={20} /><div><strong>Shape discovery around what matters to you.</strong><p>Only goals and interests you choose or confirm become part of your profile.</p></div></div>
      <label className="profile-field">What are you working toward?<textarea value={goal} onChange={(event) => setGoal(event.target.value)} placeholder="For example: I want to become a backend developer, or improve Japanese for work." /></label>
      <div className="preference-editor__interests">
        <span>Interests</span>
        <div className="preference-editor__chips">{interests.map((item) => <button type="button" key={item} onClick={() => setInterests((current) => current.filter((value) => value !== item))}>{item}<X size={13} /></button>)}</div>
        <div className="preference-editor__add"><input value={draft} onChange={(event) => setDraft(event.target.value)} onKeyDown={(event) => { if (event.key === "Enter") { event.preventDefault(); addInterest(draft) } }} placeholder="Add an interest" /><button type="button" onClick={() => addInterest(draft)} aria-label="Add interest"><Plus size={16} /></button></div>
        <div className="preference-editor__suggestions">{SUGGESTIONS.filter((item) => !interests.includes(item)).slice(0, 5).map((item) => <button type="button" key={item} onClick={() => addInterest(item)}>+ {item}</button>)}</div>
      </div>
      <label className="preference-editor__toggle"><input type="checkbox" checked={profile?.personalizationEnabled ?? true} onChange={(event) => setProfile((current) => current ? { ...current, personalizationEnabled: event.target.checked } : current)} /><span><strong>Personalized discovery</strong><small>Use your confirmed goals, interests and learning activity to improve suggestions.</small></span></label>
      {error ? <p className="learner-inline-error" role="alert">{error}</p> : null}
      {message ? <p className="profile-hub__success"><Check size={15} /> {message}</p> : null}
      <Button type="button" onClick={() => void save()} disabled={busy}>{busy ? "Saving…" : "Save learning preferences"}</Button>
    </div>
  )
}
