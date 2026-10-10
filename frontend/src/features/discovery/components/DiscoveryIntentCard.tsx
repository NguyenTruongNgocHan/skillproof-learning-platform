import type { LucideIcon } from "lucide-react"
import { ArrowUpRight } from "lucide-react"

interface DiscoveryIntentCardProps {
  icon: LucideIcon
  title: string
  description: string
  featured?: boolean
  onClick: () => void
}

export default function DiscoveryIntentCard({
  icon: Icon,
  title,
  description,
  featured = false,
  onClick,
}: DiscoveryIntentCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`discovery-intent ${featured ? "discovery-intent--featured" : ""}`}
    >
      <span className="discovery-intent__icon">
        <Icon size={21} strokeWidth={1.8} />
      </span>

      <span className="min-w-0 text-left">
        <strong className="discovery-intent__title">{title}</strong>
        <span className="discovery-intent__description">{description}</span>
      </span>

      <ArrowUpRight className="discovery-intent__arrow" size={18} />
    </button>
  )
}
