import { useAuth } from "@/features/auth/hooks/useAuth";
import { OrganizerSidebar } from "@/features/organization/components/OrganizerSidebar";
import type { NavItem } from "@/app/config/navigation";
import BrandLogo from "@/shared/ui/BrandLogo";
import { Link, useLocation } from "react-router-dom";

interface AppSidebarProps {
  items: NavItem[];
  collapsed?: boolean;
  onToggle?: () => void;
}

export default function AppSidebar({
  items,
  collapsed = false,
  onToggle,
}: AppSidebarProps) {
  const location = useLocation();
  const { user } = useAuth();
  if (user?.role === "ORGANIZER")
    return (
      <OrganizerSidebar items={items} collapsed={collapsed} toggle={onToggle} />
    );

  return (
    <div
      style={{
        background: "var(--surface)",
        borderRight: "1px solid var(--border)",
        width: "256px",
        height: "100%",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* Logo */}
      <div
        style={{
          padding: "20px 16px",
          borderBottom: "1px solid var(--border)",
        }}
      >
        <BrandLogo />
      </div>

      {/* Nav items */}
      <nav style={{ flex: 1, overflowY: "auto", padding: "8px 0" }}>
        {items.map((item) => {
          const isActive =
            location.pathname === item.path ||
            (item.path.startsWith("/organizer/") &&
              location.pathname.startsWith(item.path + "/")) ||
            (item.path === "/organizer/certifications" &&
              location.pathname.startsWith("/organizer/certificates/"));
          const Icon = item.icon;
          return (
            <Link
              key={item.path}
              to={item.path}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "12px",
                padding: "10px 16px",
                borderRadius: "8px",
                margin: "2px 8px",
                fontSize: "0.875rem",
                fontWeight: "500",
                textDecoration: "none",
                background: isActive ? "var(--brand-soft)" : "transparent",
                color: isActive ? "var(--brand)" : "var(--fg-muted)",
                transition: "background 0.15s, color 0.15s",
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  (e.currentTarget as HTMLAnchorElement).style.background =
                    "var(--bg-subtle)";
                  (e.currentTarget as HTMLAnchorElement).style.color =
                    "var(--fg)";
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  (e.currentTarget as HTMLAnchorElement).style.background =
                    "transparent";
                  (e.currentTarget as HTMLAnchorElement).style.color =
                    "var(--fg-muted)";
                }
              }}
            >
              <Icon width={18} height={18} />
              <span style={{ flex: 1 }}>{item.label}</span>
              {item.badge !== undefined && (
                <span
                  style={{
                    background: "var(--brand)",
                    color: "#fff",
                    borderRadius: "9999px",
                    fontSize: "0.7rem",
                    fontWeight: "600",
                    minWidth: "18px",
                    height: "18px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    padding: "0 5px",
                  }}
                >
                  {item.badge}
                </span>
              )}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
