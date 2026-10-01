import MediaPanel from "@/features/media/MediaPanel"
import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import Input from "@/components/ui/Input"
import Button from "@/components/ui/Button"
import { profileApi, type Profile } from "@/features/profile/profileApi"


export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null)
  const [message, setMessage] = useState("")

  useEffect(() => {
    profileApi.me().then(setProfile)
  }, [])

  if (!profile)
    return (
      <main className="min-h-screen p-8" style={{ background: "var(--bg)" }}>
        Loading…
      </main>
    )

  async function save(event: React.FormEvent) {
    event.preventDefault()

    if (!profile) return

    setProfile(await profileApi.update(profile))
    setMessage("Profile updated.")
  }

  return (
    <main
      className="min-h-screen px-6 py-10"
      style={{ background: "var(--bg)", color: "var(--fg)" }}
    >
      <form
        onSubmit={save}
        className="mx-auto max-w-xl space-y-5 rounded-2xl p-7"
        style={{ background: "var(--surface)" }}
      >
        <div>
          <Link to="/app" style={{ color: "var(--brand)" }}>
            ← Dashboard
          </Link>
          <h1 className="mt-3 text-2xl font-bold">Your profile</h1>
        </div>
        <MediaPanel
          preferredId={
            profile.avatarUrl?.startsWith("media:")
              ? profile.avatarUrl.substring(6)
              : undefined
          }
          scope="AVATAR"
          writable
          onUploaded={(item) =>
            setProfile((previous) =>
              previous
                ? { ...previous, avatarUrl: `media:${item.id}` }
                : previous,
            )
          }
        />
        <p className="text-sm">
          After uploading a new avatar, save your profile to use it.
        </p>
        <Input label="Email" value={profile.email} disabled />
        <Input
          label="Display name"
          value={profile.displayName}
          onChange={(e) =>
            setProfile({ ...profile, displayName: e.target.value })
          }
          required
        />
        <Input
          label="Headline"
          value={profile.headline ?? ""}
          onChange={(e) => setProfile({ ...profile, headline: e.target.value })}
        />
        <label className="block text-sm font-medium">
          Bio
          <textarea
            className="mt-2 min-h-32 w-full rounded-lg border p-3"
            style={{ background: "var(--bg)", borderColor: "var(--border)" }}
            value={profile.bio ?? ""}
            onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
          />
        </label>
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Locale"
            value={profile.locale}
            onChange={(e) => setProfile({ ...profile, locale: e.target.value })}
            required
          />
          <Input
            label="Timezone"
            value={profile.timezone}
            onChange={(e) =>
              setProfile({ ...profile, timezone: e.target.value })
            }
            required
          />
        </div>
        {message && (
          <p className="text-sm" style={{ color: "var(--success)" }}>
            {message}
          </p>
        )}
        <Button type="submit" variant="primary">
          Save changes
        </Button>
      </form>
    </main>
  )
}
