import { useEffect } from "react";
import { X } from "lucide-react";
import AppSidebar from "@/components/layout/AppSidebar";
import type { NavItem } from "@/config/navigation";

interface MobileDrawerProps {
  open: boolean;
  onClose: () => void;
  items: NavItem[];
}

export default function MobileDrawer({
  open,
  onClose,
  items,
}: MobileDrawerProps) {
  useEffect(() => {
    if (!open) return;
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);
  if (!open) return null;

  return (
    <>
      <div
        style={{
          position: "fixed",
          inset: 0,
          background: "rgba(0,0,0,0.4)",
          zIndex: 50,
        }}
        onClick={onClose}
      />
      <aside
        style={{
          position: "fixed",
          top: 0,
          left: 0,
          bottom: 0,
          width: "256px",
          zIndex: 60,
        }}
      >
        <div style={{ position: "absolute", top: 12, right: -40 }}>
          <button
            type="button"
            aria-label="Close navigation"
            onClick={onClose}
            style={{
              background: "var(--surface)",
              border: "1px solid var(--border)",
              borderRadius: "8px",
              padding: "6px",
              cursor: "pointer",
              color: "var(--fg)",
              display: "flex",
            }}
          >
            <X width={18} height={18} />
          </button>
        </div>
        <AppSidebar items={items} />
      </aside>
    </>
  );
}
