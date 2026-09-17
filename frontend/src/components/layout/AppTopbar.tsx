import { Menu, Bell } from "lucide-react";
import ThemeSwitcher from "@/components/ui/ThemeSwitcher";
import UserMenu from "@/components/layout/UserMenu";
import type { User } from "@/features/auth/types/auth.types";

interface AppTopbarProps {
  onMenuToggle: () => void;
  pageTitle: string;
  user: User;
}

export default function AppTopbar({ onMenuToggle, pageTitle, user }: AppTopbarProps) {
  return (
    <header
      style={{
        position: "sticky",
        top: 0,
        zIndex: 30,
        height: "56px",
        background: "var(--surface)",
        borderBottom: "1px solid var(--border)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "0 16px",
      }}
    >
      {/* Left */}
      <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
        <button
          className="md:hidden"
          onClick={onMenuToggle}
          style={{
            background: "none",
            border: "none",
            cursor: "pointer",
            color: "var(--fg)",
            display: "flex",
            padding: "4px",
          }}
        >
          <Menu width={20} height={20} />
        </button>
        <span
          style={{
            fontSize: "0.9375rem",
            fontWeight: "600",
            color: "var(--fg)",
          }}
        >
          {pageTitle}
        </span>
      </div>

      {/* Right */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "8px",
          position: "relative",
        }}
      >
        <ThemeSwitcher />
        <button
          style={{
            background: "none",
            border: "none",
            cursor: "pointer",
            color: "var(--fg-muted)",
            display: "flex",
            padding: "6px",
            borderRadius: "8px",
          }}
        >
          <Bell width={18} height={18} />
        </button>
        <UserMenu user={user} />
      </div>
    </header>
  );
}
