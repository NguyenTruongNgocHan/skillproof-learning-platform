import { useCallback, useEffect, useRef, useState } from "react"
import type { RefCallback } from "react"

export function useDiscoveryMotion(): {
  ref: RefCallback<HTMLElement>
  revealed: boolean
} {
  const nodeRef = useRef<HTMLElement | null>(null)
  const [revealed, setRevealed] = useState(false)

  const ref = useCallback<RefCallback<HTMLElement>>((node) => {
    nodeRef.current = node
  }, [])

  useEffect(() => {
    const node = nodeRef.current
    if (!node || revealed) return

    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      setRevealed(true)
      return
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry?.isIntersecting) return
        setRevealed(true)
        observer.disconnect()
      },
      { threshold: 0.12, rootMargin: "0px 0px -6%" },
    )

    observer.observe(node)
    return () => observer.disconnect()
  }, [revealed])

  return { ref, revealed }
}

export function useSessionOnce(key: string) {
  const storageKey = `skillproof-discovery:${key}`
  const [seen, setSeen] = useState(() => {
    try {
      return sessionStorage.getItem(storageKey) === "1"
    } catch {
      return false
    }
  })

  const markSeen = useCallback(() => {
    try {
      sessionStorage.setItem(storageKey, "1")
    } catch {
      // Session storage is optional.
    }
    setSeen(true)
  }, [storageKey])

  return { seen, markSeen }
}
