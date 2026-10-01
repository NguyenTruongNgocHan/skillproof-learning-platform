import { useEffect } from "react"
import { useLocation } from "react-router-dom"
export default function ScrollToRoute() {
  const { pathname, hash } = useLocation()
  useEffect(() => {
    let frame = 0
    let attempts = 0
    const scroll = () => {
      const id = decodeURIComponent(hash.slice(1))
      if (!id) {
        window.scrollTo({ top: 0, behavior: "instant" })
        return
      }
      const target = document.getElementById(id)
      if (target)
        target.scrollIntoView({
          behavior: window.matchMedia("(prefers-reduced-motion: reduce)")
            .matches
            ? "instant"
            : "smooth",
          block: "start",
        })
      else if (++attempts < 12) frame = requestAnimationFrame(scroll)
    }
    frame = requestAnimationFrame(scroll)
    return () => cancelAnimationFrame(frame)
  }, [pathname, hash])
  return null
}
