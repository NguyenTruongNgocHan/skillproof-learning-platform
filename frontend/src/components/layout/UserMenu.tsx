import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { User, Settings, HelpCircle, LogOut } from "lucide-react";
import { useAuth } from "@/features/auth/hooks/useAuth";
import Avatar from "@/components/ui/Avatar";
import type { User as UserType } from "@/features/auth/types/auth.types";

interface UserMenuProps {
  user: UserType;
}

export default function UserMenu({ user }: UserMenuProps) {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { logout } = useAuth();

  async function handleSignOut() {
    setOpen(false);
    await logout();
    navigate("/");
  }

  const menuItems = [
    { icon: User, label: "Profile", action: () => navigate("/profile") },
    { icon: Settings, label: "Settings", action: () => navigate("/settings") },
    { icon: HelpCircle, label: "Help", action: () => {} },
  ];

  return (
    <div style={{ position: "relative" }}>
      <button
        onClick={() => setOpen((v) => !v)}
        style={{
          background: "none",
          border: "none",
          cursor: "pointer",
          padding: "2px",
          borderRadius: "9999px",
        }}
      >
        <Avatar name={user.fullName ?? user.email ?? "User"} size="sm" />
      </button>

      {open && (
        <>
          <div
            style={{ position: "fixed", inset: 0, zIndex: 10 }}
            onClick={() => setOpen(false)}
          />
          <div
            style={{
              position: "absolute",
              top: "calc(100% + 8px)",
              right: 0,
              zIndex: 20,
              background: "var(--surface-elevated)",
              borderRadius: "12px",
              boxShadow: "0 8px 24px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.08)",
              border: "1px solid var(--border)",
              minWidth: "180px",
              padding: "6px",
            }}
          >
            {menuItems.map(({ icon: Icon, label, action }) => (
              <button
                key={label}
                onClick={() => {
                  setOpen(false);
                  action();
                }}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  width: "100%",
                  padding: "8px 12px",
                  background: "none",
                  border: "none",
                  cursor: "pointer",
                  color: "var(--fg)",
                  fontSize: "0.875rem",
                  borderRadius: "8px",
                  textAlign: "left",
                }}
                onMouseEnter={(e) => {
                  (e.currentTarget as HTMLButtonElement).style.background = "var(--bg-subtle)";
                }}
                onMouseLeave={(e) => {
                  (e.currentTarget as HTMLButtonElement).style.background = "none";
                }}
              >
                <Icon width={15} height={15} />
                {label}
              </button>
            ))}
            <div style={{ margin: "4px 0", borderTop: "1px solid var(--border)" }} />
            <button
              onClick={handleSignOut}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "10px",
                width: "100%",
                padding: "8px 12px",
                background: "none",
                border: "none",
                cursor: "pointer",
                color: "var(--danger, #ef4444)",
                fontSize: "0.875rem",
                borderRadius: "8px",
                textAlign: "left",
              }}
              onMouseEnter={(e) => {
                (e.currentTarget as HTMLButtonElement).style.background = "var(--bg-subtle)";
              }}
              onMouseLeave={(e) => {
                (e.currentTarget as HTMLButtonElement).style.background = "none";
              }}
            >
              <LogOut width={15} height={15} />
              Sign out
            </button>
          </div>
        </>
      )}
    </div>
  );
}
