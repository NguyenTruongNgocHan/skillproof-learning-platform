import { useDialogFocus } from "@/shared/hooks/useDialogFocus"
import type { NavItem } from "@/app/config/navigation"
import AppSidebar from "@/shared/components/layout/AppSidebar"
import { X } from "lucide-react"
import { useRef } from "react"

interface MobileDrawerProps {
  open: boolean
  onClose: () => void
  items: NavItem[]
}

export default function MobileDrawer({ open, onClose, items }: MobileDrawerProps) {
  const drawer = useRef<HTMLElement>(null)
  useDialogFocus(drawer, open, onClose)
  if (!open) return null

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
        ref={drawer}
        role="dialog"
        aria-modal="true"
        aria-label="Workspace navigation"
        tabIndex={-1}
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
  )
}
