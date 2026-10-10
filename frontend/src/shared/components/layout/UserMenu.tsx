import { LogOut, Settings, User } from "lucide-react"
import { useEffect, useRef, useState } from "react"
import { useNavigate } from "react-router-dom"

import { useAuth } from "@/features/auth/hooks/useAuth"
import type { User as UserType } from "@/features/auth/types/auth.types"
import Avatar from "@/shared/ui/Avatar"

interface UserMenuProps {
  user: UserType
}

export default function UserMenu({ user }: UserMenuProps) {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()
  const { logout } = useAuth()

  useEffect(() => {
    if (!open) return
    const onKeyDown = (event: KeyboardEvent) => event.key === "Escape" && setOpen(false)
    const onPointerDown = (event: PointerEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false)
    }
    window.addEventListener("keydown", onKeyDown)
    window.addEventListener("pointerdown", onPointerDown)
    return () => {
      window.removeEventListener("keydown", onKeyDown)
      window.removeEventListener("pointerdown", onPointerDown)
    }
  }, [open])

  async function handleSignOut() {
    setOpen(false)
    await logout()
    navigate("/", { replace: true })
  }

  const displayName = user.fullName?.trim() || "SkillProof learner"
  const email = user.email ?? ""

  return (
    <div className="user-menu" ref={rootRef}>
      <button
        type="button"
        className={`user-menu__trigger${open ? " is-open" : ""}`}
        onClick={() => setOpen((value) => !value)}
        aria-label="Open account menu"
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <Avatar name={displayName || email || "User"} size="sm" />
      </button>

      {open && (
        <div className="user-menu__popover" role="menu">
          <div className="user-menu__identity">
            <Avatar name={displayName || email || "User"} size="md" />
            <div>
              <strong>{displayName}</strong>
              {email && <span>{email}</span>}
            </div>
          </div>
          <div className="user-menu__divider" />
          <button
            type="button"
            role="menuitem"
            className="user-menu__item"
            onClick={() => {
              setOpen(false)
              navigate("/profile")
            }}
          >
            <User size={16} />
            <span>Profile</span>
          </button>
          <button
            type="button"
            role="menuitem"
            className="user-menu__item"
            onClick={() => {
              setOpen(false)
              navigate("/profile")
            }}
          >
            <Settings size={16} />
            <span>Account settings</span>
          </button>
          <div className="user-menu__divider" />
          <button
            type="button"
            role="menuitem"
            className="user-menu__item user-menu__item--danger"
            onClick={() => void handleSignOut()}
          >
            <LogOut size={16} />
            <span>Sign out</span>
          </button>
        </div>
      )}
    </div>
  )
}
