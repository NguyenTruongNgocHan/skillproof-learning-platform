import { Bell, BookOpen, Compass, Moon, Sparkles, Sun, Users } from "lucide-react"
import type { ReactNode } from "react"
import { useEffect, useState } from "react"
import { NavLink } from "react-router-dom"

import { useTheme } from "@/app/providers/ThemeProvider"
import { useAuth } from "@/features/auth/hooks/useAuth"
import UserMenu from "@/shared/components/layout/UserMenu"
import BrandLogo from "@/shared/ui/BrandLogo"

interface LearnerShellProps {
  children: ReactNode
}

const navigation = [
  { label: "Discover", to: "/app", icon: Compass, end: true },
  { label: "My Learning", to: "/app/learning", icon: BookOpen },
  { label: "Practice", to: "/practice", icon: Sparkles },
  { label: "Community", to: "/community", icon: Users },
]

export default function LearnerShell({ children }: LearnerShellProps) {
  const { user } = useAuth()
  const { resolvedTheme, setTheme } = useTheme()
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 18)
    onScroll()
    window.addEventListener("scroll", onScroll, { passive: true })
    return () => window.removeEventListener("scroll", onScroll)
  }, [])

  return (
    <div className="learner-shell skillproof-app-theme">
      <header className={`learner-nav${scrolled ? " learner-nav--scrolled" : ""}`}>
        <div className="learner-nav__inner">
          <div className="learner-nav__brand">
            <BrandLogo to="/app" />
          </div>

          <nav className="learner-nav__links" aria-label="Learner navigation">
            {navigation.map(({ label, to, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                className={({ isActive }) =>
                  `learner-nav__link${isActive ? " learner-nav__link--active" : ""}`
                }
              >
                <Icon size={16} strokeWidth={1.8} />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>

          <div className="learner-nav__actions">
            <button type="button" className="learner-nav__icon-button" aria-label="Notifications">
              <Bell size={17} />
            </button>
            <button
              type="button"
              className="learner-nav__icon-button"
              onClick={() => setTheme(resolvedTheme === "dark" ? "light" : "dark")}
              aria-label={`Switch to ${resolvedTheme === "dark" ? "light" : "dark"} theme`}
            >
              {resolvedTheme === "dark" ? <Sun size={17} /> : <Moon size={17} />}
            </button>
            {user ? <UserMenu user={user} /> : null}
          </div>
        </div>
      </header>
      <main className="learner-shell__content">{children}</main>
    </div>
  )
}
