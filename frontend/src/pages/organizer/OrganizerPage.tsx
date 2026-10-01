import MediaPanel from "@/features/media/MediaPanel"
import { useCallback, useEffect, useState, type FormEvent } from "react"
import AppShell from "@/components/layout/AppShell"
import Button from "@/components/ui/Button"
import Input from "@/components/ui/Input"
import {
  organizationApi,
  type Member,
  type Organization,
  type Authority,
} from "@/features/organization/organizationApi"

const authorities: Authority[] = [
  "MANAGE_PROFILE",
  "MANAGE_MEMBERS",
  "MANAGE_CONTENT",
  "ISSUE_CERTIFICATES",
]

export default function OrganizerPage() {
  const [org, setOrg] = useState<Organization | null>(null)
  const [members, setMembers] = useState<Member[]>([])
  const [canManage, setCanManage] = useState(false)
  const [canEdit, setCanEdit] = useState(false)
  const [email, setEmail] = useState("")
  const [message, setMessage] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError("")
    try {
      const organization = await organizationApi.mine()
      setOrg(organization)
      const [memberAuthority, profileAuthority] = await Promise.all([
        organizationApi.can(organization.id, "MANAGE_MEMBERS"),
        organizationApi.can(organization.id, "MANAGE_PROFILE"),
      ])
      setCanManage(memberAuthority.allowed)
      setCanEdit(profileAuthority.allowed)
      setMembers(
        memberAuthority.allowed
          ? await organizationApi.members(organization.id)
          : [],
      )
      return true
    } catch (reason) {
      setError(
        reason instanceof Error
          ? reason.message
          : "Unable to load organization.",
      )
      return false
    } finally {
      setLoading(false)
    }
  }, [])
  useEffect(() => {
    void load()
  }, [load])

  async function run(action: () => Promise<unknown>, success: string) {
    setBusy(true)
    setError("")
    setMessage("")
    try {
      await action()
      const refreshed = await load()
      if (refreshed) setMessage(success)
      return refreshed
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Action failed.")
      return false
    } finally {
      setBusy(false)
    }
  }
  async function addMember(e: FormEvent) {
    e.preventDefault()
    if (!org || !email.trim()) return
    if (
      await run(
        () => organizationApi.addMember(org.id, email.trim()),
        "Member added. Grant permissions as needed.",
      )
    )
      setEmail("")
  }

  return (
    <AppShell>
      <main className="org-workspace">
        <div className="org-workspace-header">
          <span className="org-eyebrow">ORGANIZER WORKSPACE</span>
          <h1>{org?.displayName ?? "Organization"}</h1>
          <p>Manage your profile and team permissions after approval.</p>
        </div>
        {error && (
          <p className="org-error" role="alert">
            {error}{" "}
            <button type="button" onClick={() => void load()}>
              Retry
            </button>
          </p>
        )}
        {message && (
          <p className="org-success" role="status">
            {message}
          </p>
        )}
        {loading ? (
          <p role="status">Loading organization…</p>
        ) : (
          org && (
            <>
              <section className="org-panel">
                <h2>Organization profile</h2>
                <p>
                  Approval: <strong>{org.status}</strong>
                </p>
                {canEdit ? (
                  <form
                    key={org.id}
                    className="org-form"
                    onSubmit={(e) => {
                      e.preventDefault()
                      const data = new FormData(e.currentTarget)
                      void run(
                        () =>
                          organizationApi.update(org.id, {
                            displayName: String(data.get("displayName")).trim(),
                            website: String(data.get("website")).trim(),
                            industry: String(data.get("industry")).trim(),
                            contactPhone: String(
                              data.get("contactPhone"),
                            ).trim(),
                          }),
                        "Organization profile saved.",
                      )
                    }}
                  >
                    <Input
                      name="displayName"
                      label="Display name"
                      required
                      defaultValue={org.displayName}
                    />
                    <Input
                      name="industry"
                      label="Industry"
                      required
                      defaultValue={org.industry}
                    />
                    <Input
                      name="website"
                      label="Website"
                      type="url"
                      defaultValue={org.website ?? ""}
                    />
                    <Input
                      name="contactPhone"
                      label="Contact phone"
                      defaultValue={org.contactPhone ?? ""}
                    />
                    <Button type="submit" disabled={busy}>
                      Save organization
                    </Button>
                  </form>
                ) : (
                  <p>
                    Your account does not have permission to edit this profile.
                  </p>
                )}
              </section>
              <section className="org-panel">
                <MediaPanel
                  scope="ORGANIZATION"
                  target={org.id}
                  writable={canEdit}
                />
                <p>
                  Only authorized organization members and administrators can
                  download these documents.
                </p>
              </section>
              {canManage && (
                <section className="org-panel">
                  <h2>Team and authority</h2>
                  <p>
                    Add an active Organizer by email. New members have no
                    permissions until you grant them.
                  </p>
                  <form
                    className="org-form-grid"
                    onSubmit={(e) => void addMember(e)}
                  >
                    <Input
                      label="Organizer email"
                      type="email"
                      autoComplete="off"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                    <Button type="submit" disabled={busy}>
                      Add member
                    </Button>
                  </form>
                  <div className="org-member-list">
                    {members.map((member) => (
                      <article key={member.user_id} className="org-member">
                        <strong>{member.email}</strong>
                        {member.user_id !== org.ownerUserId && (
                          <>
                            <div className="org-grants">
                              {authorities.map((authority) => (
                                <label key={authority}>
                                  <input
                                    type="checkbox"
                                    checked={member.grants.includes(authority)}
                                    disabled={busy}
                                    onChange={(e) =>
                                      void run(
                                        () =>
                                          organizationApi.grant(
                                            org.id,
                                            member.user_id,
                                            authority,
                                            e.target.checked,
                                          ),
                                        "Permission updated.",
                                      )
                                    }
                                  />
                                  {authority.replace(/_/g, " ").toLowerCase()}
                                </label>
                              ))}
                            </div>
                            <Button
                              variant="outline"
                              disabled={busy}
                              onClick={() => {
                                if (window.confirm(`Remove ${member.email}?`))
                                  void run(
                                    () =>
                                      organizationApi.removeMember(
                                        org.id,
                                        member.user_id,
                                      ),
                                    "Member removed.",
                                  )
                              }}
                            >
                              Remove member
                            </Button>
                          </>
                        )}
                      </article>
                    ))}
                  </div>
                </section>
              )}
            </>
          )
        )}
      </main>
    </AppShell>
  )
}
