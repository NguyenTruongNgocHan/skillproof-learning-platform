import { Users, TrendingUp, Building2, Clock, BookOpen, Award } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export interface MetricCard {
  icon: LucideIcon;
  label: string;
  value: string;
  color: string;
  bg: string;
  highlight?: boolean;
}

export const ADMIN_METRICS: MetricCard[] = [
  {
    icon: Users,
    label: "Total Users",
    value: "1,248",
    color: "#3B82F6",
    bg: "rgba(59,130,246,0.12)",
  },
  {
    icon: TrendingUp,
    label: "Active Learners",
    value: "892",
    color: "var(--success)",
    bg: "var(--success-bg)",
  },
  {
    icon: Building2,
    label: "Organizations",
    value: "23",
    color: "#8B5CF6",
    bg: "rgba(139,92,246,0.12)",
  },
  {
    icon: Clock,
    label: "Pending Approvals",
    value: "3",
    color: "var(--warning)",
    bg: "rgba(245,158,11,0.12)",
    highlight: true,
  },
  {
    icon: BookOpen,
    label: "Published Paths",
    value: "67",
    color: "var(--brand)",
    bg: "var(--brand-soft)",
  },
  {
    icon: Award,
    label: "Certificates Issued",
    value: "412",
    color: "#14B8A6",
    bg: "rgba(20,184,166,0.12)",
  },
];

export interface PendingOrg {
  name: string;
  submitted: string;
}

export const PENDING_ORGS: PendingOrg[] = [
  { name: "TechVentures Vietnam", submitted: "2 days ago" },
  { name: "EduPro Academy", submitted: "3 days ago" },
  { name: "CloudLearn Inc.", submitted: "5 days ago" },
];

export interface PendingPath {
  name: string;
  org: string;
}

export const PENDING_PATHS: PendingPath[] = [
  { name: "Advanced React Patterns", org: "EduPro Academy" },
  { name: "Kubernetes Operations", org: "CloudLearn Inc." },
];

export interface ReportedItem {
  name: string;
  reason: string;
}

export const REPORTED_CONTENT: ReportedItem[] = [
  { name: "Quiz: Java Interview Prep", reason: "Reported for misleading content" },
];

export interface ActivityRow {
  action: string;
  user: string;
  time: string;
  status: string;
}

export const ACTIVITY_ROWS: ActivityRow[] = [
  { action: "User registered", user: "alex.nguyen@gmail.com", time: "5 min ago", status: "Success" },
  { action: "Organization submitted", user: "techventures@vn.com", time: "1 hour ago", status: "Pending" },
  { action: "Certificate issued", user: "system", time: "2 hours ago", status: "Success" },
  { action: "Learning path published", user: "admin@skillproof.io", time: "3 hours ago", status: "Success" },
  { action: "Login attempt failed", user: "unknown@test.com", time: "4 hours ago", status: "Failed" },
  { action: "Quiz reported", user: "learner@gmail.com", time: "Yesterday", status: "Review" },
];
