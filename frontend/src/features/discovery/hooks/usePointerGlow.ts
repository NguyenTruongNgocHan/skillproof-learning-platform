import type { PointerEvent } from "react"

export function usePointerGlow() {
  function onPointerMove(event: PointerEvent<HTMLElement>) {
    const rect = event.currentTarget.getBoundingClientRect()
    event.currentTarget.style.setProperty("--pointer-x", `${event.clientX - rect.left}px`)
    event.currentTarget.style.setProperty("--pointer-y", `${event.clientY - rect.top}px`)
  }

  function onPointerLeave(event: PointerEvent<HTMLElement>) {
    event.currentTarget.style.removeProperty("--pointer-x")
    event.currentTarget.style.removeProperty("--pointer-y")
  }

  return { onPointerMove, onPointerLeave }
}
