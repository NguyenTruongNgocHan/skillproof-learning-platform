import { ROUTES } from "@/app/config/appRoutes"
import { LANDING_NAV, sectionUrl } from "@/app/config/publicNavigation"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { getNextRouteForUserState } from "@/features/auth/utils/authFlow"
import BrandLogo from "@/shared/ui/BrandLogo"
import Button from "@/shared/ui/Button"
import ThemeSwitcher from "@/shared/ui/ThemeSwitcher"
import { Menu, X } from "lucide-react"
import { useEffect, useState } from "react"
import { Link, useLocation } from "react-router-dom"

export default function PublicHeader() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [active, setActive] = useState("")
  const location = useLocation()
  const { user } = useAuth()
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8)
    onScroll()
    window.addEventListener("scroll", onScroll, { passive: true })
    return () => window.removeEventListener("scroll", onScroll)
  }, [])
  useEffect(() => {
    setMobileOpen(false)
  }, [location.pathname, location.hash])
  useEffect(() => {
    if (!mobileOpen) return
    const close = (e: KeyboardEvent) => {
      if (e.key === "Escape") setMobileOpen(false)
    }
    window.addEventListener("keydown", close)
    return () => window.removeEventListener("keydown", close)
  }, [mobileOpen])
  useEffect(() => {
    if (location.pathname !== ROUTES.HOME) {
      setActive("")
      return
    }
    const sections = LANDING_NAV.map((item) => document.getElementById(item.id)).filter(
      (node): node is HTMLElement => !!node,
    )
    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) if (entry.isIntersecting) setActive(entry.target.id)
      },
      { rootMargin: "-100px 0px -58% 0px", threshold: 0 },
    )
    sections.forEach((section) => observer.observe(section))
    return () => observer.disconnect()
  }, [location.pathname])
  const links = LANDING_NAV.map((item) => (
    <Link
      key={item.id}
      to={sectionUrl(item.id)}
      aria-current={active === item.id ? "location" : undefined}
      className={`public-nav-link ${active === item.id ? "public-nav-link--active" : ""}`}
      onClick={() => setMobileOpen(false)}
    >
      {item.label}
    </Link>
  ))
  const accountRoute = getNextRouteForUserState(user)
  return (
    <header className={`public-header ${scrolled ? "public-header--scrolled" : ""}`}>
      <div className="public-header-inner">
        <BrandLogo />
        <nav aria-label="Main navigation" className="public-desktop-nav">
          {links}
        </nav>
        <div className="public-desktop-actions">
          <ThemeSwitcher />
          <Link className="public-verify-link" to={ROUTES.VERIFY_CERT}>
            Verify certificate
          </Link>
          {user ? (
            <Button variant="primary" size="sm" asChild>
              <Link to={accountRoute}>My workspace</Link>
            </Button>
          ) : (
            <>
              <Button variant="ghost" size="sm" asChild>
                <Link to={ROUTES.LOGIN}>Log in</Link>
              </Button>
              <Button variant="primary" size="sm" asChild>
                <Link to={ROUTES.REGISTER}>Get started</Link>
              </Button>
            </>
          )}
        </div>
        <div className="public-mobile-actions">
          <ThemeSwitcher />
          <button
            type="button"
            className="theme-toggle"
            onClick={() => setMobileOpen((v) => !v)}
            aria-expanded={mobileOpen}
            aria-controls="public-mobile-nav"
            aria-label={mobileOpen ? "Close menu" : "Open menu"}
          >
            {mobileOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </div>
      {mobileOpen && (
        <nav id="public-mobile-nav" aria-label="Mobile navigation" className="mobile-public-menu">
          {links}
          <Link to={ROUTES.VERIFY_CERT} onClick={() => setMobileOpen(false)}>
            Verify certificate
          </Link>
          {user ? (
            <Link to={accountRoute} onClick={() => setMobileOpen(false)}>
              My workspace
            </Link>
          ) : (
            <>
              <Link to={ROUTES.LOGIN} onClick={() => setMobileOpen(false)}>
                Log in
              </Link>
              <Link to={ROUTES.REGISTER} onClick={() => setMobileOpen(false)}>
                Get started
              </Link>
            </>
          )}
        </nav>
      )}
    </header>
  )
}
