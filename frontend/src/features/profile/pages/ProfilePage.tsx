import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import { BookOpen, CheckCircle2, Settings2, Sparkles, UserRound } from "lucide-react"
import { useEffect, useRef, useState } from "react"
import { Link } from "react-router-dom"

import { useAuth } from "@/features/auth/hooks/useAuth"
import MediaPanel from "@/features/media/components/MediaPanel"
import "../styles/profile-workspace.css"
import { profileApi } from "@/features/profile/api/profileApi"
import LearningPreferencesPanel from "@/features/profile/components/LearningPreferencesPanel"
import type { Profile } from "@/features/profile/types/profile.types"
import AppShell from "@/shared/components/layout/AppShell"
import LearnerShell from "@/shared/components/layout/LearnerShell"
import Button from "@/shared/ui/Button"
import Input from "@/shared/ui/Input"

export default function ProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState<Profile | null>(null)
  const { setDirty } = useOrganizationContext()
  const [baseline, setBaseline] = useState<Profile | null>(null)
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [retry, setRetry] = useState(0)
  const lock = useRef(false)
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")

  useEffect(() => {
    let live = true
    profileApi
      .me()
      .then((value) => {
        if (live) {
          setProfile(value)
          setBaseline(value)
          setError("")
        }
      })
      .catch((cause) => {
        if (live) setError(cause instanceof Error ? cause.message : "Unable to load your profile.")
      })
    return () => {
      live = false
    }
  }, [retry])
  const dirty = Boolean(profile && baseline && JSON.stringify(profile) !== JSON.stringify(baseline))
  useEffect(() => {
    setDirty("personal-profile", dirty)
    return () => setDirty("personal-profile", false)
  }, [dirty, setDirty])
  async function save(event: React.FormEvent) {
    event.preventDefault()
    if (!profile || lock.current || uploading) return
    lock.current = true
    setBusy(true)
    setError("")
    setMessage("")
    try {
      const value = await profileApi.update(profile)
      setProfile(value)
      setBaseline(value)
      setDirty("personal-profile", false)
      setMessage("Your profile has been updated.")
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Unable to save your profile.")
    } finally {
      lock.current = false
      setBusy(false)
    }
  }

  const content = (
    <section
      className={
        user?.role === "LEARNER" ? "learner-page learner-page--profile" : "profile-workspace"
      }
    >
      {!profile ? (
        <div
          className={`learner-state${error ? " learner-state--error" : ""}`}
          role={error ? "alert" : "status"}
        >
          {error || "Loading your profile…"}
          {error && <Button onClick={() => setRetry((n) => n + 1)}>Retry profile</Button>}
        </div>
      ) : (
        <>
          <header className="profile-hub__hero">
            <div className="profile-hub__avatar">
              <MediaPanel
                preferredId={
                  profile.avatarUrl?.startsWith("media:")
                    ? profile.avatarUrl.substring(6)
                    : undefined
                }
                scope="AVATAR"
                writable={!busy}
                onBusyChange={setUploading}
                onUploaded={(item) =>
                  setProfile((previous) =>
                    previous ? { ...previous, avatarUrl: `media:${item.id}` } : previous,
                  )
                }
              />
            </div>
            <div>
              <span>Your SkillProof</span>
              <h1>{profile.displayName}</h1>
              <p>
                {profile.headline ||
                  "Shape how you show up, what SkillProof knows about you, and the learning that belongs to you."}
              </p>
            </div>
          </header>

          {user?.role === "LEARNER" ? (
            <nav className="profile-hub__nav" aria-label="Profile sections">
              <a href="#about">
                <UserRound size={16} /> About you
              </a>
              <Link to="/app/learning">
                <BookOpen size={16} /> My learning
              </Link>
              <Link to="/app/eligibility">
                <CheckCircle2 size={16} /> Achievements
              </Link>
              <a href="#preferences">
                <Sparkles size={16} /> Preferences
              </a>
              <a href="#account">
                <Settings2 size={16} /> Account
              </a>
            </nav>
          ) : null}

          <form onSubmit={save} className="profile-hub__form" id="about">
            <fieldset disabled={busy || uploading} style={{ border: 0, padding: 0, minWidth: 0 }}>
              <section className="profile-hub__section">
                <div className="profile-hub__section-copy">
                  <h2>About you</h2>
                  <p>This is the identity people see across SkillProof.</p>
                </div>
                <div className="profile-hub__fields">
                  <Input label="Email" value={profile.email} disabled />
                  <Input
                    label="Display name"
                    minLength={2}
                    maxLength={100}
                    value={profile.displayName}
                    onChange={(event) =>
                      setProfile({ ...profile, displayName: event.target.value })
                    }
                    required
                  />
                  <Input
                    label="Headline"
                    maxLength={160}
                    value={profile.headline ?? ""}
                    onChange={(event) => setProfile({ ...profile, headline: event.target.value })}
                  />
                  <label className="profile-field">
                    Bio
                    <textarea
                      value={profile.bio ?? ""}
                      onChange={(event) => setProfile({ ...profile, bio: event.target.value })}
                      maxLength={1000}
                      placeholder="A little about what you are learning or working toward…"
                    />
                  </label>
                </div>
              </section>
              <section className="profile-hub__section" id="account">
                <div className="profile-hub__section-copy">
                  <h2>Language & time</h2>
                  <p>Used to make dates and language feel natural to you.</p>
                </div>
                <div className="profile-hub__fields profile-hub__fields--two">
                  <Input
                    label="Locale"
                    maxLength={16}
                    value={profile.locale}
                    onChange={(event) => setProfile({ ...profile, locale: event.target.value })}
                    required
                  />
                  <Input
                    label="Timezone"
                    maxLength={64}
                    value={profile.timezone}
                    onChange={(event) => setProfile({ ...profile, timezone: event.target.value })}
                    required
                  />
                </div>
              </section>
              {error ? (
                <p className="learner-inline-error" role="alert">
                  {error}
                </p>
              ) : null}
              {message ? (
                <p className="profile-hub__success">
                  <CheckCircle2 size={16} /> {message}
                </p>
              ) : null}
              <div className="profile-hub__save">
                <Button type="submit" size="lg" disabled={busy || uploading || !dirty}>
                  {busy ? "Saving…" : "Save changes"}
                </Button>
              </div>
            </fieldset>
          </form>
          {user?.role === "LEARNER" ? (
            <section className="profile-hub__section" id="preferences">
              <div className="profile-hub__section-copy">
                <h2>Learning preferences</h2>
                <p>
                  Your goals and interests belong to your personal space. You stay in control of
                  them.
                </p>
              </div>
              <LearningPreferencesPanel />
            </section>
          ) : null}
        </>
      )}
    </section>
  )

  return user?.role === "LEARNER" ? (
    <LearnerShell>{content}</LearnerShell>
  ) : (
    <AppShell>{content}</AppShell>
  )
}
