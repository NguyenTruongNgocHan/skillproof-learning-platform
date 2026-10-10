import "@/features/organization/styles/workspace.css";
import { getNavForRole } from "@/app/config/navigation";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useOrganizationContext } from "@/features/organization/providers/OrganizationProvider";
import AppSidebar from "@/shared/components/layout/AppSidebar";
import AppTopbar from "@/shared/components/layout/AppTopbar";
import MobileDrawer from "@/shared/components/layout/MobileDrawer";
import type { ReactNode } from "react";
import { useState, useEffect, type CSSProperties } from "react";
import { useLocation } from "react-router-dom";

interface AppShellProps {
  children: ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return (
        localStorage.getItem("skillproof.organizer.sidebar-collapsed") ===
        "true"
      );
    } catch {
      return false;
    }
  });
  function toggleSidebar() {
    setCollapsed((previous) => {
      const next = !previous;
      try {
        localStorage.setItem(
          "skillproof.organizer.sidebar-collapsed",
          String(next),
        );
      } catch {
        /* UI preference only. */
      }
      return next;
    });
  }
  const location = useLocation();
  const { user } = useAuth();
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const { grants, loading, error } = useOrganizationContext();
  const navItems = (user ? getNavForRole(user.role) : []).filter((item) => {
    if (user?.role !== "ORGANIZER") return true;
    // Keep navigation visible during access verification; route guards block content.
    if (loading || error) return true;
    if (
      [
        "/organizer/paths",
        "/organizer/courses",
        "/organizer/questions",
        "/organizer/library",
        "/organizer/access",
      ].includes(item.path)
    )
      return grants.includes("MANAGE_CONTENT");
    if (item.path === "/organizer/certifications")
      return grants.includes("ISSUE_CERTIFICATES");
    return true;
  });
  const currentItem =
    user?.role === "ORGANIZER"
      ? [...navItems]
          .sort((a, b) => b.path.length - a.path.length)
          .find(
            (item) =>
              location.pathname === item.path ||
              (item.path !== "/organizer" &&
                location.pathname.startsWith(item.path + "/")) ||
              (item.path === "/organizer/certifications" &&
                location.pathname.startsWith("/organizer/certificates/")),
          )
      : navItems.find((item) => item.path === location.pathname);
  const pageTitle = currentItem?.label ?? "Dashboard";

  if (!user) return null;

  return (
    <div
      className={`app-shell${user.role === "ORGANIZER" ? " organizer-shell" : ""}`}
      style={
        user.role === "ORGANIZER"
          ? ({
              "--organizer-sidebar-width": collapsed ? "76px" : "240px",
            } as CSSProperties)
          : undefined
      }
    >
      {/* Desktop sidebar */}
      <aside className="app-sidebar-desktop">
        <AppSidebar
          items={navItems}
          collapsed={user.role === "ORGANIZER" && collapsed}
          onToggle={user.role === "ORGANIZER" ? toggleSidebar : undefined}
        />
      </aside>

      {/* Mobile drawer */}
      <MobileDrawer
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        items={navItems}
      />

      {/* Main area */}
      <div className="app-shell-content">
        <AppTopbar
          onMenuToggle={() => {
            if (
              user.role === "ORGANIZER" &&
              window.matchMedia("(min-width: 768px)").matches
            )
              toggleSidebar();
            else setSidebarOpen(true);
          }}
          pageTitle={pageTitle}
          user={user}
        />
        <main className="app-main">{children}</main>
      </div>
    </div>
  );
}
