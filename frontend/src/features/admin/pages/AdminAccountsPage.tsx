import { useCallback, useEffect, useMemo, useState } from "react"
import { UserRound, Users, UserCheck, UserX, ShieldCheck, Copy, CalendarClock } from "lucide-react"
import AppShell from "@/shared/components/layout/AppShell"
import { adminApi } from "../api/adminApi"
import type { Account } from "../types/admin.types"
import AdminTable, { type Column } from "../components/AdminTable"
import AdminDialog from "../components/AdminDialog"
import AdminPageHeader from "../components/AdminPageHeader"
import AdminStatusBadge from "../components/AdminStatusBadge"
import "../styles/admin-console.css"
const columns: Column<Account>[] = [
  {
    key: "account",
    label: "Account",
    render: (r) => (
      <div className="adm-person">
        <span className="adm-avatar">{r.displayName?.slice(0, 2).toUpperCase() || "U"}</span>
        <span>
          <strong className="adm-primary-cell">{r.displayName}</strong>
          <small className="adm-secondary-cell">{r.email}</small>
        </span>
      </div>
    ),
    sortValue: (r) => r.displayName,
  },
  {
    key: "role",
    label: "Role",
    render: (r) => <span className="adm-pill">{r.role}</span>,
    sortValue: (r) => r.role,
  },
  {
    key: "status",
    label: "Status",
    render: (r) => <AdminStatusBadge value={r.status} />,
    sortValue: (r) => r.status,
  },
  {
    key: "created",
    label: "Created at",
    render: (r) => (
      <span className="adm-secondary-cell">
        {r.createdAt ? new Date(r.createdAt).toLocaleString() : "—"}
      </span>
    ),
    sortValue: (r) => r.createdAt || "",
  },
  {
    key: "last",
    label: "Last login",
    render: (r) => (
      <span className="adm-secondary-cell">
        {r.lastLoginAt ? new Date(r.lastLoginAt).toLocaleString() : "Never"}
      </span>
    ),
    sortValue: (r) => r.lastLoginAt || "",
  },
  {
    key: "id",
    label: "Account ID",
    render: (r) => (
      <span className="adm-secondary-cell" title={r.id}>
        {r.id.slice(0, 13)}…
      </span>
    ),
  },
]
export default function AdminAccountsPage() {
  const [rows, setRows] = useState<Account[]>([])
  const [selected, setSelected] = useState<Account | null>(null)
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)
  const [role, setRole] = useState<Account["role"]>("LEARNER")
  const [error, setError] = useState("")
  const [busy, setBusy] = useState(false)
  const [reload, setReload] = useState(0)
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    let active = true
    setLoading(true)
    adminApi
      .accountsPage(page, 20)
      .then((r) => {
        if (active) {
          setRows(r.content)
          setTotal(r.totalElements)
          setError("")
        }
      })
      .catch((e) => {
        if (active) setError(e instanceof Error ? e.message : "Could not load accounts")
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [page, reload])
  const open = useCallback((r: Account) => {
    setSelected(r)
    setRole(r.role)
  }, [])
  const counts = useMemo(
    () => ({
      active: rows.filter((r) => r.status === "ACTIVE").length,
      disabled: rows.filter((r) => r.status === "DISABLED").length,
      pending: rows.filter((r) => r.status === "PENDING_VERIFICATION").length,
    }),
    [rows],
  )
  async function changeRole() {
    if (!selected || busy || !window.confirm(`Change role for ${selected.email} to ${role}?`))
      return
    setBusy(true)
    try {
      await adminApi.changeRole(selected.id, role)
      setSelected(null)
      setReload((x) => x + 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Update failed")
    } finally {
      setBusy(false)
    }
  }
  async function changeStatus() {
    if (!selected || busy || !window.confirm(`Change status for ${selected.email}?`)) return
    setBusy(true)
    try {
      await adminApi.changeStatus(selected.id, selected.status === "ACTIVE" ? "DISABLED" : "ACTIVE")
      setSelected(null)
      setReload((x) => x + 1)
    } catch (e) {
      setError(e instanceof Error ? e.message : "Update failed")
    } finally {
      setBusy(false)
    }
  }
  return (
    <AppShell>
      <section className="adm-page">
        <AdminPageHeader
          eyebrow="Identity & access"
          title="Accounts"
          description="Manage platform identities, permissions and account status."
          actions={
            <span className="adm-pill">
              <Users size={14} /> {total} accounts
            </span>
          }
        />
        <div className="adm-stats adm-stats-accounts">
          <div className="adm-stat">
            <span className="adm-stat-icon">
              <Users size={19} />
            </span>
            <span className="adm-muted">Total accounts</span>
            <strong>{total}</strong>
            <small className="adm-secondary-cell">Platform accounts</small>
          </div>
          <div className="adm-stat">
            <span className="adm-stat-icon adm-icon-good">
              <UserCheck size={19} />
            </span>
            <span className="adm-muted">Active on loaded page</span>
            <strong>{counts.active}</strong>
            <small className="adm-secondary-cell">Of {rows.length} loaded</small>
          </div>
          <div className="adm-stat">
            <span className="adm-stat-icon adm-icon-warn">
              <CalendarClock size={19} />
            </span>
            <span className="adm-muted">Pending verification</span>
            <strong>{counts.pending}</strong>
            <small className="adm-secondary-cell">Of {rows.length} loaded</small>
          </div>
          <div className="adm-stat">
            <span className="adm-stat-icon adm-icon-danger">
              <UserX size={19} />
            </span>
            <span className="adm-muted">Disabled on loaded page</span>
            <strong>{counts.disabled}</strong>
            <small className="adm-secondary-cell">Of {rows.length} loaded</small>
          </div>
        </div>
        {error && (
          <p className="adm-error" role="alert">
            {error}
          </p>
        )}
        {loading && (
          <p className="adm-muted" role="status">
            Loading accounts…
          </p>
        )}
        <AdminTable
          rows={rows}
          columns={columns}
          searchText={(r) => `${r.displayName} ${r.email} ${r.role} ${r.status}`}
          filters={[
            { key: "status", label: "Status", value: (r) => r.status },
            { key: "role", label: "Role", value: (r) => r.role },
          ]}
          onOpen={open}
          serverPage={page}
          serverTotal={total}
          serverNext={(page + 1) * 20 < total}
          onServerPage={setPage}
          empty="No accounts on this page."
        />
        {selected && (
          <AdminDialog
            title={selected.displayName}
            subtitle={selected.email}
            onClose={() => setSelected(null)}
            footer={
              <>
                <button className="adm-btn" onClick={() => setSelected(null)}>
                  Cancel
                </button>
                <button
                  className="adm-btn adm-btn-danger"
                  disabled={busy}
                  onClick={() => void changeStatus()}
                >
                  {selected.status === "ACTIVE" ? "Disable account" : "Enable account"}
                </button>
                <button
                  className="adm-btn adm-btn-primary"
                  disabled={busy || role === selected.role}
                  onClick={() => void changeRole()}
                >
                  {busy ? "Saving…" : "Save role"}
                </button>
              </>
            }
          >
            <div className="adm-account-hero">
              <span className="adm-avatar adm-avatar-large">
                {selected.displayName?.slice(0, 2).toUpperCase() || "U"}
              </span>
              <div>
                <h3>{selected.displayName}</h3>
                <p className="adm-muted">{selected.email}</p>
                <AdminStatusBadge value={selected.status} />
              </div>
            </div>
            <div className="adm-detail-sections">
              <section className="adm-detail-panel">
                <h3>
                  <UserRound size={17} /> Basic information
                </h3>
                <dl className="adm-detail-list">
                  <dt>Full name</dt>
                  <dd>{selected.displayName}</dd>
                  <dt>Email address</dt>
                  <dd>{selected.email}</dd>
                  <dt>Account ID</dt>
                  <dd className="adm-id-line">
                    <code>{selected.id}</code>
                    <button
                      className="adm-icon-button"
                      aria-label="Copy account ID"
                      onClick={() => void navigator.clipboard.writeText(selected.id)}
                    >
                      <Copy size={15} />
                    </button>
                  </dd>
                  <dt>Created at</dt>
                  <dd>
                    {selected.createdAt ? new Date(selected.createdAt).toLocaleString() : "—"}
                  </dd>
                  <dt>Last login</dt>
                  <dd>
                    {selected.lastLoginAt
                      ? new Date(selected.lastLoginAt).toLocaleString()
                      : "Never"}
                  </dd>
                  <dt>Current role</dt>
                  <dd>
                    <span className="adm-pill">{selected.role}</span>
                  </dd>
                  <dt>Account status</dt>
                  <dd>
                    <AdminStatusBadge value={selected.status} />
                  </dd>
                </dl>
              </section>
              <section className="adm-detail-panel">
                <h3>
                  <ShieldCheck size={17} /> Access management
                </h3>
                <label className="adm-form-label" htmlFor="adm-role">
                  Assign a new role
                </label>
                <select
                  id="adm-role"
                  className="adm-field"
                  value={role}
                  onChange={(e) => setRole(e.target.value as Account["role"])}
                >
                  <option>LEARNER</option>
                  <option>ORGANIZER</option>
                  <option>ADMIN</option>
                </select>
                <p className="adm-muted">
                  Changing a role or disabling an account requires confirmation. The backend remains
                  responsible for authorization.
                </p>
              </section>
            </div>
          </AdminDialog>
        )}
      </section>
    </AppShell>
  )
}
