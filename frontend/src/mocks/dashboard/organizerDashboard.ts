import { BookOpen, Users, FileCheck, Award } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export interface StatCard {
  icon: LucideIcon;
  iconColor: string;
  iconBg: string;
  label: string;
  value: string;
}

export const ORGANIZER_STATS: StatCard[] = [
  {
    icon: BookOpen,
    iconColor: "var(--brand)",
    iconBg: "var(--brand-soft)",
    label: "Published Paths",
    value: "3",
  },
  {
    icon: Users,
    iconColor: "#3B82F6",
    iconBg: "rgba(59,130,246,0.12)",
    label: "Active Learners",
    value: "284",
  },
  {
    icon: FileCheck,
    iconColor: "var(--warning)",
    iconBg: "rgba(245,158,11,0.12)",
    label: "Assessments",
    value: "12",
  },
  {
    icon: Award,
    iconColor: "var(--success)",
    iconBg: "var(--success-bg)",
    label: "Certificates Issued",
    value: "47",
  },
];

export interface PathRow {
  name: string;
  status: "Published" | "Draft";
  enrolled: number | null;
  completion: string | null;
}

export const PATH_ROWS: PathRow[] = [
  {
    name: "Backend Engineering Fundamentals",
    status: "Published",
    enrolled: 128,
    completion: "67%",
  },
  {
    name: "API Design & REST Principles",
    status: "Published",
    enrolled: 84,
    completion: "52%",
  },
  {
    name: "Database Design Mastery",
    status: "Draft",
    enrolled: null,
    completion: null,
  },
];

export interface ActivityItem {
  text: string;
  time: string;
}

export const RECENT_ACTIVITY: ActivityItem[] = [
  { text: "Alex Nguyen completed Module 4 — API Design", time: "2 min ago" },
  { text: "New enrollment: Backend Engineering Path", time: "15 min ago" },
  { text: "Certificate issued to Maria Santos", time: "1 hour ago" },
  { text: "Quiz 'REST API Basics' was published", time: "3 hours ago" },
  { text: "New learner registered via invite link", time: "Yesterday" },
];
