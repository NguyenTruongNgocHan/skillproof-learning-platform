import type { ReactNode } from "react"
import { ArrowRight } from "lucide-react"
import { Link } from "react-router-dom"
import { useDiscoveryMotion } from "@/features/discovery/hooks/useDiscoveryMotion"

interface DiscoverySectionProps {
  eyebrow: string
  title: string
  description?: string
  actionLabel?: string
  actionTo?: string
  children: ReactNode
  className?: string
}

export default function DiscoverySection({
  eyebrow,
  title,
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
      className={`discovery-section discovery-reveal${revealed ? " is-revealed" : ""} ${className}`.trim()}
    >
      <div className="discovery-section__heading">
        <div>
          <span className="discovery-section__eyebrow">{eyebrow}</span>
          <h2>{title}</h2>
          {description && <p>{description}</p>}
        </div>

        {actionLabel && actionTo && (
          <Link to={actionTo} className="discovery-section__action">
            {actionLabel}
            <ArrowRight size={16} />
          </Link>
        )}
      </div>
      {children}
    </section>
  )
}
