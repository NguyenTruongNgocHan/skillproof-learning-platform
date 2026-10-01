import type { ReactNode } from "react"
import PublicHeader from "@/components/layout/PublicHeader"
import PublicFooter from "@/components/layout/PublicFooter"
export default function LegalLayout({
  title,
  intro,
  children,
}: {
  title: string
  intro: string
  children: ReactNode
}) {
  return (
    <div className="public-page">
      <PublicHeader />
      <main className="legal-page">
        <div className="legal-intro">
          <span className="eyebrow">SKILLPROOF · PRODUCT POLICY DRAFT</span>
          <h1>{title}</h1>
          <p>{intro}</p>
          <div className="legal-review-note" role="note">
            Owner review required before public launch: confirm operator
            identity, contact channel, retention periods, infrastructure
            locations and all deployed integrations.
          </div>
        </div>
        <article className="legal-content">{children}</article>
      </main>
      <PublicFooter />
    </div>
  )
}
