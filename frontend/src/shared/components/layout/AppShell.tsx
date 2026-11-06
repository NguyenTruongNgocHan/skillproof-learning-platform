import "@/features/organization/styles/workspace.css"
import "@/shared/styles/platform.css"
import { getNavForRole } from "@/app/config/navigation"
import { useAuth } from "@/features/auth/hooks/useAuth"
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider"
import AppSidebar from "@/shared/components/layout/AppSidebar"
import AppTopbar from "@/shared/components/layout/AppTopbar"
import MobileDrawer from "@/shared/components/layout/MobileDrawer"
import type { ReactNode } from "react"
import { useState, useEffect, type CSSProperties } from "react"
import { useLocation } from "react-router-dom"

interface AppShellProps {
  children: ReactNode
}

export default function AppShell({ children }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return localStorage.getItem("skillproof.sidebar-collapsed") === "true"
    } catch {
      return false
    }
  })
  function toggleSidebar() {
    setCollapsed((previous) => {
      const next = !previous
      try {
        localStorage.setItem("skillproof.sidebar-collapsed", String(next))
      } catch {
        /* UI preference only. */
      }
      return next
    })
  }
  const location = useLocation()
  const { user } = useAuth()
  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  const { grants, loading, error } = useOrganizationContext()
  const navItems = (user ? getNavForRole(user.role) : []).filter((item) => {
    if (user?.role !== "ORGANIZER") return true
    // Keep navigation visible during access verification; route guards block content.
    if (loading || error) return true
    if (
      [
        "/organizer/paths",
        "/organizer/courses",
        "/organizer/questions",
        "/organizer/library",
        "/organizer/access",
      ].includes(item.path)
    )
      return grants.includes("MANAGE_CONTENT")
    if (item.path === "/organizer/pricing") return grants.includes("MANAGE_PRICING")
    if (item.path === "/organizer/grading") return grants.includes("GRADE_ASSIGNMENTS")
    if (item.path === "/organizer/analytics") return grants.includes("VIEW_ANALYTICS")
    if (item.path === "/organizer/feedback") return grants.includes("MANAGE_FEEDBACK")
    if (item.path === "/organizer/governance") return grants.includes("MANAGE_PROFILE")
    if (item.path === "/organizer/certifications") return grants.includes("ISSUE_CERTIFICATES")
    return true
  })
  const currentItem = [...navItems]
    .sort((a, b) => b.path.length - a.path.length)
    .find(
      (item) =>
        location.pathname === item.path ||
        (item.path !== "/admin" &&
          item.path !== "/organizer" &&
          item.path !== "/app" &&
          location.pathname.startsWith(item.path + "/")) ||
        (item.path === "/organizer/certifications" &&
          location.pathname.startsWith("/organizer/certificates/")),
    )
  const pageTitle = currentItem?.label ?? "Dashboard"

  if (!user) return null

  return (
    <div
      className="app-shell organizer-shell"
      style={{ "--organizer-sidebar-width": collapsed ? "76px" : "240px" } as CSSProperties}
    >
      <a className="sr-only focus:not-sr-only" href="#workspace-content">
        Skip to content
      </a>
      {/* Desktop sidebar */}
      <aside className="app-sidebar-desktop">
        <AppSidebar items={navItems} collapsed={collapsed} onToggle={toggleSidebar} />
      </aside>

      {/* Mobile drawer */}
      <MobileDrawer open={sidebarOpen} onClose={() => setSidebarOpen(false)} items={navItems} />

      {/* Main area */}
      <div className="app-shell-content">
        <AppTopbar
          onMenuToggle={() => {
            if (window.matchMedia("(min-width: 768px)").matches) toggleSidebar()
            else setSidebarOpen(true)
          }}
          pageTitle={pageTitle}
          user={user}
        />
        <main id="workspace-content" className="app-main" tabIndex={-1}>
          {children}
        </main>
      </div>
    </div>
  )
}
