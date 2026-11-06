import type { ReactNode } from "react"
import { Link } from "react-router-dom"
export default function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  breadcrumb,
}: {
  eyebrow?: string
  title: string
  description?: string
  actions?: ReactNode
  breadcrumb?: { label: string; to: string }
}) {
  return (
    <header className="platform-page-header">
      <div>
        {breadcrumb && (
          <nav aria-label="Breadcrumb">
            <Link to={breadcrumb.to}>{breadcrumb.label}</Link> / <span>{title}</span>
          </nav>
        )}
        {eyebrow && <p className="platform-eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {description && <p className="platform-description">{description}</p>}
      </div>
      {actions && <div className="platform-page-actions">{actions}</div>}
    </header>
  )
}
