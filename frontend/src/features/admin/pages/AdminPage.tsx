import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import {
  Users,
  Building2,
  BookOpen,
  Library,
  ShieldCheck,
  MessageSquare,
  GraduationCap,
  ClipboardList,
  ArrowUpRight,
  RefreshCcw,
} from "lucide-react"
import AppShell from "@/shared/components/layout/AppShell"
import type { Organization } from "@/features/organization/types/organization.types"
import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import { adminOrganizationApi, type OrganizationDashboard } from "../api/adminOrganizationApi"
import "../styles/admin-console.css"
type Analytics = OrganizationDashboard & {
  submittedLast7Days?: number[]
  reviewedLast7Days?: number[]
  waitingBuckets?: number[]
}
const sections = [
  {
    title: "Organization applications",
    path: "/admin/organizations",
    icon: Building2,
    description: "Verification queue",
  },
  {
    title: "Issuer applications",
    path: "/admin/issuers",
    icon: ShieldCheck,
    description: "Issuer authorizations",
  },
  {
    title: "Course review",
    path: "/admin/course-review",
    icon: BookOpen,
    description: "Course versions",
  },
  {
    title: "Resource review",
    path: "/admin/library",
    icon: Library,
    description: "Learning resources",
  },
  {
    title: "Organizations",
    path: "/admin/organization-status",
    icon: GraduationCap,
    description: "Governance",
  },
  {
    title: "Certificates",
    path: "/admin/certificates",
    icon: ShieldCheck,
    description: "Certificate actions",
  },
  {
    title: "Feedback",
    path: "/admin/feedback",
    icon: MessageSquare,
    description: "Report moderation",
  },
  { title: "Accounts", path: "/admin/accounts", icon: Users, description: "Identity & access" },
]
export default function AdminPage() {
  const [data, setData] = useState<Analytics | null>(null)
  const [priority, setPriority] = useState<Organization[]>([])
  const [error, setError] = useState("")
  const [refresh, setRefresh] = useState(0)
  useEffect(() => {
    let active = true
    adminOrganizationApi
      .dashboardSummary()
      .then((r) => {
        if (active) {
          setData(r as Analytics)
          setError("")
        }
      })
      .catch((e) => {
        if (active) setError(e instanceof Error ? e.message : "Analytics unavailable")
      })
    adminOrganizationApi
      .pending()
      .then((r) => {
        if (active) setPriority(r)
      })
      .catch(() => {
        if (active) setPriority([])
      })
    return () => {
      active = false
    }
  }, [refresh])
  const submitted = data?.submittedLast7Days
  const reviewed = data?.reviewedLast7Days
  const hasSeries = Boolean(submitted?.length === 7 && reviewed?.length === 7)
  const max = Math.max(1, ...(submitted || []), ...(reviewed || []))
  const anyActivity = Boolean([...(submitted || []), ...(reviewed || [])].some((n) => n > 0))
  const buckets = data?.waitingBuckets
  const maxBucket = Math.max(1, ...(buckets || []))
  const stats = [
    {
      name: "Pending organizations",
      value: data?.organizationPending,
      detail: "Submitted and awaiting a decision",
      href: "/admin/organizations",
    },
    {
      name: "SLA breaches",
      value: data?.organizationOverdue,
      detail: "Waiting more than 72 hours",
      href: "/admin/organizations",
    },
    {
      name: "7-day submissions",
      value: submitted?.reduce((a, b) => a + b, 0),
      detail: "Organization revisions",
      href: "/admin/organizations",
    },
    {
      name: "7-day decisions",
      value: reviewed?.reduce((a, b) => a + b, 0),
      detail: "Organization review outcomes",
      href: "/admin/organizations",
    },
  ]
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Platform administration"
          title="Overview"
          description="Monitor review workload, queue aging and decisions across supported operations."
          actions={
            <button className="adm-btn" onClick={() => setRefresh((x) => x + 1)}>
              <RefreshCcw size={15} /> Refresh
            </button>
          }
        />
        {error && (
          <p className="adm-error" role="alert">
            {error}
          </p>
        )}
        <div className="adm-stats">
          {stats.map((s) => (
            <Link
              key={s.name}
              to={s.href}
              className="adm-stat"
              style={{ textDecoration: "none", color: "inherit" }}
            >
              <div className="adm-muted">
                {s.name} <ArrowUpRight size={14} style={{ float: "right" }} />
              </div>
              <strong>{s.value ?? "—"}</strong>
              <span className="adm-muted">{s.detail}</span>
            </Link>
          ))}
        </div>
        <div className="adm-grid">
          <section className="adm-panel">
            <div className="adm-panel-head">
              <div>
                <h2>Submission & decision volume</h2>
                <span className="adm-muted">Last 7 days · Organization applications</span>
              </div>
              <span className="adm-pill">7 days</span>
            </div>
            {hasSeries ? (
              anyActivity ? (
                <>
                  <div className="adm-chart">
                    {submitted!.map((n, i) => (
                      <div className="adm-chart-col" key={i}>
                        <div className="adm-chart-bars">
                          <div
                            className="adm-chart-bar"
                            title={`${n} submitted`}
                            style={{ height: `${(n / max) * 100}%` }}
                          />
                          <div
                            className="adm-chart-bar adm-chart-bar-alt"
                            title={`${reviewed![i]} reviewed`}
                            style={{ height: `${(reviewed![i] / max) * 100}%` }}
                          />
                        </div>
                        <small>
                          {new Date(Date.now() - (6 - i) * 86400000).toLocaleDateString(undefined, {
                            month: "numeric",
                            day: "numeric",
                          })}
                        </small>
                      </div>
                    ))}
                  </div>
                  <div className="adm-legend">
                    <span>
                      <i className="adm-dot" /> Submitted
                    </span>
                    <span>
                      <i className="adm-dot adm-dot-alt" /> Reviewed
                    </span>
                  </div>
                </>
              ) : (
                <div className="adm-empty">
                  <strong>No review activity in this period</strong>
                  <span>
                    Submission and decision counts are both zero for the selected 7-day window.
                  </span>
                </div>
              )
            ) : (
              <div className="adm-empty">
                Trend data is unavailable from the current backend response.
              </div>
            )}
          </section>
          <section className="adm-panel">
            <div className="adm-panel-head">
              <div>
                <h2>Queue aging</h2>
                <span className="adm-muted">Time since application submission</span>
              </div>
            </div>
            {buckets?.length === 4 ? (
              ["0–24h", "24–48h", "48–72h", ">72h"].map((label, i) => (
                <div className="adm-queue-item" key={label}>
                  <span style={{ fontSize: 12, minWidth: 58 }}>{label}</span>
                  <div className="adm-progress">
                    <span style={{ width: `${((buckets[i] || 0) / maxBucket) * 100}%` }} />
                  </div>
                  <strong>{buckets[i] || 0}</strong>
                </div>
              ))
            ) : (
              <div className="adm-empty">Queue aging data unavailable.</div>
            )}
            <p className="adm-muted">
              Overdue threshold: 72 hours. Counts use revision submission timestamps.
            </p>
          </section>
        </div>
        <section className="adm-panel adm-priority">
          <div className="adm-panel-head">
            <div>
              <h2>Action required</h2>
              <span className="adm-muted">Live organization applications awaiting a decision</span>
            </div>
            <Link to="/admin/organizations" className="adm-btn">
              Open queue <ArrowUpRight size={14} />
            </Link>
          </div>
          {priority.length ? (
            <div className="adm-priority-list">
              {priority.slice(0, 5).map((o) => (
                <Link key={o.id} to="/admin/organizations" className="adm-priority-row">
                  <span className="adm-person">
                    <span className="adm-avatar">{o.displayName.slice(0, 2).toUpperCase()}</span>
                    <span>
                      <strong className="adm-primary-cell">{o.displayName}</strong>
                      <small className="adm-secondary-cell">
                        {o.legalName} · {o.country}
                      </small>
                    </span>
                  </span>
                  <AdminStatusBadge value={o.status} />
                  <ArrowUpRight size={16} />
                </Link>
              ))}
            </div>
          ) : (
            <div className="adm-empty">
              <strong>Organization review queue is clear</strong>
              <span>No pending applications returned by the current queue endpoint.</span>
            </div>
          )}
        </section>
        <section className="adm-panel">
          <div className="adm-panel-head">
            <div>
              <h2>Review center & governance</h2>
              <span className="adm-muted">Jump to the appropriate operational queue</span>
            </div>
            <ClipboardList size={19} />
          </div>
          <div className="adm-quicklinks">
            {sections.map((s) => (
              <Link to={s.path} key={s.path}>
                <s.icon size={18} />
                <span>
                  {s.title}
                  <span className="adm-secondary-cell">{s.description}</span>
                </span>
                <ArrowUpRight size={14} style={{ marginLeft: "auto" }} />
              </Link>
            ))}
          </div>
        </section>
        <p className="adm-muted" style={{ marginTop: 14 }}>
          No other domain counts are fabricated. Platform-wide analytics require corresponding
          backend reporting contracts.
        </p>
      </section>
    </AppShell>
  )
}
