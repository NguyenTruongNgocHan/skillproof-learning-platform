import { useAuth } from "@/features/auth/hooks/useAuth"
import type { NavItem } from "@/app/config/navigation"
import { WorkspaceSidebar } from "./WorkspaceSidebar"
export default function AppSidebar({
  items,
  collapsed = false,
  onToggle,
}: {
  items: NavItem[]
  collapsed?: boolean
  onToggle?: () => void
}) {
  const { user } = useAuth()
  const home =
    user?.role === "ADMIN" ? "/admin" : user?.role === "ORGANIZER" ? "/organizer" : "/app"
  return <WorkspaceSidebar items={items} collapsed={collapsed} toggle={onToggle} home={home} />
}
