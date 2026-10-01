import { cloneElement, isValidElement } from "react"
import type { ButtonHTMLAttributes, ReactNode } from "react"
type Variant = "primary" | "secondary" | "outline" | "ghost" | "destructive"
type Size = "sm" | "md" | "lg"
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  children: ReactNode
  asChild?: boolean
}
export default function Button({
  variant = "primary",
  size = "md",
  children,
  disabled,
  className = "",
  asChild = false,
  ...props
}: ButtonProps) {
  const classes = `sp-button sp-button--${variant} sp-button--${size} ${className}`
  if (asChild && isValidElement<{ className?: string }>(children))
    return cloneElement(children, {
      className: `${classes} ${children.props.className ?? ""}`,
    })
  return (
    <button
      {...props}
      type={props.type ?? "button"}
      disabled={disabled}
      className={classes}
    >
      {children}
    </button>
  )
}
