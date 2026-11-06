export default function AdminStatusBadge({ value }: { value: string }) {
  const s = value.toUpperCase()
  const tone = /ACTIVE|APPROVED|PUBLISHED|RESOLVED|VERIFIED/.test(s)
    ? "good"
    : /REJECT|DISABLED|REVOKED|SUSPENDED|OVERDUE/.test(s)
      ? "danger"
      : /PENDING|NEEDS|DRAFT|REVIEW/.test(s)
        ? "warn"
        : "neutral"
  return (
    <span className={`adm-status adm-status-${tone}`}>
      <span className="adm-status-dot" />
      {value.replaceAll("_", " ")}
    </span>
  )
}
