import { ArrowRight } from "lucide-react"
import type { ReactNode } from "react"
import { Link } from "react-router-dom"

import { useDiscoveryMotion } from "@/features/discovery/hooks/useDiscoveryMotion"

interface DiscoverySectionProps {
  title: string
  eyebrow?: string
  description?: string
  actionLabel?: string
  actionTo?: string
  children: ReactNode
  className?: string
}

export default function DiscoverySection({
  title,
  eyebrow,
  description,
  actionLabel,
  actionTo,
  children,
  className = "",
}: DiscoverySectionProps) {
  const { ref, revealed } = useDiscoveryMotion()

  return (
    <section
      ref={ref}
      className={`discovery-section discovery-reveal${
        revealed ? " is-revealed" : ""
      } ${className}`.trim()}
    >
      <div className="discovery-section__heading">
        <div>
          {eyebrow ? <span className="discovery-section__eyebrow">{eyebrow}</span> : null}
          <h2>{title}</h2>
          {description ? <p>{description}</p> : null}
        </div>

        {actionLabel && actionTo ? (
          <Link to={actionTo} className="discovery-section__action">
            {actionLabel}
            <ArrowRight size={16} />
          </Link>
        ) : null}
      </div>
      {children}
    </section>
  )
}
