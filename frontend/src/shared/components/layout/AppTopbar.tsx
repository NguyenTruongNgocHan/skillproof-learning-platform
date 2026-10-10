import { Link, useLocation } from "react-router-dom";
import type { User } from "@/features/auth/types/auth.types";
import OrganizationSwitcher from "@/features/organization/components/OrganizationSwitcher";
import UserMenu from "@/shared/components/layout/UserMenu";
import ThemeSwitcher from "@/shared/ui/ThemeSwitcher";
import { Menu } from "lucide-react";
import "@/shared/styles/app-shell.css";

interface AppTopbarProps {
  onMenuToggle: () => void;
  pageTitle: string;
  user: User;
}
export default function AppTopbar({
  onMenuToggle,
  pageTitle,
  user,
}: AppTopbarProps) {
  const location = useLocation();
  const inCourse =
    user.role === "ORGANIZER" &&
    /^\/organizer\/courses\/[^/]+$/.test(location.pathname);
  return (
    <header className="app-topbar">
      <div className="app-topbar__left">
        <button
          className="app-menu-trigger app-topbar__menu"
          type="button"
          aria-label="Open navigation"
          onClick={onMenuToggle}
        >
          <Menu width={20} height={20} />
        </button>
        {user.role === "ORGANIZER" ? (
          <nav className="organizer-breadcrumb" aria-label="Breadcrumb">
            <Link to="/organizer">Workspace</Link>
            <span aria-hidden="true">/</span>
            {inCourse ? (
              <>
                <Link to="/organizer/courses">Courses</Link>
                <span aria-hidden="true">/</span>
                <span aria-current="page">Studio</span>
              </>
            ) : (
              <span aria-current="page">{pageTitle}</span>
            )}
          </nav>
        ) : (
          <span className="app-topbar__title" title={pageTitle}>
            {pageTitle}
          </span>
        )}
      </div>
      <div className="app-topbar__actions">
        {user.role === "ORGANIZER" && <OrganizationSwitcher />}
        <ThemeSwitcher />
        <UserMenu user={user} />
      </div>
    </header>
  );
}
