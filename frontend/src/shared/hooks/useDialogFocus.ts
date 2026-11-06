import { useEffect, useRef, type RefObject } from "react"
/** Scope keyboard focus and restore the invoker without resetting on parent rerenders. */
export function useDialogFocus<T extends HTMLElement>(
  root: RefObject<T | null>,
  open: boolean,
  close: () => void,
) {
  const closeRef = useRef(close)
  closeRef.current = close
  useEffect(() => {
    if (!open) return
    const previous = document.activeElement instanceof HTMLElement ? document.activeElement : null
    const overflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    const items = () =>
      Array.from(
        root.current?.querySelectorAll<HTMLElement>(
          'button:not([disabled]),a[href],input:not([disabled]),textarea:not([disabled]),select:not([disabled]),[tabindex="0"]',
        ) ?? [],
      ).filter((e) => !e.hidden && !e.closest("[hidden], [inert]"))
    ;(items()[0] ?? root.current)?.focus()
    const handler = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        event.preventDefault()
        event.stopPropagation()
        closeRef.current()
      }
      if (event.key !== "Tab") return
      const all = items()
      const first = all[0]
      const last = all.at(-1)
      if (!first || !last) {
        event.preventDefault()
        root.current?.focus()
        return
      }
      if (
        event.shiftKey &&
        (document.activeElement === first || !root.current?.contains(document.activeElement))
      ) {
        event.preventDefault()
        last.focus()
      } else if (
        !event.shiftKey &&
        (document.activeElement === last || !root.current?.contains(document.activeElement))
      ) {
        event.preventDefault()
        first.focus()
      }
    }
    document.addEventListener("keydown", handler)
    return () => {
      document.removeEventListener("keydown", handler)
      document.body.style.overflow = overflow
      previous?.focus()
    }
  }, [open, root])
}
