import { adminApi } from "@/features/admin/api/adminApi"
import { adminOrganizationApi } from "@/features/admin/api/adminOrganizationApi"
import type { Account } from "@/features/admin/types/admin.types"
import MediaPanel from "@/features/media/components/MediaPanel"
import type { Organization } from "@/features/organization/types/organization.types"
import AppShell from "@/shared/components/layout/AppShell"
import Button from "@/shared/ui/Button"
import { useEffect, useState } from "react"
export default function AdminPage() {
  const [pending, setPending] = useState<Organization[]>([]),
    [accounts, setAccounts] = useState<Account[]>([]),
    [selected, setSelected] = useState<Organization | null>(null),
    [reason, setReason] = useState(""),
    [error, setError] = useState(""),
    [message, setMessage] = useState(""),
    [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(true)
  const [reviews, setReviews] = useState<Awaited<ReturnType<typeof adminOrganizationApi.reviews>>>(
    [],
  )
  const [reviewError, setReviewError] = useState("")
  useEffect(() => {
    if (!selected) {
      setReviews([])
      setReviewError("")
      return
    }
    let active = true
    adminOrganizationApi
      .reviews(selected.id)
      .then((rows) => {
        if (active) {
          setReviews(rows)
          setReviewError("")
        }
      })
      .catch((e: unknown) => {
        if (active) setReviewError(e instanceof Error ? e.message : "Could not load review history")
      })
    return () => {
      active = false
    }
  }, [selected])
  async function load() {
    setLoading(true)
    try {
      const [orgs, page] = await Promise.all([
        adminOrganizationApi.pending(),
        adminApi.accounts(50),
      ])
      setPending(orgs)
      setAccounts(page.content)
      setError("")
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not load administration data")
    } finally {
      setLoading(false)
    }
  }
  useEffect(() => {
    void load()
  }, [])
  async function run(action: () => Promise<unknown>) {
    setBusy(true)
    setError("")
    setMessage("")
    try {
      await action()
      setMessage("Change saved.")
      setSelected(null)
      setReason("")
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : "Action failed")
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <div className="org-workspace">
        <div className="org-workspace-header">
          <span className="org-eyebrow">PLATFORM ADMINISTRATION</span>
          <h1>Reviews and accounts</h1>
          <p>
            Decisions are saved with your identity and remain visible in the organization record.
          </p>
        </div>
        {error && (
          <p role="alert" className="org-error">
            {error}{" "}
            <Button variant="outline" onClick={() => void load()}>
              Retry
            </Button>
          </p>
        )}
        {message && (
          <p role="status" className="org-success">
            {message}
          </p>
        )}
        <section className="org-panel">
          <h2>
            Organization applications <span className="org-count">{pending.length}</span>
          </h2>
          {loading ? (
            <p>Loading applications…</p>
          ) : error ? null : pending.length === 0 ? (
            <p>No applications awaiting review.</p>
          ) : (
            <div className="org-member-list">
              {pending.map((o) => (
                <button className="org-review-row" key={o.id} onClick={() => setSelected(o)}>
                  <strong>{o.displayName}</strong>
                  <span>
                    {o.legalName} · {o.country}
                  </span>
                  <small>Submitted {new Date(o.createdAt).toLocaleDateString()}</small>
                </button>
              ))}
            </div>
          )}
        </section>
        {selected && (
          <section className="org-panel" aria-label="Review organization">
            <span className="org-eyebrow">APPLICATION DETAILS</span>
            <h2>{selected.legalName}</h2>
            <div className="org-detail-grid">
              <p>
                <b>Display name</b>
                <br />
                {selected.displayName}
              </p>
              <p>
                <b>Industry</b>
                <br />
                {selected.industry}
              </p>
              <p>
                <b>Country</b>
                <br />
                {selected.country}
              </p>
              <p>
                <b>Registration number</b>
                <br />
                {selected.registrationNumber || "Not provided"}
              </p>
              <p>
                <b>Contact</b>
                <br />
                {selected.contactName} · {selected.contactEmail}
              </p>
              <p>
                <b>Website</b>
                <br />
                {selected.website ? (
                  <a href={selected.website} target="_blank" rel="noreferrer">
                    {selected.website}
                  </a>
                ) : (
                  "Not provided"
                )}
              </p>
            </div>
            {reviewError ? (
              <p role="alert">Review history: {reviewError}</p>
            ) : (
              <div>
                <h3>Review history</h3>
                {reviews.length ? (
                  <ul>
                    {reviews.map((review, index) => (
                      <li key={`${review.reviewed_at}-${index}`}>
                        {review.decision} · {new Date(review.reviewed_at).toLocaleString()} ·{" "}
                        {review.reason || "No reason provided"}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p>No previous decisions recorded.</p>
                )}
              </div>
            )}
            <MediaPanel scope="ORGANIZATION" target={selected.id} />
            <label className="org-reason">
              Review reason (required when rejecting)
              <textarea
                maxLength={1000}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
              />
            </label>
            <div className="org-actions">
              <Button
                disabled={busy}
                variant="primary"
                onClick={() =>
                  void run(() =>
                    adminOrganizationApi.reviewOrganization(selected.id, "APPROVED", reason),
                  )
                }
              >
                Approve
              </Button>
              <Button
                disabled={busy || !reason.trim()}
                variant="outline"
                onClick={() =>
                  void run(() =>
                    adminOrganizationApi.reviewOrganization(selected.id, "REJECTED", reason),
                  )
                }
              >
                Reject
              </Button>
              <Button variant="ghost" onClick={() => setSelected(null)}>
                Close
              </Button>
            </div>
          </section>
        )}
        <section className="org-panel">
          <h2>Accounts</h2>
          <p>First 50 accounts. Account changes take effect at the backend.</p>
          {!loading && !error && (
            <div className="org-member-list">
              {accounts.map((a) => (
                <article className="org-account" key={a.id}>
                  <div>
                    <strong>{a.displayName}</strong>
                    <small>
                      {a.email} · {a.role} · {a.status}
                    </small>
                  </div>
                  {a.role !== "ADMIN" && (
                    <div className="org-actions">
                      <label className="org-role-label">
                        Role{" "}
                        <select
                          aria-label={`Role for ${a.email}`}
                          value={a.role}
                          disabled={busy}
                          onChange={(e) =>
                            void run(() =>
                              adminApi.changeRole(a.id, e.target.value as Account["role"]),
                            )
                          }
                        >
                          <option value="LEARNER">Learner</option>
                          <option value="ORGANIZER">Organizer</option>
                        </select>
                      </label>
                      {a.status !== "PENDING_VERIFICATION" && (
                        <Button
                          variant="outline"
                          disabled={busy}
                          onClick={() =>
                            void run(() =>
                              adminApi.changeStatus(
                                a.id,
                                a.status === "ACTIVE" ? "DISABLED" : "ACTIVE",
                              ),
                            )
                          }
                        >
                          {a.status === "ACTIVE" ? "Disable" : "Activate"}
                        </Button>
                      )}
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </AppShell>
  )
}
