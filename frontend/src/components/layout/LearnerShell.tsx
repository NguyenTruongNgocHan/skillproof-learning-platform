import type { ReactNode } from "react"
import { useEffect, useState } from "react"
import { NavLink } from "react-router-dom"
import {
  Bell,
  BookOpen,
  Compass,
  Moon,
  Sparkles,
  Sun,
  Users,
} from "lucide-react"

import { useTheme } from "@/app/providers/ThemeProvider"
import { useAuth } from "@/features/auth/hooks/useAuth"
import logo from "@/imports/logo_light.png"

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

  const initials = getInitials(user?.fullName)

  return (
    <div className="learner-shell">
      <header className={`learner-nav${scrolled ? " learner-nav--scrolled" : ""}`}>
        <div className="learner-nav__inner">
          <NavLink to="/app" className="learner-nav__brand" aria-label="SkillProof home">
            <img src={logo} alt="SkillProof" />
          </NavLink>

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
                <Icon size={17} strokeWidth={1.8} />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>

          <div className="learner-nav__actions">
            <button type="button" className="learner-nav__icon-button" aria-label="Notifications">
              <Bell size={18} />
            </button>

            <button
              type="button"
              className="learner-nav__icon-button"
              onClick={() => setTheme(resolvedTheme === "dark" ? "light" : "dark")}
              aria-label={`Switch to ${resolvedTheme === "dark" ? "light" : "dark"} theme`}
            >
              {resolvedTheme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            </button>

            <NavLink to="/profile" className="learner-nav__avatar" aria-label="Open profile">
              {initials}
            </NavLink>
          </div>
        </div>
      </header>

      <main className="learner-shell__content">{children}</main>
    </div>
  )
}

function getInitials(name?: string | null) {
  if (!name?.trim()) return "SP"
  return name.trim().split(/\s+/).slice(-2).map((part) => part[0]?.toUpperCase()).join("")
}
