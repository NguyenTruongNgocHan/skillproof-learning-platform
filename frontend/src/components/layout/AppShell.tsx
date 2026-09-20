import { useState } from "react";
import type { ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getNavForRole } from "@/config/navigation";
import AppSidebar from "@/components/layout/AppSidebar";
import AppTopbar from "@/components/layout/AppTopbar";
import MobileDrawer from "@/components/layout/MobileDrawer";

interface AppShellProps {
  children: ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const { user } = useAuth();

  const navItems = user ? getNavForRole(user.role) : [];
  const currentItem = navItems.find((item) => item.path === location.pathname);
  const pageTitle = currentItem?.label ?? "Dashboard";

  if (!user) return null;

  return (
    <div className="app-shell">
      {/* Desktop sidebar */}
      <aside
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          bottom: 0,
          width: "256px",
          zIndex: 40,
          display: "none",
        }}
        className="md:block"
      >
        <AppSidebar items={navItems} />
      </aside>

      {/* Mobile drawer */}
      <MobileDrawer
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        items={navItems}
      />

      {/* Main area */}
      <div style={{ flex: 1, marginLeft: 0 }} className="md:ml-64">
        <AppTopbar
          onMenuToggle={() => setSidebarOpen(true)}
          pageTitle={pageTitle}
          user={user}
        />
        <main className="app-main">{children}</main>
      </div>
    </div>
  );
}
