import { Link, useLocation } from "react-router-dom";
import { Building2, PanelLeftClose, PanelLeftOpen } from "lucide-react";
import BrandLogo from "@/shared/ui/BrandLogo";
import type { NavItem } from "@/app/config/navigation";
export function OrganizerSidebar({
  items,
  collapsed = false,
  toggle,
}: {
  items: NavItem[];
  collapsed?: boolean;
  toggle?: () => void;
}) {
  const location = useLocation();
  return (
    <div className={`organizer-sidebar${collapsed ? " is-collapsed" : ""}`}>
      <header>
        {collapsed ? (
          <Link
            to="/organizer"
            aria-label="SkillProof workspace"
            className="organizer-sidebar-mark"
          >
            <Building2 size={22} />
          </Link>
        ) : (
          <BrandLogo />
        )}
        {toggle && (
          <button
            onClick={toggle}
            aria-label={collapsed ? "Expand sidebar" : "Collapse sidebar"}
            title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          >
            {collapsed ? (
              <PanelLeftOpen size={17} />
            ) : (
              <PanelLeftClose size={17} />
            )}
          </button>
        )}
      </header>
      <nav aria-label="Organizer navigation">
        {!collapsed && <p>Workspace</p>}
        {items.map((item) => {
          const Icon = item.icon;
          const active =
            location.pathname === item.path ||
            (item.path !== "/organizer" &&
              location.pathname.startsWith(item.path + "/")) ||
            (item.path === "/organizer/certifications" &&
              location.pathname.startsWith("/organizer/certificates/"));
          return (
            <Link
              key={item.path}
              to={item.path}
              aria-current={active ? "page" : undefined}
              title={collapsed ? item.label : undefined}
            >
              <Icon size={18} />
              {!collapsed && <span>{item.label}</span>}
            </Link>
          );
        })}
      </nav>
      {!collapsed && (
        <footer>
          <span className="organizer-sidebar-dot" /> SkillProof workspace
        </footer>
      )}
    </div>
  );
}
