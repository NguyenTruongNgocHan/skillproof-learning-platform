import { useEffect, useId, useRef, type ReactNode } from "react"
import { createPortal } from "react-dom"
import { X } from "lucide-react"

type AdminDialogProps = {
  title: string
  subtitle?: string
  onClose: () => void
  children: ReactNode
  footer?: ReactNode
}

export default function AdminDialog({
  title,
  subtitle,
  onClose,
  children,
  footer,
}: AdminDialogProps) {
  const heading = useId()
  const dialogRef = useRef<HTMLElement>(null)
  const closeRef = useRef<HTMLButtonElement>(null)
  const onCloseRef = useRef(onClose)
  onCloseRef.current = onClose

  useEffect(() => {
    const previouslyFocused =
      document.activeElement instanceof HTMLElement ? document.activeElement : null
    const oldOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    closeRef.current?.focus()

    const onKeyDown = (event: KeyboardEvent) => {
      const dialog = dialogRef.current
      if (!dialog) return
      if (event.key === "Escape") {
        event.preventDefault()
        event.stopPropagation()
        onCloseRef.current()
        return
      }
      if (event.key !== "Tab") return
      const focusable = Array.from(
        dialog.querySelectorAll<HTMLElement>(
          'button:not([disabled]), input:not([disabled]), textarea:not([disabled]), select:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])',
        ),
      ).filter((element) => element.getClientRects().length > 0)
      if (!focusable.length) {
        event.preventDefault()
        return
      }
      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      if (
        event.shiftKey &&
        (document.activeElement === first || !dialog.contains(document.activeElement))
      ) {
        event.preventDefault()
        last.focus()
      } else if (
        !event.shiftKey &&
        (document.activeElement === last || !dialog.contains(document.activeElement))
      ) {
        event.preventDefault()
        first.focus()
      }
    }
    document.addEventListener("keydown", onKeyDown)
    return () => {
      document.removeEventListener("keydown", onKeyDown)
      document.body.style.overflow = oldOverflow
      previouslyFocused?.focus()
    }
  }, [])

  return createPortal(
    <div
      className="adm-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onCloseRef.current()
      }}
    >
      <section
        ref={dialogRef}
        className="adm-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={heading}
      >
        <header className="adm-dialog-header">
          <div>
            <h2 id={heading}>{title}</h2>
            {subtitle && <p>{subtitle}</p>}
          </div>
          <button
            ref={closeRef}
            className="adm-icon-button adm-dialog-close"
            aria-label="Close dialog"
            onClick={() => onCloseRef.current()}
          >
            <X size={20} />
          </button>
        </header>
        <div className="adm-dialog-body">{children}</div>
        {footer && <footer className="adm-dialog-footer">{footer}</footer>}
      </section>
    </div>,
    document.body,
  )
}
