import { useEffect, useState } from "react"
import { BookOpen, CheckCircle2, Settings2, Sparkles, UserRound } from "lucide-react"
import { Link } from "react-router-dom"

import AppShell from "@/components/layout/AppShell"
import LearnerShell from "@/components/layout/LearnerShell"
import Button from "@/components/ui/Button"
import Input from "@/components/ui/Input"
import { useAuth } from "@/features/auth/hooks/useAuth"
import MediaPanel from "@/features/media/MediaPanel"
import { profileApi, type Profile } from "@/features/profile/profileApi"
import LearningPreferencesPanel from "@/features/profile/LearningPreferencesPanel"

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")

  useEffect(() => { profileApi.me().then(setProfile).catch((cause) => setError(cause instanceof Error ? cause.message : "We couldn't load your profile.")) }, [])

  async function save(event: React.FormEvent) {
    event.preventDefault()
    if (!profile) return
    try { setProfile(await profileApi.update(profile)); setMessage("Your profile has been updated."); setError("") }
    catch (cause) { setError(cause instanceof Error ? cause.message : "We couldn't save your changes.") }
  }

  const content = (
    <main className="learner-page learner-page--profile">
      {!profile ? <div className={`learner-state${error ? " learner-state--error" : ""}`} role={error ? "alert" : "status"}>{error || "Loading your profile…"}</div> : (
        <>
          <header className="profile-hub__hero">
            <div className="profile-hub__avatar"><MediaPanel preferredId={profile.avatarUrl?.startsWith("media:") ? profile.avatarUrl.substring(6) : undefined} scope="AVATAR" writable onUploaded={(item) => setProfile((previous) => previous ? { ...previous, avatarUrl: `media:${item.id}` } : previous)} /></div>
            <div><span>Your SkillProof</span><h1>{profile.displayName}</h1><p>{profile.headline || "Shape how you show up, what SkillProof knows about you, and the learning that belongs to you."}</p></div>
          </header>

          {user?.role === "LEARNER" ? <nav className="profile-hub__nav" aria-label="Profile sections"><a href="#about"><UserRound size={16} /> About you</a><Link to="/app/learning"><BookOpen size={16} /> My learning</Link><Link to="/app/eligibility"><CheckCircle2 size={16} /> Achievements</Link><a href="#preferences"><Sparkles size={16} /> Preferences</a><a href="#account"><Settings2 size={16} /> Account</a></nav> : null}

          <form onSubmit={save} className="profile-hub__form" id="about">
            <section className="profile-hub__section"><div className="profile-hub__section-copy"><h2>About you</h2><p>This is the identity people see across SkillProof.</p></div><div className="profile-hub__fields"><Input label="Email" value={profile.email} disabled /><Input label="Display name" value={profile.displayName} onChange={(event) => setProfile({ ...profile, displayName: event.target.value })} required /><Input label="Headline" value={profile.headline ?? ""} onChange={(event) => setProfile({ ...profile, headline: event.target.value })} /><label className="profile-field">Bio<textarea value={profile.bio ?? ""} onChange={(event) => setProfile({ ...profile, bio: event.target.value })} placeholder="A little about what you are learning or working toward…" /></label></div></section>
            <section className="profile-hub__section" id="account"><div className="profile-hub__section-copy"><h2>Language & time</h2><p>Used to make dates and language feel natural to you.</p></div><div className="profile-hub__fields profile-hub__fields--two"><Input label="Locale" value={profile.locale} onChange={(event) => setProfile({ ...profile, locale: event.target.value })} required /><Input label="Timezone" value={profile.timezone} onChange={(event) => setProfile({ ...profile, timezone: event.target.value })} required /></div></section>
            {user?.role === "LEARNER" ? <section className="profile-hub__section" id="preferences"><div className="profile-hub__section-copy"><h2>Learning preferences</h2><p>Your goals and interests belong to your personal space. You stay in control of them.</p></div><LearningPreferencesPanel /></section> : null}
            {error ? <p className="learner-inline-error" role="alert">{error}</p> : null}{message ? <p className="profile-hub__success"><CheckCircle2 size={16} /> {message}</p> : null}<div className="profile-hub__save"><Button type="submit" size="lg">Save changes</Button></div>
          </form>
        </>
      )}
    </main>
  )

  return user?.role === "LEARNER" ? <LearnerShell>{content}</LearnerShell> : <AppShell>{content}</AppShell>
}
