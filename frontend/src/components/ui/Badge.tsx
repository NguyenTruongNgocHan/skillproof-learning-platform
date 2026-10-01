import type { ReactNode } from "react"
type Variant = "default" | "success" | "warning" | "error" | "brand" | "outline"
export default function Badge({
  variant = "default",
  children,
  className = "",
}: {
  variant?: Variant
  children: ReactNode
  className?: string
}) {
  return (
    <span className={`sp-badge sp-badge--${variant} ${className}`}>
      {children}
    </span>
  )
}
