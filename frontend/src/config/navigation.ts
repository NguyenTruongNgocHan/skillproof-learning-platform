import {
  LayoutDashboard,
  UserRound,
  BookOpen,
  LibraryBig,
  ListChecks,
  Award,
} from "lucide-react"
import type { LucideIcon } from "lucide-react"
import type { UserRole } from "@/features/auth/types/auth.types"

export interface NavItem {
  label: string
  path: string
  icon: LucideIcon
  badge?: number
}

export const LEARNER_NAV: NavItem[] = [
  { label: "Overview", path: "/app", icon: LayoutDashboard },
  { label: "My learning", path: "/app/learning", icon: BookOpen },
  { label: "Explore paths", path: "/learning-paths", icon: LibraryBig },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
]

export const ORGANIZER_NAV: NavItem[] = [
  { label: "Overview", path: "/organizer", icon: LayoutDashboard },
  {
    label: "Organization & team",
    path: "/organizer/organization",
    icon: UserRound,
  },
  { label: "Learning paths", path: "/organizer/paths", icon: BookOpen },
  { label: "Question banks", path: "/organizer/questions", icon: ListChecks },
  { label: "Certifications", path: "/organizer/certifications", icon: Award },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
]

export const ADMIN_NAV: NavItem[] = [
  { label: "Overview", path: "/admin", icon: LayoutDashboard },
  {
    label: "Application review",
    path: "/admin/organizations",
    icon: ListChecks,
  },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
]

export function getNavForRole(role: UserRole): NavItem[] {
  switch (role) {
    case "LEARNER":
      return LEARNER_NAV
    case "ORGANIZER":
      return ORGANIZER_NAV
    case "ADMIN":
      return ADMIN_NAV
  }
}
