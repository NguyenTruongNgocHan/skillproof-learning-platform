import { usePointerGlow } from "@/features/discovery/hooks/usePointerGlow"
import type { HTMLAttributes, ReactNode } from "react"

interface InteractiveCardProps extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
}

export default function InteractiveCard({
  children,
  className = "",
  ...props
}: InteractiveCardProps) {
  const pointer = usePointerGlow()

  return (
    <div {...props} {...pointer} className={`interactive-card ${className}`.trim()}>
      {children}
    </div>
  )
}
