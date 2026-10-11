import type { UserRole } from "@/features/auth/types/auth.types";
import type { LucideIcon } from "lucide-react";
import {
  Award,
  BookOpen,
  LayoutDashboard,
  LibraryBig,
  ListChecks,
  UserRound,
} from "lucide-react";

export interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
  badge?: number;
}

export const LEARNER_NAV: NavItem[] = [
  { label: "Resources", path: "/resources", icon: LibraryBig },
  {
    label: "Certificate feedback",
    path: "/app/certificate-feedback",
    icon: Award,
  },
  { label: "Overview", path: "/app", icon: LayoutDashboard },
  { label: "My learning", path: "/app/learning", icon: BookOpen },
  { label: "Course catalog", path: "/catalog", icon: LibraryBig },
  { label: "Explore paths", path: "/learning-paths", icon: LibraryBig },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
];

export const ORGANIZER_NAV: NavItem[] = [
  { label: "Overview", path: "/organizer", icon: LayoutDashboard },
  {
    label: "Organization & team",
    path: "/organizer/organization",
    icon: UserRound,
  },
  {
    label: "Verification & issuer",
    path: "/organizer/governance",
    icon: Award,
  },
  { label: "Courses", path: "/organizer/courses", icon: BookOpen },
  {
    label: "Reach & analytics",
    path: "/organizer/analytics",
    icon: LayoutDashboard,
  },
  { label: "Learner feedback", path: "/organizer/feedback", icon: ListChecks },
  {
    label: "Activity & notifications",
    path: "/organizer/activity",
    icon: ListChecks,
  },
  { label: "Resource library", path: "/organizer/library", icon: LibraryBig },
  { label: "Learner access", path: "/organizer/access", icon: UserRound },
  {
    label: "Pricing & promotions",
    path: "/organizer/pricing",
    icon: ListChecks,
  },
  { label: "Assignment grading", path: "/organizer/grading", icon: ListChecks },
  { label: "Question banks", path: "/organizer/questions", icon: ListChecks },
  { label: "Certifications", path: "/organizer/certifications", icon: Award },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
];

export const ADMIN_NAV: NavItem[] = [
  { label: "Certificate governance", path: "/admin/certificates", icon: Award },
  { label: "Resource review", path: "/admin/library", icon: LibraryBig },
  {
    label: "Organization governance",
    path: "/admin/organization-status",
    icon: UserRound,
  },
  { label: "Issuer review", path: "/admin/issuers", icon: Award },
  { label: "Feedback moderation", path: "/admin/feedback", icon: ListChecks },
  { label: "Overview", path: "/admin", icon: LayoutDashboard },
  {
    label: "Application review",
    path: "/admin/organizations",
    icon: ListChecks,
  },
  { label: "Identity & profile", path: "/profile", icon: UserRound },
];

export function getNavForRole(role: UserRole): NavItem[] {
  switch (role) {
    case "LEARNER":
      return LEARNER_NAV;
    case "ORGANIZER":
      return ORGANIZER_NAV;
    case "ADMIN":
      return ADMIN_NAV;
  }
}
